package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.note.InMemoryNoteRepository;
import com.mybooks.bookshelf.book.note.Note;
import com.mybooks.bookshelf.book.note.NoteService;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.book.payload.UpdateBookRequest;
import com.mybooks.bookshelf.exception.BookNotFoundException;
import com.mybooks.bookshelf.exception.UnauthorizedAccessException;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private InMemoryBookRepository bookRepository;
    private InMemoryNoteRepository noteRepository;
    private BookService bookService;
    private User user;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        noteRepository = new InMemoryNoteRepository();
        NoteService noteService = new NoteService(noteRepository);
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
        expectedBook.setId(1L);

        Book foundBook = bookService.getUserBookById(expectedBook.getId(), user);

        assertNotNull(foundBook);
        assertEquals(expectedBook, foundBook);
    }

    @Test
    void whenBookWithGivenIdDoesNotExist_ThrowResourceNotFoundException() {
        Long idOfNonExistentBook = 999L;
        BookNotFoundException e = assertThrows(BookNotFoundException.class, () ->
                bookService.getUserBookById(idOfNonExistentBook, user));
        assertEquals("Book with ID: 999 doesn't exist.", e.getMessage());
    }

    @Test
    void whenUserIsNotOwnerOfBookWithGivenId_ThrowUnauthorizedAccessException() {
        Long idOfUserFirstBook = 1L; // See setUp()
        User anotherUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        anotherUser.setId(2L);

        UnauthorizedAccessException e = assertThrows(UnauthorizedAccessException.class, () ->
                bookService.getUserBookById(idOfUserFirstBook, anotherUser));

        assertEquals("You don't have authorization.", e.getMessage());
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
    void whenCreateBookRequestWithCategories_ReturnSavedBook() {
        Set<BookCategory> categories = new HashSet<>();
        categories.add(BookCategory.IT);
        CreateBookRequest request = new CreateBookRequest("Title 3", "Author 3", BookStatus.WAITING, "link", categories);

        Book savedBook = bookService.createBook(request, user);

        assertEquals(user, savedBook.getBookOwner());
        assertEquals("Title 3", savedBook.getTitle());
        assertEquals("Author 3", savedBook.getAuthor());
        assertEquals(BookStatus.WAITING, savedBook.getStatus());
        assertEquals("link", savedBook.getLinkToCover());
        assertEquals(categories, savedBook.getCategories());
    }

    @Test
    void whenCreateBookRequestWithEmptyCategories_ReturnSavedBook() {
        CreateBookRequest request = new CreateBookRequest("Title 4", "Author 4", BookStatus.WAITING, "link", new HashSet<>());

        Book savedBook = bookService.createBook(request, user);

        assertEquals(user, savedBook.getBookOwner());
        assertEquals("Title 4", savedBook.getTitle());
        assertEquals("Author 4", savedBook.getAuthor());
        assertEquals(BookStatus.WAITING, savedBook.getStatus());
        assertEquals("link", savedBook.getLinkToCover());
        assertTrue(savedBook.getCategories().isEmpty());
    }

    @Test
    void whenUpdateBookRequest_ReturnUpdatedBook() {
        Set<BookCategory> categories = new HashSet<>();
        categories.add(BookCategory.IT);
        UpdateBookRequest request = new UpdateBookRequest("Title 11", "Author 11", BookStatus.READ, "newLink", categories);

        Book updatedBook = bookService.updateBook(1L, request, user);

        assertEquals("Title 11", updatedBook.getTitle());
        assertEquals("Author 11", updatedBook.getAuthor());
        assertEquals(BookStatus.READ, updatedBook.getStatus());
        assertEquals("newLink", updatedBook.getLinkToCover());
        assertEquals(categories, updatedBook.getCategories());
        assertEquals(user, updatedBook.getBookOwner());
    }

    @Test
    void whenTriesUpdateBookThatUserIsNotOwner_ThrowUnauthorizedAccessException() {
        User anotherUser = new User("Bob", "bob@example.com", "111", UserRole.USER);
        anotherUser.setId(2L);
        UpdateBookRequest request = new UpdateBookRequest("Title 11", "Author 11", BookStatus.READ, "link", new HashSet<>());

        assertNotEquals(user, anotherUser);
        assertThrows(UnauthorizedAccessException.class, () -> bookService.updateBook(1L, request, anotherUser));
    }

    @Test
    void whenTriesUpdateBookThatDoesNotExist_ThrowBookNotFoundException() {
        UpdateBookRequest request = new UpdateBookRequest("Title 11", "Author 11", BookStatus.READ, "link", new HashSet<>());
        assertThrows(BookNotFoundException.class, () -> bookService.updateBook(999L, request, user));
    }

    @Test
    void whenBookHasNoNotes_RemoveBook() {
        Long bookId = 1L;
        assertTrue(bookRepository.findById(bookId).isPresent(), "Book should exist before deletion.");

        bookService.deleteBookById(bookId);

        assertFalse(bookRepository.findById(bookId).isPresent());
    }

    @Test
    void whenBookHasNotes_RemoveBookAndNotes() {
        Book book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(10L);
        bookRepository.save(book);
        Note note = new Note("Note content", book);
        noteRepository.save(note);

        assertTrue(bookRepository.findById(book.getId()).isPresent(), "Book should exist before deletion.");
        assertTrue(noteRepository.findByBookId(note.getBook().getId()).isPresent(), "Note should exist before deletion.");

        bookService.deleteBookById(book.getId());

        assertFalse(bookRepository.findById(book.getId()).isPresent());
        assertFalse(noteRepository.findByBookId(note.getBook().getId()).isPresent());
    }

}
