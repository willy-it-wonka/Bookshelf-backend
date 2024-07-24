package com.mybooks.bookshelf.exception;

public class BookNotFoundException extends RuntimeException {

    public BookNotFoundException(Long id) {
        super(String.format("Book with ID: %s doesn't exist.", id));
    }

}
