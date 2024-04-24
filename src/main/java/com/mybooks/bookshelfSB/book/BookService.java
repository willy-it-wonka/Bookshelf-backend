package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.exception.ResourceNotFoundException;
import com.mybooks.bookshelfSB.exception.UnauthorizedAccessException;
import com.mybooks.bookshelfSB.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    // Get the list of user's books.
    List<Book> getAllBooks(UserDetails userDetails) {
        return bookRepository.findByBookOwner((User) userDetails);
    }

    // Get the book with specified id. If id doesn't exist, throw an exception.
    Book getBookById(Long id, UserDetails userDetails) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));

        // Checks if the logged-in user is the owner of the book with the specified id.
        if (!book.getBookOwner().getId().equals(((User) userDetails).getId()))
            throw new UnauthorizedAccessException();

        return book;
    }

    // Get a list of books by status.
    List<Book> getBookByStatus(String status, UserDetails userDetails) {
        BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase()); // Without this only address .../status/READ will be ok, .../status/read will not.
        return bookRepository.findByStatusAndBookOwner(bookStatus, (User) userDetails);
    }

    // Add the book to the database.
    Book createBook(Book book, UserDetails userDetails) {
        book.setBookOwner((User) userDetails);
        return bookRepository.save(book);
    }

    // Get the book by id and modify it.
    Book updateBook(Long id, Book book, UserDetails userDetails) {
        Book bookToUpdate = getBookById(id, userDetails);
        bookToUpdate.setTitle(book.getTitle());
        bookToUpdate.setAuthor(book.getAuthor());
        bookToUpdate.setStatus(book.getStatus());
        bookToUpdate.setLinkToCover(book.getLinkToCover());
        return bookRepository.save(bookToUpdate);
    }

    // Delete the book with the specified id.
    void deleteBookById(Long id, UserDetails userDetails) {
        Book book = getBookById(id, userDetails);
        bookRepository.delete(book);
    }

}