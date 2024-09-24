package com.mybooks.bookshelf.exception;

public class IncorrectPasswordException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Incorrect password.";

    public IncorrectPasswordException() {
        super(ERROR_MESSAGE);
    }

}
