package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.BookResponse;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookMapperTest {

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        user.setId(1L);
        book = new Book("Title", "Author", BookStatus.READ, "link", user);
        book.setId(1L);
        book.setCreatedDate(LocalDateTime.now());
        book.setLastModifiedDate(LocalDateTime.now());
    }

    @Test
    void whenBookWithCategoriesProvided_ReturnBookResponse() {
        book.setCategories(new HashSet<>(Set.of(BookCategory.IT)));

        BookResponse response = BookMapper.mapToBookResponse(book);

        assertNotNull(response);
        assertEquals(book.getId(), response.id());
        assertEquals(book.getTitle(), response.title());
        assertEquals(book.getAuthor(), response.author());
        assertEquals(book.getStatus(), response.status());
        assertEquals(book.getLinkToCover(), response.linkToCover());
        assertEquals(book.getCategories(), response.categories());
        assertEquals(book.getCreatedDate(), response.createdDate());
        assertEquals(book.getLastModifiedDate(), response.lastModifiedDate());
    }

    @Test
    void whenBookWithEmptyCategoriesProvided_ReturnBookResponse() {
        BookResponse response = BookMapper.mapToBookResponse(book);

        assertNotNull(response);
        assertEquals(book.getId(), response.id());
        assertEquals(book.getTitle(), response.title());
        assertEquals(book.getAuthor(), response.author());
        assertEquals(book.getStatus(), response.status());
        assertEquals(book.getLinkToCover(), response.linkToCover());
        assertNotNull(response.categories());
        assertTrue(response.categories().isEmpty());
        assertEquals(book.getCreatedDate(), response.createdDate());
        assertEquals(book.getLastModifiedDate(), response.lastModifiedDate());
    }

    @Test
    void whenCreateBookRequestWithCategories_ReturnBook() {
        Set<BookCategory> categories = new HashSet<>(Set.of(BookCategory.IT, BookCategory.HISTORY));
        CreateBookRequest request = new CreateBookRequest("Title", "Author", BookStatus.READ, "link", categories);

        Book mappedBook = BookMapper.mapToEntity(request, user);

        assertNotNull(mappedBook);
        assertEquals(request.title(), mappedBook.getTitle());
        assertEquals(request.author(), mappedBook.getAuthor());
        assertEquals(request.status(), mappedBook.getStatus());
        assertEquals(request.linkToCover(), mappedBook.getLinkToCover());
        assertEquals(request.categories(), mappedBook.getCategories());
        assertEquals(user, mappedBook.getBookOwner());
    }

    @Test
    void whenCreateBookRequestWithEmptyCategories_ReturnBookWithEmptyCategories() {
        CreateBookRequest request = new CreateBookRequest("Title", "Author", BookStatus.READ, "link", new HashSet<>());

        Book mappedBook = BookMapper.mapToEntity(request, user);

        assertNotNull(mappedBook);
        assertEquals(request.title(), mappedBook.getTitle());
        assertEquals(request.author(), mappedBook.getAuthor());
        assertEquals(request.status(), mappedBook.getStatus());
        assertEquals(request.linkToCover(), mappedBook.getLinkToCover());
        assertTrue(mappedBook.getCategories().isEmpty());
        assertEquals(user, mappedBook.getBookOwner());
    }

}
