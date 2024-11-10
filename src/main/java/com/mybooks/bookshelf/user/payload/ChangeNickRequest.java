package com.mybooks.bookshelf.user.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangeNickRequest(
        @NotBlank(message = "Nick cannot be empty.")
        @Size(max = 25, message = "Nick can have a maximum of 25 characters.")
        String nick,
        String password) {
}
