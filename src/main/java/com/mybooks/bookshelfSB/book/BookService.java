package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // Get the book with specified id. If id doesn't exist, throw an exception.
    public Book getBookById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
    }

    // Get a list of books by status.
    public List<Book> getBookByStatus(String status) {
        BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase()); // Without this only address .../status/READ will be ok, .../status/read will not.
        return bookRepository.findByStatus(bookStatus);
    }

    // Add the book to the list.
    public Book createBook(Book book) {
        return bookRepository.save(book);
    }

    // Get the book by id and modify it.
    public ResponseEntity<Book> updateBook(Long id, Book book) {
        Book bookToUpdate = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        bookToUpdate.setTitle(book.getTitle());
        bookToUpdate.setAuthor(book.getAuthor());
        bookToUpdate.setStatus(book.getStatus());
        bookToUpdate.setLinkToCover(book.getLinkToCover());
        Book updatedBook = bookRepository.save(bookToUpdate);
        return ResponseEntity.ok(updatedBook);
    }

    // Delete the book with the specified id.
    public void deleteBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        bookRepository.delete(book);
    }
}
