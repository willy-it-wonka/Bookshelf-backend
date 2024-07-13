package com.mybooks.bookshelfSB.exception;

public class EmailIssueException extends RuntimeException {

    public EmailIssueException(String message) {
        super(String.format("This email %s.", message));
    }

}
