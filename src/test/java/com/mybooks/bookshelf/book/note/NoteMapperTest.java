package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.NoteResponse;
import com.mybooks.bookshelf.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteMapperTest {

    private Book book;

    @BeforeEach
    void setUp() {
        book = new Book("Title", "Author", BookStatus.READ, "link", new User());
        book.setId(1L);
    }

    @Test
    void whenValidNoteProvided_ReturnNoteResponse() {
        Note note = new Note("Content", book);
        note.setId(10L);

        NoteResponse response = NoteMapper.mapToNoteResponse(note);

        assertNotNull(response);
        assertEquals(note.getId(), response.id());
        assertEquals(note.getContent(), response.content());
        assertEquals(note.getBook().getId(), response.bookId());
    }

    @Test
    void whenNoteHasEmptyContent_ReturnNoteResponse() {
        Note emptyNote = new Note("", book);
        emptyNote.setId(10L);

        NoteResponse response = NoteMapper.mapToNoteResponse(emptyNote);

        assertNotNull(response);
        assertEquals(emptyNote.getId(), response.id());
        assertEquals(emptyNote.getContent(), response.content());
        assertEquals(emptyNote.getBook().getId(), response.bookId());
    }

    @Test
    void whenBookIsNull_ThrowNullPointerException() {
        Note note = new Note("Content", null);
        assertThrows(NullPointerException.class, () -> NoteMapper.mapToNoteResponse(note));
    }

    @Test
    void whenCreateNoteRequest_ReturnNote() {
        CreateNoteRequest request = new CreateNoteRequest("Content", book);

        Note note = NoteMapper.mapToEntity(request);

        assertNotNull(note);
        assertEquals(request.content(), note.getContent());
        assertEquals(request.book(), note.getBook());
    }

}
