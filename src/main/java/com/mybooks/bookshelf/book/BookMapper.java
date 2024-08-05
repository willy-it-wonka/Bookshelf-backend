package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.BookResponse;

public class BookMapper {

    private BookMapper() {
    }

    static BookResponse mapToBookResponse(Book book) {
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
