package com.mybooks.bookshelf.user.token;

import com.mybooks.bookshelf.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tokens")
@Getter
@Setter
public class Token {   // Tokens used to confirm an email account.

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
    private User tokenOwner;

    public Token() {
    }

    public Token(String token, LocalDateTime creationDate, LocalDateTime expirationDate, User tokenOwner) {
        this.token = token;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.tokenOwner = tokenOwner;
    }

}
