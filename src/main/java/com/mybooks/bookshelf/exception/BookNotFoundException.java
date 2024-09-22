package com.mybooks.bookshelf.exception;

public class BookNotFoundException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Book with ID: %s doesn't exist.";

    public BookNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }

}
