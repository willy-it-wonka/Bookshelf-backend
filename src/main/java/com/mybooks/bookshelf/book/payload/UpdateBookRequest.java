package com.mybooks.bookshelf.book.payload;

import com.mybooks.bookshelf.book.BookCategory;
import com.mybooks.bookshelf.book.BookStatus;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record UpdateBookRequest(
        @NotBlank(message = "Title cannot be empty.")
        String title,
        @NotBlank(message = "Author cannot be empty.")
        String author,
        BookStatus status,
        String linkToCover,
        Set<BookCategory> categories) {
}
