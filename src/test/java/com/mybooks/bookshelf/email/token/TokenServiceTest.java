package com.mybooks.bookshelf.email.token;

import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.user.InMemoryUserRepository;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        tokenService = new TokenService(tokenRepository, userRepository);

        user = new User("Tom", "tom@example.com", "123", UserRole.USER);
        userRepository.save(user);

        testTime = LocalDateTime.now();
        token = new Token("test-token", testTime, testTime.plusMinutes(30), user);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.clear();
        userRepository.clear();
    }

    @Test
    void whenCorrectTokenDataProvided_SaveToken() {
        tokenService.saveToken(token);

        Optional<Token> foundToken = tokenRepository.findByConfirmationToken("test-token");
        assertTrue(foundToken.isPresent());
        assertEquals(user, foundToken.get().getTokenOwner());
    }

    @Test
    void whenTokenExists_ConfirmToken() {
        tokenRepository.save(token);

        String result = tokenService.confirmToken(token.getConfirmationToken());

        User confirmedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new AssertionError("User should be present in the InMemoryUserRepository."));
        Token updatedToken = tokenRepository.findByConfirmationToken(token.getConfirmationToken())
                .orElseThrow(() -> new AssertionError("Token should be present in the InMemoryTokenRepository."));

        assertNotNull(updatedToken.getConfirmationDate());
        assertTrue(confirmedUser.getEnabled());
        assertEquals("Token confirmed.", result);
    }

    @Test
    void whenTokenToConfirmationDoesNotExist_ThrowTokenException() {
        String invalidToken = "invalid-token";

        TokenException e = assertThrows(TokenException.class, () ->
                tokenService.confirmToken(invalidToken));

        assertEquals("Email confirmation error: token not found.", e.getMessage());
    }

    @Test
    void whenTokenAlreadyConfirmed_ThrowTokenException() {
        token.setConfirmationDate(testTime.minusMinutes(5));
        tokenRepository.save(token);
        String updatedToken = token.getConfirmationToken();

        TokenException e = assertThrows(TokenException.class, () ->
                tokenService.confirmToken(updatedToken));

        assertEquals("Email confirmation error: email already confirmed.", e.getMessage());
    }

    @Test
    void whenTokenExpired_ThrowTokenException() {
        token.setExpirationDate(testTime.minusMinutes(60));
        tokenRepository.save(token);
        String updatedToken = token.getConfirmationToken();

        TokenException e = assertThrows(TokenException.class, () ->
                tokenService.confirmToken(updatedToken));

        assertEquals("Email confirmation error: token expired.", e.getMessage());
    }

    @Test
    void whenUserHasToken_ReturnLatestToken() {
        tokenRepository.save(token);

        Token latestToken = tokenService.getLatestUserToken(user);

        assertNotNull(latestToken);
        assertEquals(token, latestToken);
    }

    @Test
    void whenUserHasNoToken_ThrowTokenException() {
        TokenException e = assertThrows(TokenException.class, () -> tokenService.getLatestUserToken(user));
        assertEquals("Email confirmation error: token not found.", e.getMessage());
    }

    @Test
    void whenUserHasMultipleTokens_ReturnLatestToken() {
        Token newerToken = new Token("new-token", testTime.plusMinutes(5), testTime.plusMinutes(35), user);
        tokenRepository.save(token);
        tokenRepository.save(newerToken);

        Token latestToken = tokenService.getLatestUserToken(user);

        assertNotNull(latestToken);
        assertEquals(newerToken, latestToken);
        assertEquals(newerToken.getCreationDate(), latestToken.getCreationDate());
    }

}
