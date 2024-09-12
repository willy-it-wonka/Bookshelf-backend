package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.NoteResponse;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NoteMapperTest {

    @Test
    void whenValidNoteProvided_ReturnNoteResponse() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        Book book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(1L);
        Note note = new Note("Note content", book);
        note.setId(10L);

        NoteResponse response = NoteMapper.mapToNoteResponse(note);

        assertNotNull(response);
        assertEquals(note.getId(), response.id());
        assertEquals(note.getContent(), response.content());
        assertEquals(note.getBook().getId(), response.bookId());
    }

    @Test
    void whenNoteHasEmptyContent_ReturnNoteResponse() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        Book book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(1L);
        Note note = new Note("", book);
        note.setId(10L);

        NoteResponse response = NoteMapper.mapToNoteResponse(note);

        assertNotNull(response);
        assertEquals(note.getId(), response.id());
        assertEquals(note.getContent(), response.content());
        assertEquals(note.getBook().getId(), response.bookId());
    }

    @Test
    void whenNoteIsNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> NoteMapper.mapToNoteResponse(null));
    }

    @Test
    void whenBookIsNull_ThrowNullPointerException() {
        Note note = new Note("Some content", null);
        assertThrows(NullPointerException.class, () -> NoteMapper.mapToNoteResponse(note));
    }

    @Test
    void whenCreateNoteRequest_ReturnNote() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        Book book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(1L);
        CreateNoteRequest request = new CreateNoteRequest("Note content", book);

        Note note = NoteMapper.mapToEntity(request);

        assertNotNull(note);
        assertEquals(request.content(), note.getContent());
        assertEquals(request.book(), note.getBook());
    }

    @Test
    void whenCreateNoteRequestIsNull_ThrowNullPointerException() {
        assertThrows(NullPointerException.class, () -> NoteMapper.mapToEntity(null));
    }

}
