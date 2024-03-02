package com.mybooks.bookshelfSB.repository;

import com.mybooks.bookshelfSB.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

}
