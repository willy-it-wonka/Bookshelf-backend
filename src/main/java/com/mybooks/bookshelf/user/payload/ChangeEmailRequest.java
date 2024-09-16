package com.mybooks.bookshelf.user.payload;

public record ChangeEmailRequest(
        String email,
        String password) {
}
