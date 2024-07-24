package com.mybooks.bookshelf.book.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByBookId(Long bookId);

    void deleteByBookId(Long bookId);

}
