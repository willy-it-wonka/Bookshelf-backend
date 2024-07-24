package com.mybooks.bookshelf.book.note.payload;

import com.mybooks.bookshelf.book.Book;

public record CreateNoteRequest(String content, Book book) {
}
