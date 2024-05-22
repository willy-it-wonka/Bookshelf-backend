package com.mybooks.bookshelfSB.user.token;

import com.mybooks.bookshelfSB.user.User;
import com.mybooks.bookshelfSB.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TokenRepositoryIT {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testDB")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Token token;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void setUp() {
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
    void save_CorrectDataProvided_SavesToken() {
        Token savedToken = tokenRepository.save(token);
        assertThat(entityManager.find(Token.class, savedToken.getId())).isEqualTo(token);
    }

    @Test
    void findByToken_TokenFound_ReturnsIt() {
        entityManager.persist(token);

        Optional<Token> foundToken = tokenRepository.findByToken(token.getToken());

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get()).isEqualTo(token);
    }

    @Test
    void findByToken_NonExistingToken_ReturnsEmpty() {
        entityManager.persist(token);
        Optional<Token> notFoundToken = tokenRepository.findByToken("wrong-token");
        assertThat(notFoundToken).isNotPresent();
    }

    @Test
    void updateConfirmationDate_WithValidToken_UpdatesAndReturnsMoreThanZero() {
        entityManager.persist(token);
        LocalDateTime confirmationDate = LocalDateTime.of(2024, 5, 16, 12, 10);

        int updatedResp = tokenRepository.updateConfirmationDate(token.getToken(), confirmationDate);
        entityManager.refresh(token);

        assertThat(updatedResp).isGreaterThan(0);
        assertThat(token.getConfirmationDate()).isEqualTo(confirmationDate);
    }

    @Test
    void updateConfirmationDate_WithInvalidToken_NoUpdatesAndReturnsZero() {
        entityManager.persist(token);
        LocalDateTime confirmationDate = LocalDateTime.of(2024, 5, 16, 12, 10);

        int updatedResp = tokenRepository.updateConfirmationDate("wrong-token", confirmationDate);
        entityManager.refresh(token);

        assertThat(updatedResp).isZero();
    }

}
