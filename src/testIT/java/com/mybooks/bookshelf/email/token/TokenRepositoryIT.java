package com.mybooks.bookshelf.email.token;

import com.mybooks.bookshelf.SingletonDatabaseContainer;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TokenRepositoryIT {

    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Token token;
    private User user;
    private LocalDateTime confirmationDate;

    @BeforeAll
    public static void setUpBeforeAll() {
        System.setProperty("spring.datasource.url", SingletonDatabaseContainer.getInstance().getJdbcUrl());
        System.setProperty("spring.datasource.username", SingletonDatabaseContainer.getInstance().getUsername());
        System.setProperty("spring.datasource.password", SingletonDatabaseContainer.getInstance().getPassword());
    }

    @BeforeEach
    void setUpBeforeEach() {
        user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        entityManager.persist(user);

        LocalDateTime testTime = LocalDateTime.of(2024, 5, 16, 12, 0);
        token = new Token("f127e33b-1781-4435-bb0d-a0dff0564ba4", testTime, testTime.plusMinutes(30), user);
        entityManager.persist(token);

        confirmationDate = LocalDateTime.of(2024, 5, 16, 12, 10);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void whenCorrectTokenDataProvided_SaveToken() {
        tokenRepository.deleteAll();
        entityManager.clear();

        Token savedToken = tokenRepository.save(token);

        assertEquals(entityManager.find(Token.class, savedToken.getId()), token);
    }

    @Test
    void whenTokenFound_ReturnToken() {
        Optional<Token> foundToken = tokenRepository.findByConfirmationToken(token.getConfirmationToken());

        assertThat(foundToken).isPresent();
        assertEquals(foundToken.get(), token);
    }

    @Test
    void whenTokenDoesNotExist_ReturnEmpty() {
        Optional<Token> tokenNotFound = tokenRepository.findByConfirmationToken("wrong-token");
        assertThat(tokenNotFound).isNotPresent();
    }

    @Test
    void whenMultipleTokensExist_ReturnLatestToken() {
        LocalDateTime olderTime = LocalDateTime.of(2024, 5, 15, 10, 0);
        Token olderToken = new Token("older-token", olderTime, olderTime.plusMinutes(30), user);
        entityManager.persist(olderToken);

        Optional<Token> latestToken = tokenRepository.findTop1ByTokenOwnerOrderByCreationDateDesc(user);

        assertThat(latestToken).isPresent();
        assertEquals(token, latestToken.get());
    }

    @Test
    void whenNoTokensExistForUser_ReturnEmpty() {
        tokenRepository.deleteAll();
        Optional<Token> latestToken = tokenRepository.findTop1ByTokenOwnerOrderByCreationDateDesc(user);
        assertThat(latestToken).isNotPresent();
    }

    @Test
    void whenValidToken_UpdateConfirmationDateAndReturnPositive() {
        int response = tokenRepository.updateConfirmationDate(token.getConfirmationToken(), confirmationDate);
        entityManager.refresh(token);

        assertThat(response).isPositive();
        assertEquals(token.getConfirmationDate(), confirmationDate);
    }

    @Test
    void whenInvalidToken_NoUpdateAndReturnZero() {
        int response = tokenRepository.updateConfirmationDate("wrong-token", confirmationDate);
        entityManager.refresh(token);

        assertThat(response).isZero();
    }

}
