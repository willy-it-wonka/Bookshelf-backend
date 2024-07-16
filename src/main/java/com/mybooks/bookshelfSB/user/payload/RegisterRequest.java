package com.mybooks.bookshelfSB.user.payload;

public record RegisterRequest(
        String nick,
        String email,
        String password) {
}
