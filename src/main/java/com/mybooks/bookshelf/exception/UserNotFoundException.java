package com.mybooks.bookshelf.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class UserNotFoundException extends UsernameNotFoundException {

    private static final String ERROR_MESSAGE = "User not found.";

    public UserNotFoundException() {
        super(ERROR_MESSAGE);
    }

}
