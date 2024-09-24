package com.mybooks.bookshelf.exception;

public class TokenException extends RuntimeException {

    private static final String ERROR_MESSAGE = "Email confirmation error: %s";

    public TokenException(String message) {
        super(String.format(ERROR_MESSAGE, message));
    }

    public TokenException(String message, boolean includePrefix) {
        super(includePrefix ? String.format(ERROR_MESSAGE, message) : message);
    }

}
