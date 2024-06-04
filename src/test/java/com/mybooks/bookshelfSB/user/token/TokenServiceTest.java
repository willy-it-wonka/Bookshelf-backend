package com.mybooks.bookshelfSB.user.token;

import com.mybooks.bookshelfSB.user.InMemoryUserRepository;
import com.mybooks.bookshelfSB.user.User;
import com.mybooks.bookshelfSB.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class TokenServiceTest {

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

        Optional<Token> foundToken = tokenRepository.findByToken("test-token");
        assertTrue(foundToken.isPresent());
        assertEquals(user, foundToken.get().getTokenOwner());
    }

    @Test
    void whenTokenExists_ConfirmToken() {
        tokenRepository.save(token);

        String result = tokenService.confirmToken(token.getToken());

        User confirmedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new AssertionError("User should be present in the InMemoryUserRepository."));
        Token updatedToken = tokenRepository.findByToken(token.getToken())
                .orElseThrow(() -> new AssertionError("Token should be present in the InMemoryTokenRepository."));

        assertNotNull(updatedToken.getConfirmationDate()); // Check: setConfirmationDate(token);
        assertTrue(confirmedUser.getEnabled()); //Check: enableUser(confirmationToken.getTokenOwner().getEmail());
        assertEquals("Token confirmed.", result);
    }

    @Test
    void whenTokenToConfirmationDoesNotExist_ThrowIllegalStateException() {
        String invalidToken = "invalid-token";

        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                tokenService.confirmToken(invalidToken));

        assertEquals("Token not found.", e.getMessage());
    }

    @Test
    void whenTokenAlreadyConfirmed_ThrowIllegalStateException() {
        token.setConfirmationDate(testTime.minusMinutes(5));
        tokenRepository.save(token);

        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                tokenService.confirmToken(token.getToken()));

        assertEquals("Email already confirmed.", e.getMessage());
    }

    @Test
    void whenTokenExpired_ThrowIllegalStateException() {
        token.setExpirationDate(testTime.minusMinutes(60));
        tokenRepository.save(token);

        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                tokenService.confirmToken(token.getToken()));

        assertEquals("Token expired.", e.getMessage());
    }

}
