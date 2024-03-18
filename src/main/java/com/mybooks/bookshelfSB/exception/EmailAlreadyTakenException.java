package com.mybooks.bookshelfSB.exception;

public class EmailAlreadyTakenException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EmailAlreadyTakenException(String email) {
        super(String.format("Email address %s is already associated with some account.", email));
    }
}
