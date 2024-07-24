package com.mybooks.bookshelf.user.payload;

public record LoginRequest(
        String email,
        String password) {
}
