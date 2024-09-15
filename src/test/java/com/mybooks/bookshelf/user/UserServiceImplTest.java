package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.EmailService;
import com.mybooks.bookshelf.email.token.Token;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.payload.LoginRequest;
import com.mybooks.bookshelf.user.payload.LoginResponse;
import com.mybooks.bookshelf.user.payload.RegisterRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private InMemoryUserRepository userRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private TokenService tokenService;
    private EmailService emailService;
    private JsonWebToken jsonWebToken;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        passwordEncoder = mock(PasswordEncoder.class);
        tokenService = mock(TokenService.class);
        emailService = mock(EmailService.class);
        jsonWebToken = mock(JsonWebToken.class);

        userService = new UserServiceImpl(userRepository, passwordEncoder, tokenService, emailService, jsonWebToken);
    }

    @AfterEach
    void tearDown() {
        userRepository.clear();
    }

    //    REGISTRATION
    @Test
    void whenCorrectRegisterRequest_CreateUserAndSendEmail() {
        RegisterRequest request = new RegisterRequest("Tom", "tom@gmail.com", "123");
        String encodedPassword = "encodedPassword";
        String emailContent = "Mocked email content";
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(emailService.buildEmail(anyString(), anyString())).thenReturn(emailContent);
        Token token = new Token("token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), new User());
        when(tokenService.createConfirmationToken(any(User.class))).thenReturn(token);

        userService.createUser(request);

        User savedUser = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new AssertionError("User should be present in the InMemoryUserRepository."));
        assertEquals(encodedPassword, savedUser.getPassword());
        verify(emailService).send(request.email(), emailContent);
    }

    @Test
    void whenEmailAlreadyTaken_ThrowEmailException() {
        User existingUser = new User("Bob", "tom@gmail.com", "111", UserRole.USER);
        userRepository.save(existingUser);
        RegisterRequest request = new RegisterRequest("Tom", "tom@gmail.com", "123");

        EmailException e = assertThrows(EmailException.class, () -> userService.createUser(request));

        assertEquals("This email is already associated with some account.", e.getMessage());
    }

    @Test
    void whenTwoRegisterRequestWithSameEmail_ThrowEmailException() {
        RegisterRequest request1 = new RegisterRequest("Tom", "tom@gmail.com", "123");
        RegisterRequest request2 = new RegisterRequest("Tom", "tom@gmail.com", "123");
        // Mock token creation for request1.
        Token mockToken = new Token("token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), new User());
        when(tokenService.createConfirmationToken(any(User.class))).thenReturn(mockToken);

        userService.createUser(request1);
        EmailException e = assertThrows(EmailException.class, () -> userService.createUser(request2));

        assertEquals("This email is already associated with some account.", e.getMessage());
    }

    //    LOGIN
    @Test
    void whenUserExistsByUsername_ReturnUserDetails() {
        User existingUser = new User("Martin", "martin@gmail.com", "123", UserRole.USER);
        userRepository.save(existingUser);

        UserDetails userDetails = userService.loadUserByUsername(existingUser.getEmail());

        assertEquals(existingUser.getEmail(), userDetails.getUsername());
        assertEquals(existingUser, userDetails);
    }

    @Test
    void whenUserDoesNotExistByUsername_ThrowUsernameNotFoundException() {
        String nonExistentEmail = "wrong@email.com";

        UsernameNotFoundException e = assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername(nonExistentEmail));

        assertEquals("User not found.", e.getMessage());
    }

    @Test
    void whenUserExistsById_ReturnUser() {
        User existingUser = new User("Martin", "martin@gmail.com", "123", UserRole.USER);
        userRepository.save(existingUser);

        User user = userService.loadUserById(existingUser.getId());

        assertEquals(existingUser.getEmail(), user.getEmail());
        assertEquals(existingUser, user);
    }

    @Test
    void whenUserDoesNotExistById_ThrowUsernameNotFoundException() {
        Long nonExistentUserId = 999L;

        UsernameNotFoundException e = assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserById(nonExistentUserId));

        assertEquals("User not found.", e.getMessage());
    }

    @Test
    void whenCorrectLoginRequest_ReturnSuccessfulLoginResponse() {
        User user = new User("Tom", "tom@gmail.com", passwordEncoder.encode("123"), UserRole.USER);
        userRepository.save(user);
        when(passwordEncoder.matches("123", user.getPassword())).thenReturn(true);
        when(jsonWebToken.generateToken(user)).thenReturn("JWT");
        LoginRequest request = new LoginRequest("tom@gmail.com", "123");

        LoginResponse loginResponse = userService.loginUser(request);

        assertTrue(loginResponse.status());
        assertEquals("JWT", loginResponse.message());
    }

    @Test
    void whenIncorrectPassword_ReturnFailureLoginResponse() {
        User user = new User("Tom", "tom@gmail.com", passwordEncoder.encode("123"), UserRole.USER);
        userRepository.save(user);
        LoginRequest request = new LoginRequest("tom@gmail.com", "wrongPassword");

        LoginResponse loginResponse = userService.loginUser(request);

        assertFalse(loginResponse.status());
        assertEquals("Incorrect password.", loginResponse.message());
    }

    @Test
    void whenUserDoesNotExist_ReturnFailureLoginResponse() {
        LoginRequest request = new LoginRequest("non@gmail.com", "123");
        // Don't save user in InMemoryUserRepository.

        LoginResponse loginResponse = userService.loginUser(request);

        assertFalse(loginResponse.status());
        assertEquals("User not found.", loginResponse.message());
    }

    //    ACCOUNT MANAGEMENT
    @Test
    void whenUserEnabled_ReturnTrue() {
        User user = new User("Tom", "tom@gmail.com", "123", UserRole.USER);
        user.setEnabled(true);
        userRepository.save(user);

        assertTrue(userService.isEnabled(user.getId().toString()));
    }

    @Test
    void whenUserDisabled_ReturnFalse() {
        User user = new User("Tom", "tom@example.com", "123", UserRole.USER);
        user.setEnabled(false);
        userRepository.save(user);

        assertFalse(userService.isEnabled(user.getId().toString()));
    }

    @Test
    void whenUserExistsAndIsNotEnabled_EnableUserAndReturnPositive() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        user.setEnabled(false);
        userRepository.save(user);

        int result = userService.enableUser(user.getEmail());

        assertThat(result).isPositive();
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() ->
                new AssertionError("User should be present in the InMemoryUserRepository."));
        assertTrue(updatedUser.isEnabled());
    }

    @Test
    void whenUserAlreadyEnabled_ThenDoNothingAndReturnZero() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        user.setEnabled(true);
        userRepository.save(user);

        int result = userService.enableUser(user.getEmail());

        assertEquals(0, result);
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() ->
                new AssertionError("User should be present in the InMemoryUserRepository."));
        assertTrue(updatedUser.isEnabled());
    }

    @Test
    void whenUserDoesNotExist_ThenReturnZero() {
        String email = "nonexistent@test.com";
        int result = userService.enableUser(email);
        assertEquals(0, result);
    }

    @Test
    void whenEnoughTimePassed_SendNewConfirmationEmail() {
        User existingUser = new User("Bob", "tom@gmail.com", "111", UserRole.USER);
        userRepository.save(existingUser);
        String emailContent = "Mocked email content";
        // Mock the last token creation time to be more than 5 minutes ago.
        Token lastToken = new Token("token", LocalDateTime.now().minusMinutes(6), LocalDateTime.now().plusMinutes(30), existingUser);
        when(tokenService.getLatestUserToken(existingUser)).thenReturn(lastToken);
        when(emailService.buildEmail(anyString(), anyString())).thenReturn(emailContent);
        // Mock creation of a new token.
        Token newToken = new Token("new-token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), existingUser);
        when(tokenService.createConfirmationToken(existingUser)).thenReturn(newToken);

        userService.sendNewConfirmationEmail(existingUser.getId().toString());

        verify(emailService).send(existingUser.getEmail(), emailContent);
    }

    @Test
    void whenRequestForNewEmailIsTooSoon_ThrowTokenException() {
        User existingUser = new User("Bob", "tom@gmail.com", "111", UserRole.USER);
        userRepository.save(existingUser);
        String userId = existingUser.getId().toString();

        // Mock the last token creation time to be less than 5 minutes ago.
        Token lastToken = new Token("token", LocalDateTime.now().minusMinutes(2), LocalDateTime.now().plusMinutes(30), existingUser);
        when(tokenService.getLatestUserToken(existingUser)).thenReturn(lastToken);

        TokenException e = assertThrows(TokenException.class, () -> userService.sendNewConfirmationEmail(userId));

        assertTrue(e.getMessage().contains("You can request a new confirmation email in"));
        verify(emailService, never()).send(anyString(), anyString());
    }

}
