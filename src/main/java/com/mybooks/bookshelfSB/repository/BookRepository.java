package com.mybooks.bookshelfSB.repository;

import com.mybooks.bookshelfSB.model.Book;
import com.mybooks.bookshelfSB.model.BookStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    List<Book> findByStatus(BookStatus status);

}