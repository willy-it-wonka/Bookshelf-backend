package com.mybooks.bookshelfSB.book.note.payload;

import com.mybooks.bookshelfSB.book.Book;

public record CreateNoteRequest(String content, Book book) {
}
