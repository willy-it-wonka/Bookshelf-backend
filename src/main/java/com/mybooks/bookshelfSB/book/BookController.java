package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.book.payload.CreateBookRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public List<Book> getAllUserBooks(@AuthenticationPrincipal UserDetails userDetails) { // @AuthenticationPrincipal - Spring Security provides a UserDetails object representing the logged-in user.
        return bookService.getAllUserBooks(userDetails);
    }

    @GetMapping("/books/{id}")
    public Book getUserBookById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) { // @PathVariable - get id from url
        return bookService.getUserBookById(id, userDetails);
    }

    @GetMapping("/books/status/{status}") // In case: "/books/{status}" - would lead to a conflict with "/books/{id}".
    public List<Book> getUserBooksByStatus(@PathVariable String status, @AuthenticationPrincipal UserDetails userDetails) {
        return bookService.getUserBooksByStatus(status, userDetails);
    }

    @PostMapping("/books")
    public Book createBook(@RequestBody CreateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) { // @RequestBody - Spring automatically deserializes JSON to the specified Java type.
        return bookService.createBook(request, userDetails);
    }

    @PutMapping("/books/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book, @AuthenticationPrincipal UserDetails userDetails) {
        return bookService.updateBook(id, book, userDetails);
    }

    @DeleteMapping("/books/{id}")
    public void deleteBookById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        bookService.deleteBookById(id, userDetails);
    }

}