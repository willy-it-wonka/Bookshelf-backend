package com.mybooks.bookshelf.user.payload;

import jakarta.validation.constraints.Email;

public record RegisterRequest(
        String nick,
        @Email(message = "Invalid email format.",
                regexp = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}")
        String email,
        String password) {
}
