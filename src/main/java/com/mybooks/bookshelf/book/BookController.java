package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.BookResponse;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.book.payload.UpdateBookRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "BookController")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public List<BookResponse> getAllUserBooks(@AuthenticationPrincipal UserDetails userDetails) { // @AuthenticationPrincipal - Spring Security provides a UserDetails object representing the logged-in user.
        List<Book> books = bookService.getAllUserBooks(userDetails);
        return books.stream()
                .map(BookMapper::mapToBookResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public BookResponse getUserBookById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) { // @PathVariable - get id from url
        Book book = bookService.getUserBookById(id, userDetails);
        return BookMapper.mapToBookResponse(book);
    }

    @GetMapping("/status")
    public List<BookResponse> getUserBooksByStatus(@RequestParam(value = "status") BookStatus status, @AuthenticationPrincipal UserDetails userDetails) {
        List<Book> books = bookService.getUserBooksByStatus(status, userDetails);
        return books.stream()
                .map(BookMapper::mapToBookResponse)
                .toList();
    }

    @PostMapping
    public BookResponse createBook(@RequestBody CreateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) { // @RequestBody - Spring automatically deserializes JSON to the specified Java type.
        Book book = bookService.createBook(request, userDetails);
        return BookMapper.mapToBookResponse(book);
    }

    @PutMapping("/{id}")
    public BookResponse updateBook(@PathVariable Long id, @RequestBody UpdateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Book book = bookService.updateBook(id, request, userDetails);
        return BookMapper.mapToBookResponse(book);
    }

    @DeleteMapping("/{id}")
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }

}
