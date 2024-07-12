package com.mybooks.bookshelfSB.book.payload;

import com.mybooks.bookshelfSB.book.BookCategory;
import com.mybooks.bookshelfSB.book.BookStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record BookResponse(
        Long id,
        String title,
        String author,
        BookStatus status,
        String linkToCover,
        Set<BookCategory> categories,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate) {
}
