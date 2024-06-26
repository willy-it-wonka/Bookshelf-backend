package com.mybooks.bookshelfSB.book;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mybooks.bookshelfSB.book.note.Note;
import com.mybooks.bookshelfSB.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private String linkToCover;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    @JsonBackReference   // Solve problems with cyclic object serialization and lazy initialization.
    private User bookOwner;

    @OneToOne(mappedBy = "book")
    @JsonManagedReference
    private Note note;

    public Book() {
    }

    public Book(String title, String author, BookStatus status, String linkToCover, User bookOwner) {
        this.title = title;
        this.author = author;
        this.status = status;
        this.linkToCover = linkToCover;
        this.bookOwner = bookOwner;
    }
}