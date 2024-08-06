package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.BookResponse;
import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.user.User;
import org.springframework.security.core.userdetails.UserDetails;

public class BookMapper {

    private BookMapper() {
    }

    static BookResponse mapToBookResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getStatus(),
                book.getLinkToCover(),
                book.getCategories(),
                book.getCreatedDate(),
                book.getLastModifiedDate());
    }

    static Book mapToEntity(CreateBookRequest request, UserDetails userDetails) {
        Book book = new Book(
                request.title(),
                request.author(),
                request.status(),
                request.linkToCover(),
                (User) userDetails);
        book.setCategories(request.categories());
        return book;
    }

}
