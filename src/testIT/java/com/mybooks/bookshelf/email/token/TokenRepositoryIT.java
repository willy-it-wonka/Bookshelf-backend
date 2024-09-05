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

    @BeforeAll
    public static void setUpBeforeAll() {
        System.setProperty("spring.datasource.url", SingletonDatabaseContainer.getInstance().getJdbcUrl());
        System.setProperty("spring.datasource.username", SingletonDatabaseContainer.getInstance().getUsername());
        System.setProperty("spring.datasource.password", SingletonDatabaseContainer.getInstance().getPassword());
    }

    @BeforeEach
    void setUpBeforeEach() {
        User user = new User("Tom", "tom@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        LocalDateTime testTime = LocalDateTime.of(2024, 5, 16, 12, 0);
        token = new Token("f127e33b-1781-4435-bb0d-a0dff0564ba4", testTime, testTime.plusMinutes(30), user);
    }

    @AfterEach
    void tearDown() {
        tokenRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void whenCorrectTokenDataProvided_SaveToken() {
        Token savedToken = tokenRepository.save(token);
        assertThat(entityManager.find(Token.class, savedToken.getId())).isEqualTo(token);
    }

    @Test
    void whenTokenFound_ReturnToken() {
        entityManager.persist(token);

        Optional<Token> foundToken = tokenRepository.findByConfirmationToken(token.getConfirmationToken());

        assertThat(foundToken).isPresent();
        assertEquals(foundToken.get(), token);
    }

    @Test
    void whenTokenDoesNotExist_ReturnEmpty() {
        entityManager.persist(token);
        Optional<Token> notFoundToken = tokenRepository.findByConfirmationToken("wrong-token");
        assertThat(notFoundToken).isNotPresent();
    }

    @Test
    void whenValidToken_UpdateConfirmationDateAndReturnPositive() {
        entityManager.persist(token);
        LocalDateTime confirmationDate = LocalDateTime.of(2024, 5, 16, 12, 10);

        int updatedResp = tokenRepository.updateConfirmationDate(token.getConfirmationToken(), confirmationDate);
        entityManager.refresh(token);

        assertThat(updatedResp).isPositive();
        assertThat(token.getConfirmationDate()).isEqualTo(confirmationDate);
    }

    @Test
    void whenInvalidToken_NoUpdateAndReturnZero() {
        entityManager.persist(token);
        LocalDateTime confirmationDate = LocalDateTime.of(2024, 5, 16, 12, 10);

        int updatedResp = tokenRepository.updateConfirmationDate("wrong-token", confirmationDate);
        entityManager.refresh(token);

        assertThat(updatedResp).isZero();
    }

}
