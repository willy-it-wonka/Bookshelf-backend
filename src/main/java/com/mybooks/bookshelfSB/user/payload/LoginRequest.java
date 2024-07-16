package com.mybooks.bookshelfSB.user.payload;

public record LoginRequest(
        String email,
        String password) {
}
