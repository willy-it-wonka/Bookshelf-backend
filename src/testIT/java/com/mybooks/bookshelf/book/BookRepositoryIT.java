package com.mybooks.bookshelf.book;

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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryIT {

    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Book book;
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
    void whenDeleteBookById_RemoveBook() {
        entityManager.persist(book);
        bookRepository.deleteById(book.getId());
        assertThat(entityManager.find(Book.class, book.getId())).isNull();
    }

    @Test
    void whenBookFoundByIdWithCategories_ReturnBookWithCategories() {
        book.getCategories().add(BookCategory.HISTORY);
        book.getCategories().add(BookCategory.BIOGRAPHY);
        entityManager.persist(book);

        Optional<Book> retrievedBook = bookRepository.findByIdWithCategories(book.getId());

        assertThat(retrievedBook).isPresent();
        assertThat(retrievedBook.get().getCategories()).hasSize(2);
    }

    @Test
    void whenBooksFoundByOwner_ReturnListOfAllUserBooks() {
        Book book2 = new Book("Title 2", "Author 2", BookStatus.WAITING, "link 2", user);
        Book book3 = new Book("Title 3", "Author 3", BookStatus.WAITING, "link 3", user);
        bookRepository.saveAll(List.of(book, book2, book3));

        List<Book> books = bookRepository.findByBookOwner(user);

        assertThat(books).containsExactlyInAnyOrder(book, book2, book3);
    }

    @Test
    void whenBooksFoundByStatusAndOwner_ReturnListOfBooks() {
        Book book2 = new Book("Title 2", "Author 2", BookStatus.WAITING, "link", user);
        Book book3 = new Book("Title 3", "Author 3", BookStatus.READ, "link", user);
        Book book4 = new Book("Title 4", "Author 4", BookStatus.READ, "link", user);
        bookRepository.saveAll(List.of(book, book2, book3, book4));

        List<Book> books = bookRepository.findByStatusAndBookOwner(BookStatus.READ, user);

        assertThat(books).containsExactlyInAnyOrder(book3, book4);
    }

    @Test
    void whenNoBooksWithGivenStatus_ReturnEmptyList() {
        List<Book> books = bookRepository.findByStatusAndBookOwner(BookStatus.READ, user);
        assertThat(books).isEmpty();
    }

}
