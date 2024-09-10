package com.mybooks.bookshelf.book.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.book.note.payload.CreateNoteRequest;
import com.mybooks.bookshelf.book.note.payload.UpdateNoteRequest;
import com.mybooks.bookshelf.exception.NoteNotFoundException;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
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
        User user = new User("nick", "user@gmail.com", "123", UserRole.USER);
        book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(1L);
        note = new Note("Content of the note.", book);
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenGetNoteByBookId_ReturnNoteResponse() throws Exception {
        when(noteService.getNoteByBookId(book.getId())).thenReturn(note);
        mockMvc.perform(get("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Content of the note."))
                .andExpect(jsonPath("$.bookId").value(1L));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenGetNoteByNonExistingBookId_ThrowNoteNotFoundException() throws Exception {
        Long nonExistingBookId = 999L;
        when(noteService.getNoteByBookId(nonExistingBookId)).thenThrow(new NoteNotFoundException(nonExistingBookId));

        mockMvc.perform(get("/api/v1/notes/{bookId}", nonExistingBookId)
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
    @WithMockUser(username = "user@gmail.com")
    void whenCreateNoteRequest_ReturnCreatedNoteResponse() throws Exception {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest(note.getContent(), book);
        when(noteService.createNote(any(CreateNoteRequest.class))).thenReturn(note);

        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Content of the note."))
                .andExpect(jsonPath("$.bookId").value(1L));
    }

    @Test
    void whenCreateNoteWithoutAuthorization_ReturnForbidden() throws Exception {
        CreateNoteRequest createNoteRequest = new CreateNoteRequest(note.getContent(), book);
        mockMvc.perform(post("/api/v1/notes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createNoteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenUpdateNoteRequest_ReturnUpdatedNoteResponse() throws Exception {
        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest("Updated content.");
        Note updatedNote = new Note("Updated content.", book);

        when(noteService.updateNote(book.getId(), updateNoteRequest)).thenReturn(updatedNote);

        mockMvc.perform(put("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNoteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated content."))
                .andExpect(jsonPath("$.bookId").value(1L));
    }

    @Test
    void whenUpdateNoteWithoutAuthorization_ReturnForbidden() throws Exception {
        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest("Updated content.");
        mockMvc.perform(put("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNoteRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenUpdateNoteWithNonExistingBookId_ThrowNoteNotFoundException() throws Exception {
        Long nonExistingBookId = 999L;
        UpdateNoteRequest updateNoteRequest = new UpdateNoteRequest("Updated content.");

        when(noteService.updateNote(nonExistingBookId, updateNoteRequest)).thenThrow(new NoteNotFoundException(nonExistingBookId));

        mockMvc.perform(put("/api/v1/notes/{bookId}", nonExistingBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateNoteRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenNoteExistsByBookId_DeleteNote() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        verify(noteService).deleteNoteByBookId(1L);
    }

    @Test
    void whenDeleteNoteWithoutAuthorization_ReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/{bookId}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenDeleteNoteWithNonExistingBookId_ReturnNotFound() throws Exception {
        Long nonExistingBookId = 999L;

        doThrow(new NoteNotFoundException(nonExistingBookId)).when(noteService).deleteNoteByBookId(nonExistingBookId);

        mockMvc.perform(delete("/api/v1/notes/{bookId}", nonExistingBookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
