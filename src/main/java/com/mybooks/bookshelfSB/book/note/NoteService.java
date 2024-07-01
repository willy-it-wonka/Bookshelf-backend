package com.mybooks.bookshelfSB.book.note;

import com.mybooks.bookshelfSB.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelfSB.book.note.payload.UpdateNoteRequest;
import com.mybooks.bookshelfSB.exception.NoteNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public void deleteNoteByBookId(Long bookId) {
        Note note = getNoteByBookId(bookId);
        noteRepository.delete(note);
    }

    Note getNoteByBookId(Long bookId) {
        return noteRepository.findByBookId(bookId).orElseThrow(() -> new NoteNotFoundException(bookId));
    }

    Note createNote(CreateNoteRequest request) {
        Note note = new Note(request.content(), request.book());
        return noteRepository.save(note);
    }

    Note updateNote(Long bookId, UpdateNoteRequest request) {
        Note noteToUpdate = getNoteByBookId(bookId);
        noteToUpdate.setContent(request.content());
        return noteRepository.save(noteToUpdate);
    }

}
