package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.note.NoteService;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.book.payload.UpdateBookRequest;
import com.mybooks.bookshelf.exception.BookNotFoundException;
import com.mybooks.bookshelf.exception.NoteNotFoundException;
import com.mybooks.bookshelf.exception.UnauthorizedAccessException;
import com.mybooks.bookshelf.user.User;
import org.hibernate.Hibernate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final NoteService noteService;

    public BookService(BookRepository bookRepository, NoteService noteService) {
        this.bookRepository = bookRepository;
        this.noteService = noteService;
    }

    // Get the list of user's books.
    @Transactional(readOnly = true)
    public List<Book> getAllUserBooks(UserDetails userDetails) {
        List<Book> books = bookRepository.findByBookOwner((User) userDetails);

        // Solution for lazy collection initialization.
        books.forEach(book -> Hibernate.initialize(book.getCategories()));

        return books;
    }

    // Get user's book by id.
    @Transactional(readOnly = true)
    public Book getUserBookById(Long id, UserDetails userDetails) {
        return findUserBookById(id, userDetails);
    }

    // Get the list of user's books by status.
    @Transactional(readOnly = true)
    public List<Book> getUserBooksByStatus(BookStatus status, UserDetails userDetails) {
        List<Book> books = bookRepository.findByStatusAndBookOwner(status, (User) userDetails);
        books.forEach(book -> Hibernate.initialize(book.getCategories()));
        return books;
    }

    // Add new book to the database.
    @Transactional
    public Book createBook(CreateBookRequest request, UserDetails userDetails) {
        Book book = new Book(request.title(), request.author(), request.status(), request.linkToCover(), (User) userDetails);
        book.setCategories(request.categories());
        return bookRepository.save(book);
    }

    // Get the book by id and modify it.
    @Transactional
    public Book updateBook(Long id, UpdateBookRequest request, UserDetails userDetails) {
        Book bookToUpdate = findUserBookById(id, userDetails);
        bookToUpdate.setTitle(request.title());
        bookToUpdate.setAuthor(request.author());
        bookToUpdate.setStatus(request.status());
        bookToUpdate.setLinkToCover(request.linkToCover());
        bookToUpdate.setCategories(request.categories());
        return bookRepository.save(bookToUpdate);
    }

    // Delete the book with the specified id.
    @Transactional
    public void deleteBookById(Long id) {
        // First, delete the notes for this book.
        try {
            noteService.deleteNoteByBookId(id);
        } catch (NoteNotFoundException ignored) {
            // If the notes do not exist, it will not disrupt the application,
            // and the book will be deleted anyway.
        }

        bookRepository.deleteById(id);
    }

    /*
     * Find the book with the specified id. If id doesn't exist, throw an exception.
     *
     * Logic used in more than one place in BookService, in @Transactional methods.
     * To allow it to be called, it must be in a non-transactional method. */
    private Book findUserBookById(Long id, UserDetails userDetails) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
        Hibernate.initialize(book.getCategories());

        // Checks if the logged-in user is the owner of the book with the specified id.
        if (!book.getBookOwner().getId().equals(((User) userDetails).getId()))
            throw new UnauthorizedAccessException();

        return book;
    }

}
