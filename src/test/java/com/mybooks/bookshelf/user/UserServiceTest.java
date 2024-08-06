package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.email.EmailService;
import com.mybooks.bookshelf.user.payload.LoginRequest;
import com.mybooks.bookshelf.user.payload.LoginResponse;
import com.mybooks.bookshelf.user.payload.RegisterRequest;
import com.mybooks.bookshelf.user.token.Token;
import com.mybooks.bookshelf.user.token.TokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

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

        userService = new UserService(userRepository, passwordEncoder, tokenService, emailService, jsonWebToken);
    }

    @AfterEach
    void tearDown() {
        userRepository.clear();
    }

    @Test
    void whenCorrectUserDataProvided_CreateUserAndSendEmail() {
        RegisterRequest request = new RegisterRequest("Tom", "tom@gmail.com", "123");
        String encodedPassword = "encodedPassword";
        String emailContent = "Mocked email content";
        when(passwordEncoder.encode(request.password())).thenReturn(encodedPassword);
        when(emailService.buildEmail(anyString(), anyString())).thenReturn(emailContent);

        userService.createUser(request);

        User savedUser = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new AssertionError("User should be present in the InMemoryUserRepository."));
        assertEquals(encodedPassword, savedUser.getPassword());
        verify(tokenService, times(1)).saveToken(any(Token.class)); // Check if token is saved.
        verify(emailService).send(request.email(), emailContent); // Check if email is sent.
    }

    @Test
    void whenEmailAlreadyTaken_ThrowEmailException() {
        User existingUser = new User("Bob", "tom@gmail.com", "111", UserRole.USER);
        userRepository.save(existingUser);
        RegisterRequest request = new RegisterRequest("Tom", "tom@gmail.com", "123");

        EmailException e = assertThrows(EmailException.class, () ->
                userService.createUser(request));

        assertEquals("This email is already associated with some account.", e.getMessage());
    }

    @Test
    void whenUserExistsAndAsksForNewConfirmation_SendNewConfirmationEmail() {
        User existingUser = new User("Bob", "tom@gmail.com", "111", UserRole.USER);
        userRepository.save(existingUser);
        String emailContent = "Mocked email content";
        when(emailService.buildEmail(anyString(), anyString())).thenReturn(emailContent);

        userService.sendNewConfirmationEmail(existingUser.getId().toString());

        verify(tokenService).saveToken(any(Token.class)); // Check if token is saved.
        verify(emailService).send(existingUser.getEmail(), emailContent); // Check if email is sent.
    }

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
    void whenCredentialsCorrect_ReturnSuccessfulLoginResponse() {
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

}
