package com.mybooks.bookshelfSB.book.payload;

import com.mybooks.bookshelfSB.book.BookStatus;

public record CreateBookRequest(
        String title,
        String author,
        BookStatus status,
        String linkToCover) {
}