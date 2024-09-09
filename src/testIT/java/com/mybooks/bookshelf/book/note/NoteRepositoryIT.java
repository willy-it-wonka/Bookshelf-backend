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

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoteRepositoryIT {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Note note;
    private Book book;

    @BeforeAll
    public static void setUpBeforeAll() {
        System.setProperty("spring.datasource.url", SingletonDatabaseContainer.getInstance().getJdbcUrl());
        System.setProperty("spring.datasource.username", SingletonDatabaseContainer.getInstance().getUsername());
        System.setProperty("spring.datasource.password", SingletonDatabaseContainer.getInstance().getPassword());
    }

    @BeforeEach
    void setUpBeforeEach() {
        User user = new User("Tom", "tom@gmail.com", "123", UserRole.USER);
        entityManager.persist(user);
        book = new Book("Title", "Author", BookStatus.WAITING, "link", user);
        entityManager.persist(book);

        note = new Note("Content of the note.", book);
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
        assertThat(foundNote.get().getContent()).isEqualTo(note.getContent());
    }

    @Test
    void whenNoteNotFoundByBookId_ThrowException() {
        Optional<Note> noteNotFound = noteRepository.findByBookId(999L);
        assertThat(noteNotFound).isNotPresent();
    }

    @Test
    void whenDeleteByBookId_RemoveNote() {
        entityManager.persist(note);
        noteRepository.deleteByBookId(book.getId());
        assertThat(noteRepository.findByBookId(book.getId())).isNotPresent();
    }

    @Test
    void whenCorrectNoteDataProvided_SaveNote() {
        Note savedNote = noteRepository.save(note);
        assertThat(entityManager.find(Note.class, savedNote.getId())).isEqualTo(note);
    }

}
