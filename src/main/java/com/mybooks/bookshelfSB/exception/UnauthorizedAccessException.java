package com.mybooks.bookshelfSB.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException() {
        super("You don't have authorization.");
    }

}
