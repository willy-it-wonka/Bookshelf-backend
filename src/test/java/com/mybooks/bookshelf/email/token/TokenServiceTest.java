package com.mybooks.bookshelf.email.token;

import com.mybooks.bookshelf.email.EmailService;
import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.InMemoryUserRepository;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import com.mybooks.bookshelf.user.UserServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TokenServiceTest {

    private InMemoryTokenRepository tokenRepository;
    private InMemoryUserRepository userRepository;
    private TokenService tokenService;
    private Token token;
    private User user;
    private LocalDateTime testTime;

    @BeforeEach
    void setUp() {
        tokenRepository = new InMemoryTokenRepository();
        userRepository = new InMemoryUserRepository();
        UserServiceImpl userService = new UserServiceImpl(userRepository, mock(PasswordEncoder.class), tokenService, mock(EmailService.class), mock(JsonWebToken.class));
        tokenService = new TokenService(tokenRepository, userService);

        user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        userRepository.save(user);

        testTime = LocalDateTime.now();
        token = new Token("token", testTime, testTime.plusMinutes(30), user);
        tokenRepository.save(token);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.clear();
        userRepository.clear();
    }

    @Test
    void whenCreateConfirmationTokenCalled_SaveTokenAndReturnIt() {
        Token createdToken = tokenService.createConfirmationToken(user);

        assertNotNull(createdToken);
        assertEquals(user, createdToken.getTokenOwner());
    }

    @Test
    void whenTokenExists_ConfirmToken() {
        RedirectView result = tokenService.confirmAccountActivationToken(token.getConfirmationToken());
        String resultUrl = result.getUrl();

        User enabledUser = userRepository.findByEmail(user.getEmail()).orElseThrow(() -> new AssertionError("User should be present in the InMemoryUserRepository."));
        Token confirmedToken = tokenRepository.findByConfirmationToken(token.getConfirmationToken()).orElseThrow(() -> new AssertionError("Token should be present in the InMemoryTokenRepository."));

        assertNotNull(confirmedToken.getConfirmationDate());
        assertTrue(enabledUser.getEnabled());
        assertEquals("/confirmation-success.html", resultUrl);
    }

    @Test
    void whenTokenToConfirmationDoesNotExist_RedirectViewToErrorTemplate() {
        RedirectView result = tokenService.confirmAccountActivationToken("invalid-token");
        assertEquals("/confirmation-error.html?error=Token not found.", result.getUrl());
    }

    @Test
    void whenTokenAlreadyConfirmed_RedirectViewToErrorTemplate() {
        token.setConfirmationDate(testTime.minusMinutes(5));
        String confirmationToken = token.getConfirmationToken();

        RedirectView result = tokenService.confirmAccountActivationToken(confirmationToken);

        assertEquals("/confirmation-error.html?error=Email already confirmed.", result.getUrl());
    }

    @Test
    void whenTokenExpired_RedirectViewToErrorTemplate() {
        token.setExpirationDate(testTime.minusMinutes(60));
        String confirmationToken = token.getConfirmationToken();

        RedirectView result = tokenService.confirmAccountActivationToken(confirmationToken);

        assertEquals("/confirmation-error.html?error=Token expired.", result.getUrl());
    }

    @Test
    void whenUserHasToken_ReturnLatestToken() {
        Token latestToken = tokenService.getLatestUserToken(user);

        assertNotNull(latestToken);
        assertEquals(token, latestToken);
    }

    @Test
    void whenUserHasNoToken_ThrowTokenException() {
        tokenRepository.clear();
        TokenException e = assertThrows(TokenException.class, () -> tokenService.getLatestUserToken(user));
        assertEquals("Token not found.", e.getMessage());
    }

    @Test
    void whenUserHasMultipleTokens_ReturnLatestToken() {
        Token newerToken = new Token("new-token", testTime.plusMinutes(5), testTime.plusMinutes(35), user);
        tokenRepository.save(newerToken);

        Token latestToken = tokenService.getLatestUserToken(user);

        assertNotNull(latestToken);
        assertEquals(newerToken, latestToken);
    }

}
