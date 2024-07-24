package com.mybooks.bookshelf.exception;

public class EmailException extends RuntimeException {

    public EmailException(String message) {
        super(String.format("This email %s.", message));
    }

}
