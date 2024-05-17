package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.user.User;
import com.mybooks.bookshelfSB.user.UserRole;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class BookRepositoryIT {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private EntityManager entityManager;

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void givenNewBook_whenSave_thenSuccess() {
        User user = new User("Username", "email@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        Book book = new Book("Title", "Author", BookStatus.WAITING, user);

        Book savedBook = bookRepository.save(book);

        Assertions.assertThat(entityManager.find(Book.class, savedBook.getId())).isEqualTo(book);
    }

    @Test
    void givenBookCreated_whenUpdate_thenSuccess() {
        User user = new User("Username", "email@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        Book book = new Book("Title", "Author", BookStatus.WAITING, user);
        entityManager.persist(book);
        String newTitle = "NewTitle";

        book.setTitle(newTitle);
        bookRepository.save(book);

        Assertions.assertThat(entityManager.find(Book.class, book.getId()).getTitle()).isEqualTo(newTitle);
    }

    @Test
    void givenBookCreated_whenFindById_thenSuccess() {
        User user = new User("Username", "email@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        Book book = new Book("Title", "Author", BookStatus.WAITING, user);
        entityManager.persist(book);

        Optional<Book> retrievedBook = bookRepository.findById(book.getId());

        Assertions.assertThat(retrievedBook).contains(book);
    }

    @Test
    void givenBookCreated_whenDelete_thenSuccess() {
        User user = new User("Username", "email@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        Book book = new Book("Title", "Author", BookStatus.WAITING, user);
        entityManager.persist(book);

        bookRepository.delete(book);

        Assertions.assertThat(entityManager.find(Book.class, book.getId())).isNull();
    }

    @Test
    void givenBooksOwnedByUser_whenFindByBookOwner_thenSuccess() {
        User user = new User("Username", "email@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        Book book1 = new Book("Title1", "Author1", BookStatus.WAITING, user);
        Book book2 = new Book("Title2", "Author2", BookStatus.WAITING, user);
        Book book3 = new Book("Title3", "Author3", BookStatus.WAITING, user);
        bookRepository.saveAll(List.of(book1, book2, book3));

        List<Book> booksByOwner = bookRepository.findByBookOwner(user);

        Assertions.assertThat(booksByOwner).containsExactlyInAnyOrder(book1, book2, book3);
    }

    @Test
    void givenBooksByStatusAndOwner_whenFindByStatusAndBookOwner_thenSuccess() {
        User user = new User("Username", "email@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        Book book1 = new Book("Title1", "Author1", BookStatus.READ, user);
        Book book2 = new Book("Title2", "Author2", BookStatus.WAITING, user);
        Book book3 = new Book("Title3", "Author3", BookStatus.WAITING, user);
        Book book4 = new Book("Title4", "Author4", BookStatus.WAITING, user);
        bookRepository.saveAll(List.of(book1, book2, book3, book4));

        List<Book> books = bookRepository.findByStatusAndBookOwner(BookStatus.WAITING, user);

        Assertions.assertThat(books).containsExactlyInAnyOrder(book2, book3, book4);
    }

}
