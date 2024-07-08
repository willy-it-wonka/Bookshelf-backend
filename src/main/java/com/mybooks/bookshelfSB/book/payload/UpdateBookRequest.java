package com.mybooks.bookshelfSB.book.payload;

import com.mybooks.bookshelfSB.book.BookCategory;
import com.mybooks.bookshelfSB.book.BookStatus;

import java.util.Set;

public record UpdateBookRequest(
        String title,
        String author,
        BookStatus status,
        String linkToCover,
        Set<BookCategory> categories) {
}
