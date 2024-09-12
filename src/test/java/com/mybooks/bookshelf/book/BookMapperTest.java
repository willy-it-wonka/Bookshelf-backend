package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.BookResponse;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BookMapperTest {

    @Test
    void whenBookProvided_ReturnBookResponse() {
        Book book = new Book("Title", "Author", BookStatus.READ, "link", new User());
        book.setId(1L);
        book.setCreatedDate(LocalDateTime.now());
        book.setLastModifiedDate(LocalDateTime.now());
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
        Book book = new Book("Title", "Author", BookStatus.READ, "link", new User());
        book.setId(1L);
        book.setCreatedDate(LocalDateTime.now());
        book.setLastModifiedDate(LocalDateTime.now());

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
    void whenCreateBookRequest_ReturnBook() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        user.setId(1L);
        Set<BookCategory> categories = new HashSet<>(Set.of(BookCategory.IT, BookCategory.HISTORY));
        CreateBookRequest request = new CreateBookRequest("Title", "Author", BookStatus.READ, "link", categories);

        Book book = BookMapper.mapToEntity(request, user);

        assertNotNull(book);
        assertEquals(request.title(), book.getTitle());
        assertEquals(request.author(), book.getAuthor());
        assertEquals(request.status(), book.getStatus());
        assertEquals(request.linkToCover(), book.getLinkToCover());
        assertEquals(user, book.getBookOwner());
        assertEquals(request.categories(), book.getCategories());
    }

    @Test
    void whenCreateBookRequestWithEmptyCategories_ReturnBookWithEmptyCategories() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        user.setId(1L);
        CreateBookRequest request = new CreateBookRequest("Title", "Author", BookStatus.READ, "link", new HashSet<>());

        Book book = BookMapper.mapToEntity(request, user);

        assertNotNull(book);
        assertEquals(request.title(), book.getTitle());
        assertEquals(request.author(), book.getAuthor());
        assertEquals(request.status(), book.getStatus());
        assertEquals(request.linkToCover(), book.getLinkToCover());
        assertEquals(user, book.getBookOwner());
        assertTrue(book.getCategories().isEmpty());
    }

}
