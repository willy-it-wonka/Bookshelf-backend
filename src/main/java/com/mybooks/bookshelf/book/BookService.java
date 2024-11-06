package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.book.payload.CreateBookRequest;
import com.mybooks.bookshelf.book.payload.UpdateBookRequest;
import com.mybooks.bookshelf.exception.BookNotFoundException;
import com.mybooks.bookshelf.exception.UnauthorizedAccessException;
import com.mybooks.bookshelf.user.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> getAllUserBooks(UserDetails userDetails) {
        return bookRepository.findByBookOwner((User) userDetails);
    }

    @Transactional(readOnly = true)
    public Book getUserBookById(Long id, UserDetails userDetails) {
        return findUserBookById(id, userDetails);
    }

    @Transactional(readOnly = true)
    public List<Book> getUserBooksByStatus(BookStatus status, UserDetails userDetails) {
        return bookRepository.findByStatusAndBookOwner(status, (User) userDetails);
    }

    @Transactional
    public Book createBook(CreateBookRequest request, UserDetails userDetails) {
        return bookRepository.save(BookMapper.mapToEntity(request, userDetails));
    }

    @Transactional
    public Book updateBook(Long id, UpdateBookRequest request, UserDetails userDetails) {
        Book bookToUpdate = findUserBookById(id, userDetails);
        bookToUpdate.setTitle(request.title());
        bookToUpdate.setAuthor(request.author());
        bookToUpdate.setStatus(request.status());
        bookToUpdate.setLinkToCover(request.linkToCover());
        bookToUpdate.setCategories(request.categories());
        return bookRepository.save(bookToUpdate);
    }

    @Transactional
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    private Book findUserBookById(Long id, UserDetails userDetails) {
        Book book = bookRepository.findByIdWithCategories(id).orElseThrow(() -> new BookNotFoundException(id));

        // Checks if the logged-in user is the owner of the book with the specified id.
        if (!book.getBookOwner().getId().equals(((User) userDetails).getId()))
            throw new UnauthorizedAccessException();

        return book;
    }

}
