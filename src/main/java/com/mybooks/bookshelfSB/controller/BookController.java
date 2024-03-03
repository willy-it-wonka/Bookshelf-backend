package com.mybooks.bookshelfSB.controller;

import com.mybooks.bookshelfSB.exception.ResourceNotFoundException;
import com.mybooks.bookshelfSB.model.Book;
import com.mybooks.bookshelfSB.model.BookStatus;
import com.mybooks.bookshelfSB.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Makes this class a REST controller: will handle HTTP requests.
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200")  //Later remove or change adnotation.
public class BookController {

    private final BookRepository bookRepository;

    //Automatically injects an object.
    @Autowired
    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    //GET book with specified id. If id doesn't exist, throw an exception.
    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {  //@PathVariable - get id from url
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        return ResponseEntity.ok(book);
    }

    //GET a list of books by status.
    @GetMapping("/books/status/{status}") //In case: "/books/{status}" - would lead to a conflict with "/books/{id}".
    public List<Book> getBookByStatus(@PathVariable String status) {
        BookStatus bookStatus = BookStatus.valueOf(status.toUpperCase()); //Without this only address .../status/READ will be ok, .../status/read will not.
        return bookRepository.findByStatus(bookStatus);
    }

    @PostMapping("/books")
    public Book createBook(@RequestBody Book book) {  //@RequestBody - Spring automatically deserializes the JSON into a Java type.
        return bookRepository.save(book);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        Book bookToUpdate = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        bookToUpdate.setTitle(book.getTitle());
        bookToUpdate.setAuthor(book.getAuthor());
        bookToUpdate.setStatus(book.getStatus());
        Book updatedBook = bookRepository.save(bookToUpdate);
        return ResponseEntity.ok(updatedBook);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Map<String, Boolean>> deleteBookById(@PathVariable Long id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(id));
        bookRepository.delete(book);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);
        return ResponseEntity.ok(response);
    }
}
