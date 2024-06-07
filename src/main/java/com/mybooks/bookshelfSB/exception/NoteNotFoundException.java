package com.mybooks.bookshelfSB.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoteNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public NoteNotFoundException(Long id) {
        super(String.format("Notes for the book with ID: %s don't exist.", id));
    }

}
