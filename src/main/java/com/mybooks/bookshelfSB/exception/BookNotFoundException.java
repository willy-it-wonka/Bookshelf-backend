package com.mybooks.bookshelfSB.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

//If a BookNotFoundException is thrown, the HTTP server will return a message with the status code 404 (Not Found).
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class BookNotFoundException extends RuntimeException {

    //Unique IDentifier used to identify the version of class during serialization and deserialization.
    @Serial
    private static final long serialVersionUID = 1L;

    public BookNotFoundException(Long id) {
        super(String.format("Book with ID: %s doesn't exist.", id));
    }

}
