package com.mybooks.bookshelf.user.token;

import com.mybooks.bookshelf.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// Tokens used to confirm an email account.
@Entity
@Table(name = "tokens")
@Getter
@Setter
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String confirmationToken;

    private LocalDateTime creationDate;
    private LocalDateTime confirmationDate;
    private LocalDateTime expirationDate;

    @ManyToOne
    @JoinColumn(nullable = false, name = "user_id")
    private User tokenOwner;

    public Token() {
    }

    public Token(String confirmationToken, LocalDateTime creationDate, LocalDateTime expirationDate, User tokenOwner) {
        this.confirmationToken = confirmationToken;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.tokenOwner = tokenOwner;
    }

}
