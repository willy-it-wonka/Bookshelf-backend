package com.mybooks.bookshelf.user.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 6, message = "Password must be at least 6 characters long.")
        String newPassword,
        String currentPassword) {
}
