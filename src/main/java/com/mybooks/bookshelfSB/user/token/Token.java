package com.mybooks.bookshelfSB.user.token;

import com.mybooks.bookshelfSB.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String token;
    private LocalDateTime creationDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime expirationDate;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User user;

    public Token() {
    }

    public Token(String token, LocalDateTime creationDate, LocalDateTime expirationDate, User user) {
        this.token = token;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.user = user;
    }
}
