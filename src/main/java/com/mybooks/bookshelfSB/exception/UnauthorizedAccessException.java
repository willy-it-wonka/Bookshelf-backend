package com.mybooks.bookshelfSB.exception;

import java.io.Serial;

public class UnauthorizedAccessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public UnauthorizedAccessException() {
        super("You don't have authorization.");
    }

}
