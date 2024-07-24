package com.mybooks.bookshelf.user;

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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryIT {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testDB")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
    }

    @BeforeEach
    void setUp() {
        user = new User("Tom", "tom@gmail.com", "123", UserRole.USER);
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void whenCorrectUserDataProvided_SaveUser() {
        User savedUser = userRepository.save(user);
        assertThat(entityManager.find(User.class, savedUser.getId())).isEqualTo(user);
    }

    @Test
    void whenEmailExists_ReturnUser() {
        entityManager.persist(user);

        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(user);
    }

    @Test
    void whenEmailDoesNotExist_ReturnEmpty() {
        entityManager.persist(user);
        Optional<User> notFoundUser = userRepository.findByEmail("wrong@gmail.com");
        assertThat(notFoundUser).isNotPresent();
    }

    @Test
    void whenIdExists_ReturnUser() {
        entityManager.persist(user);

        Optional<User> foundUser = userRepository.findById(user.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get()).isEqualTo(user);
    }

    @Test
    void whenIdDoesNotExist_ReturnEmpty() {
        entityManager.persist(user);
        Optional<User> notFoundUser = userRepository.findById(999L);
        assertThat(notFoundUser).isNotPresent();
    }

    @Test
    void whenEmailExists_UpdateEnabledAndReturnPositive() {
        user.setEnabled(false);
        entityManager.persist(user);

        int updatedCount = userRepository.updateEnabled(user.getEmail());
        entityManager.refresh(user);

        assertThat(updatedCount).isGreaterThan(0);
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void whenEmailDoesNotExist_NoUpdateAndReturnZero() {
        user.setEnabled(true);
        entityManager.persist(user);

        int updatedCount = userRepository.updateEnabled(user.getEmail());
        entityManager.refresh(user);

        assertThat(updatedCount).isZero();
        assertThat(user.isEnabled()).isTrue();
    }

}
