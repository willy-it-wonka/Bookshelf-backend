package com.mybooks.bookshelf.book.note.payload;

import com.mybooks.bookshelf.book.Book;
import jakarta.validation.constraints.NotBlank;

public record CreateNoteRequest(
        @NotBlank(message = "Content cannot be empty.")
        String content,
        Book book) {
}
