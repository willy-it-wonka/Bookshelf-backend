package com.mybooks.bookshelfSB.book.payload;

import com.mybooks.bookshelfSB.book.BookStatus;

import java.time.LocalDateTime;

public record BookDto(
        Long id,
        String title,
        String author,
        BookStatus status,
        String linkToCover,
        LocalDateTime createdDate,
        LocalDateTime lastModifiedDate) {
}
