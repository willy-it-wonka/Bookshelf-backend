package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.book.note.NoteService;
import com.mybooks.bookshelfSB.book.payload.CreateBookRequest;
import com.mybooks.bookshelfSB.book.payload.UpdateBookRequest;
import com.mybooks.bookshelfSB.exception.BookNotFoundException;
import com.mybooks.bookshelfSB.exception.NoteNotFoundException;
import com.mybooks.bookshelfSB.exception.UnauthorizedAccessException;
import com.mybooks.bookshelfSB.user.User;
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
    List<Book> getAllUserBooks(UserDetails userDetails) {
        return bookRepository.findByBookOwner((User) userDetails);
    }

    // Get the book with specified id. If id doesn't exist, throw an exception.
    Book getUserBookById(Long id, UserDetails userDetails) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new BookNotFoundException(id));

        // Checks if the logged-in user is the owner of the book with the specified id.
        if (!book.getBookOwner().getId().equals(((User) userDetails).getId()))
            throw new UnauthorizedAccessException();

        return book;
    }

    // Get a list of books by status.
    List<Book> getUserBooksByStatus(String status, UserDetails userDetails) {
        BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase()); // Without this only address .../status/READ will be ok, .../status/read will not.
        return bookRepository.findByStatusAndBookOwner(bookStatus, (User) userDetails);
    }

    // Add the book to the database.
    Book createBook(CreateBookRequest request, UserDetails userDetails) {
        Book book = new Book(request.title(), request.author(), request.status(), request.linkToCover(), (User) userDetails);
        return bookRepository.save(book);
    }

    // Get the book by id and modify it.
    Book updateBook(Long id, UpdateBookRequest request, UserDetails userDetails) {
        Book bookToUpdate = getUserBookById(id, userDetails);
        bookToUpdate.setTitle(request.title());
        bookToUpdate.setAuthor(request.author());
        bookToUpdate.setStatus(request.status());
        bookToUpdate.setLinkToCover(request.linkToCover());
        return bookRepository.save(bookToUpdate);
    }

    // Delete the book with the specified id.
    @Transactional
    void deleteBookById(Long id) {
        // First, delete the notes for this book.
        try {
            noteService.deleteNoteByBookId(id);
        } catch (NoteNotFoundException ignored) {}

        bookRepository.deleteById(id);
    }

}
