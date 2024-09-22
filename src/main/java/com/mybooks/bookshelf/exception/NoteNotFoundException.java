package com.mybooks.bookshelf.exception;

public class NoteNotFoundException extends RuntimeException {

    public static final String ERROR_MESSAGE = "Notes for the book with ID: %s don't exist.";

    public NoteNotFoundException(Long id) {
        super(String.format(ERROR_MESSAGE, id));
    }

}
