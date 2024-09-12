package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import com.mybooks.bookshelf.exception.NoteNotFoundException;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteServiceTest {

    private InMemoryNoteRepository noteRepository;
    private NoteService noteService;
    private Note note;
    private Book book;

    @BeforeEach
    void setUp() {
        noteRepository = new InMemoryNoteRepository();
        noteService = new NoteService(noteRepository);

        User user = new User("Tom", "tom@example.com", "123", UserRole.USER);
        book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(1L);
        note = new Note("Content of the note.", book);
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
        Long idOfBookWithoutNote = 999L;

        NoteNotFoundException e = assertThrows(NoteNotFoundException.class, () ->
                noteService.getNoteByBookId(idOfBookWithoutNote));

        assertEquals("Notes for the book with ID: 999 don't exist.", e.getMessage());
    }

    @Test
    void whenCreateNoteRequest_ReturnCreatedNote() {
        CreateNoteRequest request = new CreateNoteRequest("Content", book);

        Note createdNote = noteService.createNote(request);

        assertNotNull(createdNote.getId());
        assertEquals(request.content(), createdNote.getContent());
        assertEquals(request.book(), createdNote.getBook());
        assertTrue(noteRepository.findById(createdNote.getId()).isPresent());
    }

    @Test
    void whenNoteExistsAndUpdateNoteRequest_UpdateNoteContent() {
        UpdateNoteRequest request = new UpdateNoteRequest("Updated content");

        Note updatedNote = noteService.updateNote(note.getBook().getId(), request);

        assertNotNull(updatedNote);
        assertEquals(request.content(), updatedNote.getContent());
        assertEquals(note.getBook(), updatedNote.getBook());
    }

    @Test
    void whenNoteDoesNotExistAndUpdateNoteRequest_ThrowNoteNotFoundException() {
        Long idOfBookWithoutNote = 999L;
        UpdateNoteRequest request = new UpdateNoteRequest("Updated content");

        NoteNotFoundException e = assertThrows(NoteNotFoundException.class, () ->
                noteService.updateNote(idOfBookWithoutNote, request));

        assertEquals("Notes for the book with ID: 999 don't exist.", e.getMessage());
    }

}
