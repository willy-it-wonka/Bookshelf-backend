package com.mybooks.bookshelfSB.exception;

public class NoteNotFoundException extends RuntimeException {

    public NoteNotFoundException(Long id) {
        super(String.format("Notes for the book with ID: %s don't exist.", id));
    }

}
