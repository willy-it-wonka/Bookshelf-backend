package com.mybooks.bookshelfSB.book.note.payload;

public record NoteResponse(
        Long id,
        String content,
        Long bookId) {
}
