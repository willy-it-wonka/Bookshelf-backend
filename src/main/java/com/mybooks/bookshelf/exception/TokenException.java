package com.mybooks.bookshelf.exception;

public class TokenException extends RuntimeException {

    public TokenException(String message) {
        super(String.format("Email confirmation error: %s", message));
    }

}
