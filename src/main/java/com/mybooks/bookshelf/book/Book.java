package com.mybooks.bookshelf.book;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.mybooks.bookshelf.book.note.Note;
import com.mybooks.bookshelf.user.User;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@EqualsAndHashCode(exclude = {"createdDate", "lastModifiedDate"})
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    private String linkToCover;

    @ElementCollection(targetClass = BookCategory.class)
    @CollectionTable(name = "book_categories", joinColumns = @JoinColumn(name = "book_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private Set<BookCategory> categories = new HashSet<>();

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