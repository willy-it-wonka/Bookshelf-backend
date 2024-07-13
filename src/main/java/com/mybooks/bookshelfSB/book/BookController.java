package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.book.payload.BookResponse;
import com.mybooks.bookshelfSB.book.payload.CreateBookRequest;
import com.mybooks.bookshelfSB.book.payload.UpdateBookRequest;
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
    public List<BookResponse> getAllUserBooks(@AuthenticationPrincipal UserDetails userDetails) { // @AuthenticationPrincipal - Spring Security provides a UserDetails object representing the logged-in user.
        List<Book> books = bookService.getAllUserBooks(userDetails);
        return books.stream()
                .map(this::convertToDto)
                .toList();
    }

    @GetMapping("/books/{id}")
    public BookResponse getUserBookById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) { // @PathVariable - get id from url
        Book book = bookService.getUserBookById(id, userDetails);
        return convertToDto(book);
    }

    @GetMapping("/books/status/{status}") // In case: "/books/{status}" - would lead to a conflict with "/books/{id}".
    public List<BookResponse> getUserBooksByStatus(@PathVariable BookStatus status, @AuthenticationPrincipal UserDetails userDetails) {
        List<Book> books = bookService.getUserBooksByStatus(status, userDetails);
        return books.stream()
                .map(this::convertToDto)
                .toList();
    }

    @PostMapping("/books")
    public BookResponse createBook(@RequestBody CreateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) { // @RequestBody - Spring automatically deserializes JSON to the specified Java type.
        Book book = bookService.createBook(request, userDetails);
        return convertToDto(book);
    }

    @PutMapping("/books/{id}")
    public BookResponse updateBook(@PathVariable Long id, @RequestBody UpdateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Book book = bookService.updateBook(id, request, userDetails);
        return convertToDto(book);
    }

    @DeleteMapping("/books/{id}")
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }

    private BookResponse convertToDto(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getStatus(),
                book.getLinkToCover(),
                book.getCategories(),
                book.getCreatedDate(),
                book.getLastModifiedDate());
    }

}
