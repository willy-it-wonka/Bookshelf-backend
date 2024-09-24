package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import com.mybooks.bookshelf.exception.NoteNotFoundException;
import com.mybooks.bookshelf.user.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteServiceTest {

    private static final Long ID_OF_BOOK_WITHOUT_NOTE = 999L;
    private static final String NOTE_NOT_FOUND_ERROR = "Notes for the book with ID: 999 don't exist.";

    private InMemoryNoteRepository noteRepository;
    private NoteService noteService;
    private Note note;
    private Book book;

    @BeforeEach
    void setUp() {
        noteRepository = new InMemoryNoteRepository();
        noteService = new NoteService(noteRepository);

        book = new Book("Title", "Author", BookStatus.READ, "link", new User());
        book.setId(1L);
        note = new Note("Content", book);
        noteRepository.save(note);
    }

    @AfterEach
    void tearDown() {
        noteRepository.clear();
    }

    @Test
    void whenNoteExists_DeleteNoteByBookId() {
        noteService.deleteNoteByBookId(note.getBook().getId());
        assertFalse(noteRepository.findByBookId(note.getBook().getId()).isPresent());
    }

    @Test
    void whenNoteExists_ReturnNoteByBookId() {
        Note foundNote = noteService.getNoteByBookId(note.getBook().getId());
        assertEquals(note, foundNote);
    }

    @Test
    void whenNoteDoesNotExistByBookId_ThrowNoteNotFoundException() {
        NoteNotFoundException e = assertThrows(NoteNotFoundException.class, () ->
                noteService.getNoteByBookId(ID_OF_BOOK_WITHOUT_NOTE));
        assertEquals(NOTE_NOT_FOUND_ERROR, e.getMessage());
    }

    @Test
    void whenCreateNoteRequest_ReturnCreatedNote() {
        CreateNoteRequest request = new CreateNoteRequest("Content", book);

        Note createdNote = noteService.createNote(request);

        assertNotNull(createdNote);
        assertEquals(request.content(), createdNote.getContent());
        assertEquals(request.book(), createdNote.getBook());
    }

    @Test
    void whenNoteExistsAndUpdateNoteRequest_ReturnUpdatedNote() {
        UpdateNoteRequest request = new UpdateNoteRequest("Updated content");

        Note updatedNote = noteService.updateNote(note.getBook().getId(), request);

        assertNotNull(updatedNote);
        assertEquals(request.content(), updatedNote.getContent());
        assertEquals(note.getBook(), updatedNote.getBook());
    }

    @Test
    void whenNoteDoesNotExistAndUpdateNoteRequest_ThrowNoteNotFoundException() {
        UpdateNoteRequest request = new UpdateNoteRequest("Updated content");
        NoteNotFoundException e = assertThrows(NoteNotFoundException.class, () ->
                noteService.updateNote(ID_OF_BOOK_WITHOUT_NOTE, request));
        assertEquals(NOTE_NOT_FOUND_ERROR, e.getMessage());
    }

}
