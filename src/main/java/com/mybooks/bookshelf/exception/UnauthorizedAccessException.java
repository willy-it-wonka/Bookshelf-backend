package com.mybooks.bookshelf.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public static final String ERROR_MESSAGE = "You don't have authorization.";

    public UnauthorizedAccessException() {
        super(ERROR_MESSAGE);
    }

}
