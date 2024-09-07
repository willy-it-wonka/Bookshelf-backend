package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.id = :id")
    Optional<Book> findByIdWithCategories(Long id);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.bookOwner = :user")
    List<Book> findByBookOwner(User user);

    @Query("SELECT b FROM Book b LEFT JOIN FETCH b.categories WHERE b.status = :status AND b.bookOwner = :user")
    List<Book> findByStatusAndBookOwner(BookStatus status, User user);

}