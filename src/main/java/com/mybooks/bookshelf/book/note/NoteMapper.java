package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.NoteResponse;

public class NoteMapper {

    private NoteMapper() {
    }

    static NoteResponse mapToNoteResponse(Note note) {
        return new NoteResponse(
                note.getId(),
                note.getContent(),
                note.getBook().getId());
    }

    static Note mapToEntity(CreateNoteRequest request) {
        return new Note(
                request.content(),
                request.book());
    }

}
