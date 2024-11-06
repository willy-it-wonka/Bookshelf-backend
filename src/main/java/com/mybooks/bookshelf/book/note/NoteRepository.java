package com.mybooks.bookshelf.book.note;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    Optional<Note> findByBookId(Long bookId);

    @Transactional
    void deleteByBookId(Long bookId);

}
