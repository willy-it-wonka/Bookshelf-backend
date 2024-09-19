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
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIT {

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
        assertEquals(foundUser.get(), user);
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
        assertEquals(foundUser.get(), user);
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

        assertThat(updatedCount).isPositive();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void whenEmailExistsAndISActivated_NoUpdateAndReturnZero() {
        user.setEnabled(true);
        entityManager.persist(user);

        int updatedCount = userRepository.updateEnabled(user.getEmail());
        entityManager.refresh(user);

        assertThat(updatedCount).isZero();
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void whenEmailDoesNotExist_NoUpdateAndReturnZero() {
        String nonExistentEmail = "nonexistent@test.com";
        int updatedCount = userRepository.updateEnabled(nonExistentEmail);
        assertThat(updatedCount).isZero();
    }

    @Test
    void whenUserExists_UpdateNickAndReturnPositive() {
        String newNick = "newNick";
        entityManager.persist(user);

        int updatedCount = userRepository.updateNick(user.getId(), newNick);
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertEquals(user.getNick(), newNick);
    }

    @Test
    void whenUserDoesNotExist_NoUpdateNickAndReturnZero() {
        Long nonExistentUserId = 999L;
        int updatedCount = userRepository.updateNick(nonExistentUserId, "nick");

        assertThat(updatedCount).isZero();
    }

    @Test
    void whenUserExists_UpdateEmailAndDisableUserAndReturnPositive() {
        String newEmail = "newEmail@test.com";
        entityManager.persist(user);

        int updatedCount = userRepository.updateEmailAndDisableUser(user.getId(), newEmail);
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertEquals(user.getEmail(), newEmail);
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void whenUserDoesNotExist_NoUpdateEmailAndReturnZero() {
        Long nonExistentUserId = 999L;
        int updatedCount = userRepository.updateEmailAndDisableUser(nonExistentUserId, "email@test.com");

        assertThat(updatedCount).isZero();
    }

    @Test
    void whenUserExists_UpdatePasswordAndReturnPositive() {
        String newPassword = "newPassword";
        entityManager.persist(user);

        int updatedCount = userRepository.updatePassword(user.getId(), newPassword);
        entityManager.refresh(user);

        assertThat(updatedCount).isPositive();
        assertEquals(user.getPassword(), newPassword);
    }

    @Test
    void whenUserDoesNotExist_NoUpdatePasswordAndReturnZero() {
        Long nonExistentUserId = 999L;
        int updatedCount = userRepository.updatePassword(nonExistentUserId, "newPassword");

        assertThat(updatedCount).isZero();
    }

}
