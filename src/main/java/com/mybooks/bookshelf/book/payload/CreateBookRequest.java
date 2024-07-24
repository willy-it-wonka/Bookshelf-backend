package com.mybooks.bookshelf.book.payload;

import com.mybooks.bookshelf.book.BookCategory;
import com.mybooks.bookshelf.book.BookStatus;

import java.util.Set;

public record CreateBookRequest(
        String title,
        String author,
        BookStatus status,
        String linkToCover,
        Set<BookCategory> categories) {
}