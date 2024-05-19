package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.exception.ResourceNotFoundException;
import com.mybooks.bookshelfSB.exception.UnauthorizedAccessException;
import com.mybooks.bookshelfSB.user.User;
import com.mybooks.bookshelfSB.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private InMemoryBookRepository bookRepository;
    private BookService bookService;
    private User user;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        bookService = new BookService(bookRepository);
        user = new User("Tom", "tom@example.com", "123", UserRole.USER);
        user.setId(1L);
        bookRepository.save(new Book("Title 1", "Author 1", BookStatus.WAITING, user));
        bookRepository.save(new Book("Title 2", "Author 2", BookStatus.READ, user));
    }

    @AfterEach
    void tearDown() {
        bookRepository.clear();
    }

    @Test
    void getAllBooks_BooksExist_ReturnsUserBooks() {
        List<Book> books = bookService.getAllBooks(user);
        assertEquals(2, books.size());
    }

    @Test
    void getAllBooks_NoBooks_ReturnsEmptyList() {
        User sadUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        List<Book> books = bookService.getAllBooks(sadUser);
        assertTrue(books.isEmpty());
    }

    @Test
    void getBookById_BookExists_ReturnsBook() {
        Book expectedBook = new Book("Title 1", "Author 1", BookStatus.WAITING, user);
        Long expectedBookId = 1L;

        Book foundBook = bookService.getBookById(expectedBookId, user);

        assertNotNull(foundBook);
        assertEquals(expectedBookId, foundBook.getId());
        assertEquals(expectedBook.getTitle(), foundBook.getTitle());
    }

    @Test
    void getBookById_BookDoesNotExist_ThrowsResourceNotFoundException() {
        Long idOfNonExistentBook = 999L;
        ResourceNotFoundException thrown = assertThrows(ResourceNotFoundException.class, () -> {
            bookService.getBookById(idOfNonExistentBook, user);
        });
        assertEquals("Book with ID: 999 doesn't exist.", thrown.getMessage());
    }

    @Test
    void getBookById_UserNotOwner_ThrowsUnauthorizedAccessException() {
        Long idOfUserFirstBook = 1L; // See setUp()
        User anotherUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        anotherUser.setId(2L);

        UnauthorizedAccessException thrown = assertThrows(UnauthorizedAccessException.class, () -> {
            bookService.getBookById(idOfUserFirstBook, anotherUser);
        });

        assertEquals("You don't have authorization.", thrown.getMessage());
    }

    @Test
    void getBookByStatus_WithValidStatus_ReturnsMatchingBooks() {
        List<Book> booksRead = bookService.getBookByStatus("READ", user);

        assertEquals(1, booksRead.size());
        assertTrue(booksRead.stream().allMatch(book -> book.getStatus() == BookStatus.READ));
    }

    @Test
    void getBookByStatus_NoBooksFound_ReturnsEmptyList() {
        List<Book> booksReading = bookService.getBookByStatus("READING", user);
        assertTrue(booksReading.isEmpty());
    }

    @Test
    void createBook_CorrectDataProvided_SavesBook() {
        Book newBook = new Book("Title 3", "Author 3", BookStatus.WAITING, null);

        Book savedBook = bookService.createBook(newBook, user);

        assertEquals(user, savedBook.getBookOwner());
        assertEquals(1L, user.getId());
        assertEquals("Title 3", savedBook.getTitle());
    }

    @Test
    void updateBook_ValidUpdate_UpdatesBookDetails() {
        Book updatedDetails = new Book("Title 11", "Author 11", BookStatus.READ, user);
        updatedDetails.setLinkToCover("new link");

        Book updatedBook = bookService.updateBook(1L, updatedDetails, user);

        assertEquals("Title 11", updatedBook.getTitle());
        assertEquals("Author 11", updatedBook.getAuthor());
        assertEquals(BookStatus.READ, updatedBook.getStatus());
        assertEquals("new link", updatedBook.getLinkToCover());
    }

    @Test
    void updateBook_UserNotOwner_ThrowsUnauthorizedAccessException() {
        User anotherUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        anotherUser.setId(2L);
        Book updatedDetails = new Book("Title 11", "Author 11", BookStatus.READ, user);

        assertThrows(UnauthorizedAccessException.class, () -> {
            bookService.updateBook(1L, updatedDetails, anotherUser);
        });
    }

    @Test
    void updateBook_BookDoesNotExist_ThrowsResourceNotFoundException() {
        Book updatedDetails = new Book("Title 11", "Author 11", BookStatus.READ, user);
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.updateBook(999L, updatedDetails, user);
        });
    }

    @Test
    void deleteBookById_ValidDeletion_RemovesBookFromInMemoryBookRepository() {
        Long bookId = 1L;
        bookService.deleteBookById(bookId, user);
        assertFalse(bookRepository.findById(bookId).isPresent());
    }

    @Test
    void deleteBookById_BookDoesNotExist_ThrowsResourceNotFoundException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            bookService.deleteBookById(999L, user);
        });
    }

}
