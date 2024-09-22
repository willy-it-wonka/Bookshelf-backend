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

    public static final Long BOOK_ID = 1L;
    public static final Long NONEXISTENT_BOOK_ID = 999L;
    public static final String BOOK_NOT_FOUND_ERROR = "Book with ID: 999 doesn't exist.";
    public static final String AUTHORIZATION_ERROR = "You don't have authorization.";

    private InMemoryBookRepository bookRepository;
    private InMemoryNoteRepository noteRepository;
    private BookService bookService;
    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        bookRepository = new InMemoryBookRepository();
        noteRepository = new InMemoryNoteRepository();
        NoteService noteService = new NoteService(noteRepository);
        bookService = new BookService(bookRepository, noteService);

        user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        user.setId(BOOK_ID);
        book = new Book("Title 1", "Author 1", BookStatus.WAITING, "link 1", user);
        bookRepository.save(book);
        bookRepository.save(new Book("Title 2", "Author 2", BookStatus.READ, "link 2", user));
    }

    @AfterEach
    void tearDown() {
        bookRepository.clear();
    }

    @Test
    void whenBooksExist_ReturnListOfAllUserBooks() {
        List<Book> books = bookService.getAllUserBooks(user);
        assertEquals(2, books.size());
    }

    @Test
    void whenNoBooks_ReturnEmptyList() {
        User sadUser = new User("Bob", "bob@test.com", "123", UserRole.USER);
        List<Book> books = bookService.getAllUserBooks(sadUser);
        assertTrue(books.isEmpty());
    }

    @Test
    void whenBookWithGivenIdExists_ReturnBook() {
        Book foundBook = bookService.getUserBookById(BOOK_ID, user);

        assertNotNull(foundBook);
        assertEquals(book, foundBook);
    }

    @Test
    void whenBookWithGivenIdDoesNotExist_ThrowBookNotFoundException() {
        BookNotFoundException e = assertThrows(BookNotFoundException.class, () ->
                bookService.getUserBookById(NONEXISTENT_BOOK_ID, user));
        assertEquals(BOOK_NOT_FOUND_ERROR, e.getMessage());
    }

    @Test
    void whenUserIsNotOwnerOfBookWithGivenId_ThrowUnauthorizedAccessException() {
        User anotherUser = new User("Bob", "bob@test.com", "123", UserRole.USER);
        UnauthorizedAccessException e = assertThrows(UnauthorizedAccessException.class, () ->
                bookService.getUserBookById(BOOK_ID, anotherUser));
        assertEquals(AUTHORIZATION_ERROR, e.getMessage());
    }

    @Test
    void whenBooksExistByStatus_ReturnListOfBooks() {
        List<Book> books = bookService.getUserBooksByStatus(BookStatus.READ, user);

        assertEquals(1, books.size());
        assertTrue(books.stream().allMatch(b -> b.getStatus() == BookStatus.READ));
    }

    @Test
    void whenNoBooksFoundByStatus_ReturnEmptyList() {
        List<Book> books = bookService.getUserBooksByStatus(BookStatus.READING, user);
        assertTrue(books.isEmpty());
    }

    @Test
    void whenCreateBookRequestWithCategories_ReturnSavedBook() {
        Set<BookCategory> categories = new HashSet<>();
        categories.add(BookCategory.IT);
        CreateBookRequest request = new CreateBookRequest("Title 3", "Author 3", BookStatus.WAITING, "link 3", categories);

        Book savedBook = bookService.createBook(request, user);

        assertEquals(user, savedBook.getBookOwner());
        assertEquals("Title 3", savedBook.getTitle());
        assertEquals("Author 3", savedBook.getAuthor());
        assertEquals(BookStatus.WAITING, savedBook.getStatus());
        assertEquals("link 3", savedBook.getLinkToCover());
        assertEquals(categories, savedBook.getCategories());
    }

    @Test
    void whenCreateBookRequestWithEmptyCategories_ReturnSavedBook() {
        CreateBookRequest request = new CreateBookRequest("Title 4", "Author 4", BookStatus.WAITING, "link 4", new HashSet<>());

        Book savedBook = bookService.createBook(request, user);

        assertEquals(user, savedBook.getBookOwner());
        assertEquals("Title 4", savedBook.getTitle());
        assertEquals("Author 4", savedBook.getAuthor());
        assertEquals(BookStatus.WAITING, savedBook.getStatus());
        assertEquals("link 4", savedBook.getLinkToCover());
        assertTrue(savedBook.getCategories().isEmpty());
    }

    @Test
    void whenUpdateBookRequest_ReturnUpdatedBook() {
        UpdateBookRequest request = new UpdateBookRequest("newTile", "Author 1", BookStatus.WAITING, "link 1", new HashSet<>());

        Book updatedBook = bookService.updateBook(BOOK_ID, request, user);

        assertEquals(request.title(), updatedBook.getTitle());
        assertEquals(book.getAuthor(), updatedBook.getAuthor());
        assertEquals(book.getStatus(), updatedBook.getStatus());
        assertEquals(book.getLinkToCover(), updatedBook.getLinkToCover());
        assertTrue(updatedBook.getCategories().isEmpty());
        assertEquals(user, updatedBook.getBookOwner());
    }

    @Test
    void whenTriesUpdateBookThatUserIsNotOwner_ThrowUnauthorizedAccessException() {
        User anotherUser = new User("Bob", "bob@test.com", "123", UserRole.USER);
        anotherUser.setId(2L);
        UpdateBookRequest request = new UpdateBookRequest("Title", "Author", BookStatus.READ, "link", new HashSet<>());

        UnauthorizedAccessException e = assertThrows(UnauthorizedAccessException.class, () ->
                bookService.updateBook(BOOK_ID, request, anotherUser));

        assertNotEquals(user, anotherUser);
        assertEquals(AUTHORIZATION_ERROR, e.getMessage());
    }

    @Test
    void whenTriesUpdateBookThatDoesNotExist_ThrowBookNotFoundException() {
        UpdateBookRequest request = new UpdateBookRequest("Title", "Author", BookStatus.READ, "link", new HashSet<>());
        BookNotFoundException e = assertThrows(BookNotFoundException.class, () ->
                bookService.updateBook(NONEXISTENT_BOOK_ID, request, user));
        assertEquals(BOOK_NOT_FOUND_ERROR, e.getMessage());
    }

    @Test
    void whenBookHasNoNotes_RemoveBook() {
        assertTrue(bookRepository.findById(BOOK_ID).isPresent());
        bookService.deleteBookById(BOOK_ID);
        assertFalse(bookRepository.findById(BOOK_ID).isPresent());
    }

    @Test
    void whenBookHasNotes_RemoveBookAndNotes() {
        Note note = new Note("Note content", book);
        noteRepository.save(note);
        assertTrue(bookRepository.findById(book.getId()).isPresent());
        assertTrue(noteRepository.findByBookId(note.getBook().getId()).isPresent());

        bookService.deleteBookById(book.getId());

        assertFalse(bookRepository.findById(book.getId()).isPresent());
        assertFalse(noteRepository.findByBookId(note.getBook().getId()).isPresent());
    }

}
