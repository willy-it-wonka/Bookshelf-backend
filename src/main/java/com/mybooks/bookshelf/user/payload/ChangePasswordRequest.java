package com.mybooks.bookshelf.user.payload;

public record ChangePasswordRequest(
        String newPassword,
        String currentPassword) {
}
