package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryIT {

    @Container
    public static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testDB")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Book book;
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
        entityManager.persist(user);
        book = new Book("Title", "Author", BookStatus.WAITING, "link", user);
    }

    @AfterEach
    void tearDown() {
        bookRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void whenCorrectBookDataProvided_SaveBook() {
        Book savedBook = bookRepository.save(book);
        assertThat(entityManager.find(Book.class, savedBook.getId())).isEqualTo(book);
    }

    @Test
    void whenBookFoundById_ReturnBook() {
        entityManager.persist(book);
        Optional<Book> retrievedBook = bookRepository.findById(book.getId());
        assertThat(retrievedBook).contains(book);
    }

    @Test
    void whenValidDeletion_RemoveBookFromDB() {
        entityManager.persist(book);
        bookRepository.delete(book);
        assertThat(entityManager.find(Book.class, book.getId())).isNull();
    }

    @Test
    void whenBooksFoundByOwner_ReturnOwnerBooks() {
        Book book1 = new Book("Title1", "Author1", BookStatus.WAITING, "link", user);
        Book book2 = new Book("Title2", "Author2", BookStatus.WAITING, "link", user);
        Book book3 = new Book("Title3", "Author3", BookStatus.WAITING, "link", user);
        bookRepository.saveAll(List.of(book, book1, book2, book3));

        List<Book> booksByOwner = bookRepository.findByBookOwner(user);

        assertThat(booksByOwner).containsExactlyInAnyOrder(book, book1, book2, book3);
    }

    @Test
    void whenBooksFoundByStatusAndOwner_ReturnOwnerBooksByStatus() {
        Book book1 = new Book("Title1", "Author1", BookStatus.READ, "link", user);
        Book book2 = new Book("Title2", "Author2", BookStatus.READ, "link", user);
        Book book3 = new Book("Title3", "Author3", BookStatus.WAITING, "link", user);
        Book book4 = new Book("Title4", "Author4", BookStatus.WAITING, "link", user);
        bookRepository.saveAll(List.of(book, book1, book2, book3, book4));

        List<Book> books = bookRepository.findByStatusAndBookOwner(BookStatus.READ, user);

        assertThat(books).containsExactlyInAnyOrder(book1, book2);
    }

}
