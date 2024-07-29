package com.mybooks.bookshelf.book;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.book.payload.UpdateBookRequest;
import com.mybooks.bookshelf.exception.BookNotFoundException;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private BookService bookService;

    private User user;
    private UserDetails userDetails;
    private Book book1;
    private Book book2;
    List<Book> books;

    @BeforeEach
    void setUp() {
        user = new User("nick", "user@gmail.com", "123", UserRole.USER);
        // Mock user with authorization.
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        book1 = new Book("Book One", "Author1", BookStatus.WAITING, "link", user);
        book2 = new Book("Book Two", "Author2", BookStatus.READ, "link", user);
        book1.setId(1L);
        book2.setId(2L);
        books = Arrays.asList(book1, book2);
    }

    @Test
    @WithMockUser(username = "user@gmail.com") // Mock logged-in user.
    void whenUserAuthorized_ReturnListOfBooks() throws Exception {
        when(bookService.getAllUserBooks(eq(userDetails))).thenReturn(books);

        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Book One")))
                .andExpect(jsonPath("$[1].author", is("Author2")));
    }

    @Test
    // Unauthorized request.
    void whenUserNotAuthorized_ReturnForbidden() throws Exception {
        when(bookService.getAllUserBooks(eq(userDetails))).thenReturn(books);

        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenBookExistsByIdAndUserAuthorized_ReturnBook() throws Exception {
        Long pathVariable = 1L;
        when(bookService.getUserBookById(eq(pathVariable), eq(userDetails))).thenReturn(book1);

        mockMvc.perform(get("/api/v1/books/{id}", pathVariable)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Book One")))
                .andExpect(jsonPath("$.author", is("Author1")));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenBookDoesNotExistById_ThrowResourceNotFoundException() throws Exception {
        Long pathVariable = 999L;
        when(bookService.getUserBookById(eq(pathVariable), eq(userDetails))).thenThrow(new BookNotFoundException(pathVariable));

        mockMvc.perform(get("/api/v1/books/{id}", pathVariable)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Book with ID: %s doesn't exist.", pathVariable))));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenBooksExistByStatusAndUserAuthorized_ReturnListOfBooks() throws Exception {
        BookStatus status = BookStatus.READ;
        when(bookService.getUserBooksByStatus(eq(status), eq(userDetails))).thenReturn(Collections.singletonList(book2));

        mockMvc.perform(get("/api/v1/books/status", status)
                        .param("status", status.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Book Two")));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenCorrectBookDataProvided_ReturnSavedBook() throws Exception {
        when(bookService.createBook(any(CreateBookRequest.class), eq(userDetails))).thenReturn(book1);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Book One")))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenBookExistsAndUserAuthorized_ReturnUpdatedBook() throws Exception {
        Book updatedBook1 = new Book("Updated Title", "Author1", BookStatus.WAITING, "link", user);
        updatedBook1.setId(1L);
        when(bookService.updateBook(eq(book1.getId()), any(UpdateBookRequest.class), eq(userDetails))).thenReturn(updatedBook1);

        mockMvc.perform(put("/api/v1/books/{id}", book1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")));
    }

    @Test
    @WithMockUser(username = "user@gmail.com")
    void whenBookExistsByIdAndUserAuthorized_DeleteBook() throws Exception {
        Long bookId = 1L;

        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk());
        verify(bookService).deleteBookById(eq(bookId));
    }

}
