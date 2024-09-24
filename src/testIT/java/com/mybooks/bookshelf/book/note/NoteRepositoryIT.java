package com.mybooks.bookshelf.book.note;

import com.mybooks.bookshelf.SingletonDatabaseContainer;
import com.mybooks.bookshelf.book.Book;
import com.mybooks.bookshelf.book.BookStatus;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoteRepositoryIT {

    private static final Long BOOK_ID = 1L;
    private static final Long ID_OF_BOOK_WITHOUT_NOTE = 999L;

    @Autowired
    private NoteRepository noteRepository;
    @Autowired
    private TestEntityManager entityManager;

    private Book book;
    private Note note;

    @BeforeAll
    public static void setUpBeforeAll() {
        System.setProperty("spring.datasource.url", SingletonDatabaseContainer.getInstance().getJdbcUrl());
        System.setProperty("spring.datasource.username", SingletonDatabaseContainer.getInstance().getUsername());
        System.setProperty("spring.datasource.password", SingletonDatabaseContainer.getInstance().getPassword());
    }

    @BeforeEach
    void setUpBeforeEach() {
        User user = new User("Tom", "tom@test.com", "123", UserRole.USER);
        entityManager.persist(user);
        book = new Book("Title", "Author", BookStatus.WAITING, "link", user);
        entityManager.persist(book);

        note = new Note("Content", book);
    }

    @AfterEach
    void tearDown() {
        noteRepository.deleteAll();
        entityManager.clear();
    }

    @Test
    void whenNoteFoundByBookId_ReturnNote() {
        entityManager.persist(note);

        Optional<Note> foundNote = noteRepository.findByBookId(book.getId());

        assertThat(foundNote).isPresent();
        assertEquals(foundNote.get().getContent(), note.getContent());
    }

    @Test
    void whenNoteNotFoundByBookId_ThrowException() {
        Optional<Note> noteNotFound = noteRepository.findByBookId(ID_OF_BOOK_WITHOUT_NOTE);
        assertThat(noteNotFound).isNotPresent();
    }

    @Test
    void whenDeleteByBookId_RemoveNote() {
        entityManager.persist(note);
        noteRepository.deleteByBookId(BOOK_ID);
        assertThat(noteRepository.findByBookId(BOOK_ID)).isNotPresent();
    }

    @Test
    void whenCorrectNoteDataProvided_ReturnSavedNote() {
        Note savedNote = noteRepository.save(note);
        assertNotNull(savedNote);
        assertEquals(entityManager.find(Note.class, savedNote.getId()), note);
    }

}
