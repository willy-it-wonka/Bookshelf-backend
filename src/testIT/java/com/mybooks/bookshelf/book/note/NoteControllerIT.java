package com.mybooks.bookshelf.book.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import com.mybooks.bookshelf.exception.NoteNotFoundException;
import com.mybooks.bookshelf.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerIT {

    public static final Long BOOK_ID = 1L;
    public static final Long ID_OF_BOOK_WITHOUT_NOTE = 999L;
    public static final String NOTE_CONTENT = "Content";
    public static final String UPDATED_CONTENT = "Updated content";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private NoteService noteService;

    private Book book;
    private Note note;

    @BeforeEach
    void setUp() {
        book = new Book("Title", "Author", BookStatus.READ, "link", new User());
        book.setId(1L);
        note = new Note("Content", book);
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenGetNoteByBookId_ReturnNoteResponse() throws Exception {
        when(noteService.getNoteByBookId(book.getId())).thenReturn(note);
        mockMvc.perform(get("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(NOTE_CONTENT))
                .andExpect(jsonPath("$.bookId").value(BOOK_ID));
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenGetNonExistentNoteByBookId_ThrowNoteNotFoundException() throws Exception {
        when(noteService.getNoteByBookId(ID_OF_BOOK_WITHOUT_NOTE)).thenThrow(new NoteNotFoundException(ID_OF_BOOK_WITHOUT_NOTE));
        mockMvc.perform(get("/api/v1/notes/{bookId}", ID_OF_BOOK_WITHOUT_NOTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void whenGetNoteByBookIdWithoutAuthorization_ReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenCreateNoteRequest_ReturnNoteResponse() throws Exception {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest(NOTE_CONTENT, book);
        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(note);
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(NOTE_CONTENT))
                .andExpect(jsonPath("$.bookId").value(BOOK_ID));
    }

    @Test
    void whenCreateNoteRequestWithoutAuthorization_ReturnForbidden() throws Exception {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest(NOTE_CONTENT, book);
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNoteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenUpdateNoteRequest_ReturnNoteResponse() throws Exception {
        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest(UPDATED_CONTENT);
        Note updatedNote = new Note(UPDATED_CONTENT, book);

        when(noteService.updateNote(BOOK_ID, updateNoteRequest)).thenReturn(updatedNote);

        mockMvc.perform(put("/api/v1/notes/{bookId}", BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value(UPDATED_CONTENT))
                .andExpect(jsonPath("$.bookId").value(BOOK_ID));
    }

    @Test
    void whenUpdateNoteRequestWithoutAuthorization_ReturnForbidden() throws Exception {
        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest(UPDATED_CONTENT);
        mockMvc.perform(put("/api/v1/notes/{bookId}", BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNoteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenUpdateNoteRequestForNonExistentNote_ThrowNoteNotFoundException() throws Exception {
        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest(UPDATED_CONTENT);
        when(noteService.updateNote(ID_OF_BOOK_WITHOUT_NOTE, updateNoteRequest)).thenThrow(new NoteNotFoundException(ID_OF_BOOK_WITHOUT_NOTE));
        mockMvc.perform(put("/api/v1/notes/{bookId}", ID_OF_BOOK_WITHOUT_NOTE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNoteRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenNoteExistsByBookId_DeleteNote() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/{bookId}", BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(noteService).deleteNoteByBookId(BOOK_ID);
    }

    @Test
    void whenDeleteNoteByBookIdWithoutAuthorization_ReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/{bookId}", BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@test.com")
    void whenDeleteNoteForNonExistentNote_ReturnNotFound() throws Exception {
        doThrow(new NoteNotFoundException(ID_OF_BOOK_WITHOUT_NOTE)).when(noteService).deleteNoteByBookId(ID_OF_BOOK_WITHOUT_NOTE);
        mockMvc.perform(delete("/api/v1/notes/{bookId}", ID_OF_BOOK_WITHOUT_NOTE)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
