package com.mybooks.bookshelf.book.note;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.mybooks.bookshelf.book.Book;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "notes")
@Getter
@Setter
@EqualsAndHashCode
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToOne
    @JoinColumn(name = "book_id", nullable = false, unique = true)
    @JsonBackReference
    private Book book;

    public Note() {
    }

    public Note(String content, Book book) {
        this.content = content;
        this.book = book;
    }

}
