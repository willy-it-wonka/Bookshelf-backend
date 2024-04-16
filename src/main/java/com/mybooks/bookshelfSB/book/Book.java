package com.mybooks.bookshelfSB.book;

import com.mybooks.bookshelfSB.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "books")
@EntityListeners(AuditingEntityListener.class)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String title;
    private String author;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(nullable = false)
    protected LocalDateTime lastModifiedDate;

    private String linkToCover;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public Book() {}

    public Book(String title, String author, BookStatus status, User user) {
        this.title = title;
        this.author = author;
        this.status = status;
        this.user = user;
    }
}