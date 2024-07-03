package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.book.note.NoteService;
import com.mybooks.bookshelfSB.book.payload.CreateBookRequest;
import com.mybooks.bookshelfSB.book.payload.UpdateBookRequest;
import com.mybooks.bookshelfSB.exception.BookNotFoundException;
import com.mybooks.bookshelfSB.exception.UnauthorizedAccessException;
import com.mybooks.bookshelfSB.user.User;
import com.mybooks.bookshelfSB.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BookServiceTest {

    private InMemoryBookRepository bookRepository;
    private BookService bookService;
    private NoteService noteService;
    private User user;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        noteService = mock(NoteService.class); // TODO: Will be replaced when creating tests for NoteService.
        bookService = new BookService(bookRepository, noteService);
        user = new User("Tom", "tom@example.com", "123", UserRole.USER);
        user.setId(1L);
        bookRepository.save(new Book("Title 1", "Author 1", BookStatus.WAITING, "link", user));
        bookRepository.save(new Book("Title 2", "Author 2", BookStatus.READ, "link", user));
    }

    @AfterEach
    void tearDown() {
        bookRepository.clear();
    }

    @Test
    void whenBooksExist_ReturnListOfUserBooks() {
        List<Book> books = bookService.getAllUserBooks(user);
        assertEquals(2, books.size());
    }

    @Test
    void whenNoBooks_ReturnEmptyList() {
        User sadUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        List<Book> books = bookService.getAllUserBooks(sadUser);
        assertTrue(books.isEmpty());
    }

    @Test
    void whenBookWithGivenIdExists_ReturnBook() {
        Book expectedBook = new Book("Title 1", "Author 1", BookStatus.WAITING, "link", user);
        Long expectedBookId = 1L;

        Book foundBook = bookService.getUserBookById(expectedBookId, user);

        assertNotNull(foundBook);
        assertEquals(expectedBookId, foundBook.getId());
        assertEquals(expectedBook.getTitle(), foundBook.getTitle());
    }

    @Test
    void whenBookWithGivenIdDoesNotExist_ThrowResourceNotFoundException() {
        Long idOfNonExistentBook = 999L;
        BookNotFoundException thrown = assertThrows(BookNotFoundException.class, () ->
                bookService.getUserBookById(idOfNonExistentBook, user));
        assertEquals("Book with ID: 999 doesn't exist.", thrown.getMessage());
    }

    @Test
    void whenUserIsNotOwnerOfBookWithGivenId_ThrowUnauthorizedAccessException() {
        Long idOfUserFirstBook = 1L; // See setUp()
        User anotherUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        anotherUser.setId(2L);

        UnauthorizedAccessException thrown = assertThrows(UnauthorizedAccessException.class, () ->
                bookService.getUserBookById(idOfUserFirstBook, anotherUser));

        assertEquals("You don't have authorization.", thrown.getMessage());
    }

    @Test
    void whenValidBookStatus_ReturnListOfMatchingBooks() {
        List<Book> booksRead = bookService.getUserBooksByStatus(BookStatus.READ, user);

        assertEquals(1, booksRead.size());
        assertTrue(booksRead.stream().allMatch(book -> book.getStatus() == BookStatus.READ));
    }

    @Test
    void whenNoBooksFoundByStatus_ReturnEmptyList() {
        List<Book> booksReading = bookService.getUserBooksByStatus(BookStatus.READING, user);
        assertTrue(booksReading.isEmpty());
    }

    @Test
    void whenCorrectBookDataProvided_SaveBook() {
        CreateBookRequest request = new CreateBookRequest("Title 3", "Author 3", BookStatus.WAITING, "link");

        Book savedBook = bookService.createBook(request, user);

        assertEquals(user, savedBook.getBookOwner());
        assertEquals(1L, user.getId());
        assertEquals("Title 3", savedBook.getTitle());
    }

    @Test
    void whenValidUpdate_UpdateBookDetails() {
        UpdateBookRequest request = new UpdateBookRequest("Title 11", "Author 11", BookStatus.READ, "newLink");

        Book updatedBook = bookService.updateBook(1L, request, user);

        assertEquals("Title 11", updatedBook.getTitle());
        assertEquals("Author 11", updatedBook.getAuthor());
        assertEquals(BookStatus.READ, updatedBook.getStatus());
        assertEquals("newLink", updatedBook.getLinkToCover());
    }

    @Test
    void whenTriesUpdateBookThatUserIsNotOwner_ThrowUnauthorizedAccessException() {
        User anotherUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        anotherUser.setId(2L);
        UpdateBookRequest request = new UpdateBookRequest("Title 11", "Author 11", BookStatus.READ, "link");

        assertThrows(UnauthorizedAccessException.class, () ->
                bookService.updateBook(1L, request, anotherUser));
    }

    @Test
    void whenTriesUpdateBookThatDoesNotExist_ThrowResourceNotFoundException() {
        UpdateBookRequest request = new UpdateBookRequest("Title 11", "Author 11", BookStatus.READ, "link");
        assertThrows(BookNotFoundException.class, () ->
                bookService.updateBook(999L, request, user));
    }

    @Test
    void whenValidDeletion_RemoveBookFromRepository() {
        Long bookId = 1L;
        bookService.deleteBookById(bookId);
        assertFalse(bookRepository.findById(bookId).isPresent());
    }

}
