package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import com.mybooks.bookshelf.exception.NoteNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    Note getNoteByBookId(Long bookId) {
        return noteRepository.findByBookId(bookId).orElseThrow(() -> new NoteNotFoundException(bookId));
    }

    Note createNote(CreateNoteRequest request) {
        return noteRepository.save(NoteMapper.mapToEntity(request));
    }

    Note updateNote(Long bookId, UpdateNoteRequest request) {
        Note noteToUpdate = getNoteByBookId(bookId);
        noteToUpdate.setContent(request.content());
        return noteRepository.save(noteToUpdate);
    }

    void deleteNoteByBookId(Long bookId) {
        noteRepository.deleteByBookId(bookId);
    }

}
