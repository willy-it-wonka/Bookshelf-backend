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

    public static final Long BOOK_ID = 1L;

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
    private List<Book> books;

    @BeforeEach
    void setUp() {
        user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        // Mock user with authorization.
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();

        book1 = new Book("Book 1", "Author 1", BookStatus.WAITING, "link 1", user);
        book2 = new Book("Book 2", "Author 2", BookStatus.READ, "link 2", user);
        book1.setId(1L);
        book2.setId(2L);
        books = Arrays.asList(book1, book2);
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenUserAuthorized_ReturnListOfAllUserBooks() throws Exception {
        when(bookService.getAllUserBooks(userDetails)).thenReturn(books);

        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Book 1")))
                .andExpect(jsonPath("$[1].author", is("Author 2")));
    }

    @Test
    void whenUserNotAuthorized_ReturnForbidden() throws Exception {
        when(bookService.getAllUserBooks(userDetails)).thenReturn(books);

        mockMvc.perform(get("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenBookExistsByIdAndUserAuthorized_ReturnBook() throws Exception {
        when(bookService.getUserBookById(BOOK_ID, userDetails)).thenReturn(book1);

        mockMvc.perform(get("/api/v1/books/{id}", BOOK_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Book 1")))
                .andExpect(jsonPath("$.author", is("Author 1")));
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenBookDoesNotExistById_ThrowBookNotFoundException() throws Exception {
        Long nonExistentBookId = 999L;
        when(bookService.getUserBookById(nonExistentBookId, userDetails)).thenThrow(new BookNotFoundException(nonExistentBookId));

        mockMvc.perform(get("/api/v1/books/{id}", nonExistentBookId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(String.format("Book with ID: %s doesn't exist.", nonExistentBookId))));
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenBooksExistByStatusAndUserAuthorized_ReturnListOfBooks() throws Exception {
        BookStatus status = BookStatus.READ;
        when(bookService.getUserBooksByStatus(status, userDetails)).thenReturn(Collections.singletonList(book2));

        mockMvc.perform(get("/api/v1/books/status", status)
                        .param("status", status.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Book 2")))
                .andExpect(jsonPath("$[0].author", is("Author 2")))
                .andExpect(jsonPath("$[0].status", is("READ")));
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenInvalidBookStatusProvided_ReturnBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/books/status")
                        .param("status", "invalid_status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    @WithMockUser(username = "tom@test.com")
    void whenCreateBookRequest_ReturnSavedBook() throws Exception {
        when(bookService.createBook(any(CreateBookRequest.class), eq(userDetails))).thenReturn(book1);

        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Book 1")))
                .andExpect(jsonPath("$.author", is("Author 1")))
                .andExpect(jsonPath("$.status", is("WAITING")));
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenUpdateBookRequestAndUserAuthorized_ReturnUpdatedBook() throws Exception {
        Book updatedBook1 = new Book("Updated Title", "Author 1", BookStatus.WAITING, "link 1", user);
        when(bookService.updateBook(eq(book1.getId()), any(UpdateBookRequest.class), eq(userDetails))).thenReturn(updatedBook1);

        mockMvc.perform(put("/api/v1/books/{id}", book1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedBook1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.author", is("Author 1")));
    }

    @Test
    @WithMockUser(username = "tom@test.com")
    void whenBookExistsAndUserAuthorized_DeleteBookById() throws Exception {
        mockMvc.perform(delete("/api/v1/books/{id}", BOOK_ID))
                .andExpect(status().isOk());
        verify(bookService).deleteBookById(BOOK_ID);
    }

}
