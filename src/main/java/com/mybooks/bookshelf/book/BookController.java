package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.BookResponse;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.book.payload.UpdateBookRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "BookController")
public class BookController {

    private static final String ALL_BOOKS_SUMMARY = "Get all user's books";
    private static final String BOOK_BY_ID_SUMMARY = "Get user's book by ID";
    private static final String BOOKS_BY_STATUS_SUMMARY = "Get user's books by status";
    private static final String BOOK_CREATION_SUMMARY = "Create a new book";
    private static final String BOOK_UPDATE_SUMMARY = "Update an existing book";
    private static final String BOOK_DELETION_SUMMARY = "Delete a book by ID";

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    @Operation(summary = ALL_BOOKS_SUMMARY)
    public List<BookResponse> getAllUserBooks(@AuthenticationPrincipal UserDetails userDetails) { // @AuthenticationPrincipal - Spring Security provides a UserDetails object representing the logged-in user.
        List<Book> books = bookService.getAllUserBooks(userDetails);
        return books.stream()
                .map(BookMapper::mapToBookResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = BOOK_BY_ID_SUMMARY)
    public BookResponse getUserBookById(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) { // @PathVariable - get id from url
        Book book = bookService.getUserBookById(id, userDetails);
        return BookMapper.mapToBookResponse(book);
    }

    @GetMapping("/status")
    @Operation(summary = BOOKS_BY_STATUS_SUMMARY)
    public List<BookResponse> getUserBooksByStatus(@RequestParam(value = "status") BookStatus status, @AuthenticationPrincipal UserDetails userDetails) {
        List<Book> books = bookService.getUserBooksByStatus(status, userDetails);
        return books.stream()
                .map(BookMapper::mapToBookResponse)
                .toList();
    }

    @PostMapping
    @Operation(summary = BOOK_CREATION_SUMMARY)
    public BookResponse createBook(@RequestBody CreateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) { // @RequestBody - Spring automatically deserializes JSON to the specified Java type.
        Book book = bookService.createBook(request, userDetails);
        return BookMapper.mapToBookResponse(book);
    }

    @PutMapping("/{id}")
    @Operation(summary = BOOK_UPDATE_SUMMARY)
    public BookResponse updateBook(@PathVariable Long id, @RequestBody UpdateBookRequest request, @AuthenticationPrincipal UserDetails userDetails) {
        Book book = bookService.updateBook(id, request, userDetails);
        return BookMapper.mapToBookResponse(book);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = BOOK_DELETION_SUMMARY)
    public void deleteBookById(@PathVariable Long id) {
        bookService.deleteBookById(id);
    }

}
