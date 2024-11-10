package com.mybooks.bookshelf.book.note.payload;

import jakarta.validation.constraints.NotBlank;

public record UpdateNoteRequest(
        @NotBlank(message = "Content cannot be empty.")
        String content) {
}
