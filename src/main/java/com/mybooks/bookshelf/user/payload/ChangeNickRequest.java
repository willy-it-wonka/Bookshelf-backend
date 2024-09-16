package com.mybooks.bookshelf.user.payload;

public record ChangeNickRequest(
        String nick,
        String password) {
}
