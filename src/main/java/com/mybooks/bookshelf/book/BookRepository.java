package com.mybooks.bookshelf.book;

import com.mybooks.bookshelf.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByBookOwner(User user);

    List<Book> findByStatusAndBookOwner(BookStatus status, User user);

}