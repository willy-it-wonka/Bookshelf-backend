package com.mybooks.bookshelf.book.note.payload;

public record NoteResponse(
        Long id,
        String content,
        Long bookId) {
}
