package com.mybooks.bookshelfSB.exception;

import java.io.Serial;

public class EmailIssueException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public EmailIssueException(String message) {
        super(String.format("This email %s.", message));
    }

}
