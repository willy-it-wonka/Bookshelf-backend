package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.SingletonDatabaseContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT {

    private static final Long NONEXISTENT_USER_ID = 999L;
    private static final String NEW_NICK = "newNick";
    private static final String NEW_EMAIL = "newEmail@test.com";
    private static final String NEW_PASSWORD = "newPassword";
    private static final String WRONG_EMAIL = "wrong@email.com";

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TestEntityManager entityManager;

    private User user;

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
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void whenCorrectUserDataProvided_SaveUser() {
        User savedUser = userRepository.save(user);
        assertEquals(entityManager.find(User.class, savedUser.getId()), user);
    }

    @Test
    void whenEmailExists_ReturnUser() {
        Optional<User> foundUser = userRepository.findByEmail(user.getEmail());

        assertThat(foundUser).isPresent();
        assertEquals(user, foundUser.get());
    }

    @Test
    void whenEmailDoesNotExist_ReturnEmpty() {
        Optional<User> notFoundUser = userRepository.findByEmail(WRONG_EMAIL);
        assertThat(notFoundUser).isNotPresent();
    }

    @Test
    void whenIdExists_ReturnUser() {
        Optional<User> foundUser = userRepository.findById(user.getId());

        assertThat(foundUser).isPresent();
        assertEquals(user, foundUser.get());
    }

    @Test
    void whenIdDoesNotExist_ReturnEmpty() {
        Optional<User> notFoundUser = userRepository.findById(NONEXISTENT_USER_ID);
        assertThat(notFoundUser).isNotPresent();
    }

    @Test
    void whenEmailExists_UpdateEnabledAndReturnPositive() {
        user.setEnabled(false);

        int updatedCount = userRepository.updateEnabled(user.getEmail());
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertTrue(user.isEnabled());
    }

    @Test
    void whenEmailExistsAndISActivated_NoUpdateAndReturnZero() {
        user.setEnabled(true);

        int updatedCount = userRepository.updateEnabled(user.getEmail());
        entityManager.refresh(user);

        assertThat(updatedCount).isZero();
        assertTrue(user.isEnabled());
    }

    @Test
    void whenEmailDoesNotExist_NoUpdateAndReturnZero() {
        int updatedCount = userRepository.updateEnabled(WRONG_EMAIL);
        assertThat(updatedCount).isZero();
    }

    @Test
    void whenUserExists_UpdateNickAndReturnPositive() {
        int updatedCount = userRepository.updateNick(user.getId(), NEW_NICK);
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertEquals(NEW_NICK, user.getNick());
    }

    @Test
    void whenUserDoesNotExist_NoUpdateNickAndReturnZero() {
        int updatedCount = userRepository.updateNick(NONEXISTENT_USER_ID, NEW_NICK);
        assertThat(updatedCount).isZero();
    }

    @Test
    void whenUserExists_UpdateEmailAndDisableUserAndReturnPositive() {
        int updatedCount = userRepository.updateEmailAndDisableUser(user.getId(), NEW_EMAIL);
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertEquals(NEW_EMAIL, user.getEmail());
        assertFalse(user.isEnabled());
    }

    @Test
    void whenUserDoesNotExist_NoUpdateEmailAndReturnZero() {
        int updatedCount = userRepository.updateEmailAndDisableUser(NONEXISTENT_USER_ID, NEW_EMAIL);
        assertThat(updatedCount).isZero();
    }

    @Test
    void whenUserExists_UpdatePasswordAndReturnPositive() {
        int updatedCount = userRepository.updatePassword(user.getId(), NEW_PASSWORD);
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertEquals(NEW_PASSWORD, user.getPassword());
    }

    @Test
    void whenUserDoesNotExist_NoUpdatePasswordAndReturnZero() {
        int updatedCount = userRepository.updatePassword(NONEXISTENT_USER_ID, NEW_PASSWORD);
        assertThat(updatedCount).isZero();
    }

}
