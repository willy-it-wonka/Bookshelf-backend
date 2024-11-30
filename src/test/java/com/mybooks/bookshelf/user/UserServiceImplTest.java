package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.EmailService;
import com.mybooks.bookshelf.email.token.Token;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.exception.IncorrectPasswordException;
import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.exception.UserNotFoundException;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.payload.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private static final String CORRECT_PASSWORD = "correctPassword";
    private static final String WRONG_PASSWORD = "wrongPassword";
    private static final String ENCODED_PASSWORD = "encodedPassword";
    private static final String WRONG_EMAIL = "wrong@email.com";
    private static final String EMAIL_CONTENT = "Mocked email content";
    private static final String EMAIL_ALREADY_TAKEN_ERROR = "This email is already associated with some account.";
    private static final String USER_NOT_FOUND_ERROR = "User not found.";
    private static final String INCORRECT_PASSWORD_ERROR = "Incorrect password.";
    private static final String ASSERTION_ERROR = "User should be present in the InMemoryUserRepository.";

    private InMemoryUserRepository userRepository;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private TokenService tokenService;
    private EmailService emailService;
    private JsonWebToken jsonWebToken;
    private User user;

    @Value("${email.confirmation.path}")
    private String emailConfirmationPath;
    @Value("${password.reset.path}")
    private String passwordResetPath;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        passwordEncoder = mock(PasswordEncoder.class);
        tokenService = mock(TokenService.class);
        emailService = mock(EmailService.class);
        jsonWebToken = mock(JsonWebToken.class);
        userService = new UserServiceImpl(userRepository, passwordEncoder, tokenService, emailService, jsonWebToken);

        user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        userRepository.save(user);
    }

    @AfterEach
    void tearDown() {
        userRepository.clear();
    }

    //    REGISTRATION
    @Test
    void whenRegisterRequest_CreateUserAndSendEmail() {
        RegisterRequest request = new RegisterRequest("Tom", "tom@gmail.com", "123");
        when(passwordEncoder.encode(request.password())).thenReturn(ENCODED_PASSWORD);
        when(emailService.buildEmail(eq(emailConfirmationPath), anyString(), anyString())).thenReturn(EMAIL_CONTENT);
        when(tokenService.createConfirmationToken(any(User.class))).thenReturn(new Token("token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), new User()));

        userService.createUser(request);

        User savedUser = userRepository.findByEmail(request.email()).orElseThrow(() -> new AssertionError(ASSERTION_ERROR));
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        verify(emailService).sendConfirmationEmail(request.email(), EMAIL_CONTENT);
    }

    @Test
    void whenEmailAlreadyTaken_ThrowEmailException() {
        RegisterRequest request = new RegisterRequest("Tom", "tom@test.com", "123");
        EmailException e = assertThrows(EmailException.class, () -> userService.createUser(request));
        assertEquals(EMAIL_ALREADY_TAKEN_ERROR, e.getMessage());
    }

    @Test
    void whenTwoRegisterRequestWithSameEmail_CreateUserAndForSecondRequestThrowEmailException() {
        RegisterRequest request1 = new RegisterRequest("Tom", "tom@gmail.com", "123");
        RegisterRequest request2 = new RegisterRequest("Tom", "tom@gmail.com", "123");
        when(tokenService.createConfirmationToken(any(User.class))).thenReturn(new Token("token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), new User()));

        userService.createUser(request1);
        EmailException e = assertThrows(EmailException.class, () -> userService.createUser(request2));

        assertNotNull(userRepository.findByEmail(request1.email()));
        assertEquals(EMAIL_ALREADY_TAKEN_ERROR, e.getMessage());
    }

    //    LOGIN
    @Test
    void whenUserExistsByUsername_ReturnUserDetails() {
        UserDetails userDetails = userService.loadUserByUsername(user.getEmail());

        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user, userDetails);
    }

    @Test
    void whenUserDoesNotExistByUsername_ThrowUserNotFoundException() {
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () ->
                userService.loadUserByUsername(WRONG_EMAIL));
        assertEquals(USER_NOT_FOUND_ERROR, e.getMessage());
    }

    @Test
    void whenUserExistsById_ReturnUser() {
        User foundUser = userService.loadUserById(user.getId());
        assertEquals(user, foundUser);
    }

    @Test
    void whenUserDoesNotExistById_ThrowUserNotFoundException() {
        Long nonExistentUserId = 999L;
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () ->
                userService.loadUserById(nonExistentUserId));
        assertEquals(USER_NOT_FOUND_ERROR, e.getMessage());
    }

    @Test
    void whenLoginRequest_ReturnSuccessfulLoginResponse() {
        when(passwordEncoder.matches("123", user.getPassword())).thenReturn(true);
        when(jsonWebToken.generateToken(user)).thenReturn("JWT");
        LoginRequest request = new LoginRequest("tom@test.com", "123");

        LoginResponse loginResponse = userService.loginUser(request);

        assertEquals("JWT", loginResponse.message());
        assertTrue(loginResponse.status());
    }

    @Test
    void whenIncorrectPassword_ThrowIncorrectPasswordException() {
        LoginRequest request = new LoginRequest("tom@test.com", WRONG_PASSWORD);
        IncorrectPasswordException e = assertThrows(IncorrectPasswordException.class, () -> userService.loginUser(request));
        assertEquals(INCORRECT_PASSWORD_ERROR, e.getMessage());
    }

    @Test
    void whenUserDoesNotExist_ThrowUserNotFoundException() {
        LoginRequest request = new LoginRequest(WRONG_EMAIL, CORRECT_PASSWORD);
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () -> userService.loginUser(request));
        assertEquals(USER_NOT_FOUND_ERROR, e.getMessage());
    }

    //    ACCOUNT MANAGEMENT
    @Test
    void whenUserEnabled_ReturnTrue() {
        user.setEnabled(true);
        assertTrue(userService.isEnabled(user.getId().toString()));
    }

    @Test
    void whenUserDisabled_ReturnFalse() {
        user.setEnabled(false);
        assertFalse(userService.isEnabled(user.getId().toString()));
    }

    @Test
    void whenUserExistsAndIsNotEnabled_EnableUserAndReturnPositive() {
        user.setEnabled(false);

        int result = userService.enableUser(user.getEmail());

        assertThat(result).isPositive();
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new AssertionError(ASSERTION_ERROR));
        assertTrue(updatedUser.isEnabled());
    }

    @Test
    void whenUserAlreadyEnabled_DoNothingAndReturnZero() {
        user.setEnabled(true);

        int result = userService.enableUser(user.getEmail());

        assertThat(result).isZero();
        User updatedUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new AssertionError(ASSERTION_ERROR));
        assertTrue(updatedUser.isEnabled());
    }

    @Test
    void whenUserDoesNotExist_ReturnZero() {
        int result = userService.enableUser(WRONG_EMAIL);
        assertThat(result).isZero();
    }

    @Test
    void whenEnoughTimePassed_SendNewConfirmationEmail() {
        // Mock the last token creation time to be more than 5 minutes ago.
        Token lastToken = new Token("token", LocalDateTime.now().minusMinutes(6), LocalDateTime.now().plusMinutes(30), user);
        when(tokenService.getLatestUserToken(user)).thenReturn(lastToken);
        when(emailService.buildEmail(eq(emailConfirmationPath), anyString(), anyString())).thenReturn(EMAIL_CONTENT);
        // Mock creation of a new token.
        Token newToken = new Token("newToken", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), user);
        when(tokenService.createConfirmationToken(user)).thenReturn(newToken);

        userService.sendNewConfirmationEmail(user.getId().toString());

        verify(emailService).sendConfirmationEmail(user.getEmail(), EMAIL_CONTENT);
    }

    @Test
    void whenRequestForNewEmailIsTooSoon_ThrowTokenException() {
        String userId = user.getId().toString();
        // Mock the last token creation time to be less than 5 minutes ago.
        Token lastToken = new Token("token", LocalDateTime.now().minusMinutes(2), LocalDateTime.now().plusMinutes(30), user);
        when(tokenService.getLatestUserToken(user)).thenReturn(lastToken);
        ReflectionTestUtils.setField(userService, "emailResendLimitSeconds", 300);

        TokenException e = assertThrows(TokenException.class, () -> userService.sendNewConfirmationEmail(userId));

        assertTrue(e.getMessage().contains("You can request a new email in"));
        verify(emailService, never()).sendConfirmationEmail(anyString(), anyString());
    }

    @Test
    void whenChangeNickRequest_ChangeNickAndReturnJwt() {
        ChangeNickRequest request = new ChangeNickRequest("newNick", CORRECT_PASSWORD);
        when(passwordEncoder.matches(CORRECT_PASSWORD, "123")).thenReturn(true);
        when(jsonWebToken.generateToken(user)).thenReturn("newJwt");

        ChangeResponse response = userService.changeUserNick(user.getId().toString(), request);

        assertEquals("newJwt", response.response());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(() -> new AssertionError(USER_NOT_FOUND_ERROR));
        assertEquals("newNick", updatedUser.getNick());
    }

    @Test
    void whenChangeNickRequestWithIncorrectPassword_ThrowIncorrectPasswordException() {
        String userId = user.getId().toString();
        ChangeNickRequest request = new ChangeNickRequest("newNick", WRONG_PASSWORD);
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        IncorrectPasswordException e = assertThrows(IncorrectPasswordException.class, () ->
                userService.changeUserNick(userId, request));

        assertEquals(INCORRECT_PASSWORD_ERROR, e.getMessage());
    }

    @Test
    void whenEmailIsAlreadyTaken_ThrowEmailException() {
        String userId = user.getId().toString();
        String existingEmail = "existing@test.com";
        userRepository.save(new User("Alice", existingEmail, ENCODED_PASSWORD, UserRole.USER));
        ChangeEmailRequest request = new ChangeEmailRequest(existingEmail, CORRECT_PASSWORD);

        EmailException exception = assertThrows(EmailException.class, () -> userService.changeUserEmail(userId, request));

        assertEquals(EMAIL_ALREADY_TAKEN_ERROR, exception.getMessage());
    }

    @Test
    void whenChangeEmailRequestWithIncorrectPassword_ThrowIncorrectPasswordException() {
        String userId = user.getId().toString();
        ChangeEmailRequest request = new ChangeEmailRequest("new@test.com", WRONG_PASSWORD);
        when(passwordEncoder.matches(WRONG_PASSWORD, ENCODED_PASSWORD)).thenReturn(false);

        IncorrectPasswordException exception = assertThrows(IncorrectPasswordException.class, () ->
                userService.changeUserEmail(userId, request));

        assertEquals(INCORRECT_PASSWORD_ERROR, exception.getMessage());
    }

    @Test
    void whenChangeEmailRequest_UpdateEmailAndReturnSuccessMessage() {
        ChangeEmailRequest request = new ChangeEmailRequest("new@test.com", CORRECT_PASSWORD);
        when(passwordEncoder.matches(CORRECT_PASSWORD, "123")).thenReturn(true);

        ChangeResponse response = userService.changeUserEmail(user.getId().toString(), request);

        assertEquals("Your email has been successfully changed.", response.response());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(() -> new AssertionError(USER_NOT_FOUND_ERROR));
        assertEquals("new@test.com", updatedUser.getEmail());
    }

    @Test
    void whenChangePasswordRequest_UpdateAndReturnSuccessMessage() {
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword", CORRECT_PASSWORD);
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encodedNewPassword");

        ChangeResponse response = userService.changeUserPassword(user.getId().toString(), request);

        assertEquals("Your password has been successfully changed.", response.response());
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(() -> new AssertionError(USER_NOT_FOUND_ERROR));
        assertEquals("encodedNewPassword", updatedUser.getPassword());
    }

    @Test
    void whenChangePasswordRequestWithIncorrectCurrentPassword_ThrowIncorrectPasswordException() {
        String userId = user.getId().toString();
        ChangePasswordRequest request = new ChangePasswordRequest("newPassword", WRONG_PASSWORD);
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(false);

        IncorrectPasswordException e = assertThrows(IncorrectPasswordException.class, () ->
                userService.changeUserPassword(userId, request));

        assertEquals(INCORRECT_PASSWORD_ERROR, e.getMessage());
    }

    @Test
    void whenInitiateForgottenPasswordReset_SendResetEmail() {
        String email = user.getEmail();
        InitiateResetPasswordRequest request = new InitiateResetPasswordRequest(email);
        Token token = new Token("token", LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), user);
        when(tokenService.getLatestUserToken(user)).thenReturn(token);
        when(tokenService.createConfirmationToken(user)).thenReturn(token);
        when(emailService.buildEmail(eq(passwordResetPath), anyString(), anyString())).thenReturn(EMAIL_CONTENT);

        String response = userService.initiateForgottenPasswordReset(request);

        assertEquals("Check your email inbox.", response);
        verify(emailService).sendPasswordResetEmail(email, EMAIL_CONTENT);
    }

    @Test
    void whenEmailDoesNotExist_ThrowUserNotFoundException() {
        InitiateResetPasswordRequest request = new InitiateResetPasswordRequest(WRONG_EMAIL);
        UserNotFoundException e = assertThrows(UserNotFoundException.class, () -> userService.initiateForgottenPasswordReset(request));
        assertEquals(USER_NOT_FOUND_ERROR, e.getMessage());
    }

    @Test
    void whenEmailSentTooRecently_ThrowTokenException() {
        InitiateResetPasswordRequest request = new InitiateResetPasswordRequest(user.getEmail());
        // Mock the last token creation time to be less than 5 minutes ago.
        Token lastToken = new Token("token", LocalDateTime.now().minusMinutes(2), LocalDateTime.now().plusMinutes(30), user);
        when(tokenService.getLatestUserToken(user)).thenReturn(lastToken);
        ReflectionTestUtils.setField(userService, "emailResendLimitSeconds", 300);

        TokenException e = assertThrows(TokenException.class, () -> userService.initiateForgottenPasswordReset(request));

        assertTrue(e.getMessage().contains("You can request a new email in"));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void whenResetForgottenPassword_UpdatePasswordAndReturnSuccessMessage() {
        String confirmationToken = "123";
        Token token = new Token(confirmationToken, LocalDateTime.now().minusMinutes(10), LocalDateTime.now().plusMinutes(30), user);
        ResetPasswordRequest request = new ResetPasswordRequest(confirmationToken, "newPassword");
        when(tokenService.confirmPasswordResetToken(confirmationToken)).thenReturn(token);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encodedNewPassword");

        String response = userService.resetForgottenPassword(request);

        assertEquals("Your password has been successfully reset.", response);
        User updatedUser = userRepository.findById(user.getId()).orElseThrow(() -> new AssertionError(USER_NOT_FOUND_ERROR));
        assertEquals("encodedNewPassword", updatedUser.getPassword());
    }

}
