package com.mybooks.bookshelfSB.book;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Makes this class a REST controller: will handle HTTP requests.
@RestController
@RequestMapping("/api")
@CrossOrigin // Later remove or change.
public class BookController {

    private final BookService bookService;

    // Automatically injects an object.
    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public List<Book> getAllBooks() {
        return bookService.getAllBooks();
    }

    @GetMapping("/books/{id}")
    public ResponseEntity<Book> getBookById(@PathVariable Long id) {  // @PathVariable - get id from url
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(book);
    }

    @GetMapping("/books/status/{status}") // In case: "/books/{status}" - would lead to a conflict with "/books/{id}".
    public List<Book> getBookByStatus(@PathVariable String status) {
        return bookService.getBookByStatus(status);
    }

    @PostMapping("/books")
    public Book createBook(@RequestBody Book book) {  // @RequestBody - Spring automatically deserializes the JSON into a Java type.
        return bookService.createBook(book);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable Long id, @RequestBody Book book) {
        return bookService.updateBook(id, book);
    }

    @DeleteMapping("/books/{id}")
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }
}
