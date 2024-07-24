package com.mybooks.bookshelf.user.payload;

public record RegisterRequest(
        String nick,
        String email,
        String password) {
}
