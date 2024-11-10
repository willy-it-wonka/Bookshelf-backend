package com.mybooks.bookshelf.user.payload;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nick cannot be empty.")
        @Size(max = 25, message = "Nick can have a maximum of 25 characters.")
        String nick,

        @Email(message = "Invalid email format.",
                regexp = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}")
        String email,

        @NotBlank(message = "Password cannot be empty.")
        @Size(min = 6, message = "Password must be at least 6 characters long.")
        String password) {
}
