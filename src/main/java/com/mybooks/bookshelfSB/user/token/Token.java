package com.mybooks.bookshelfSB.user.token;

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

    public Token() {
    }

    public Token(String token, LocalDateTime creationDate, LocalDateTime confirmationDate, LocalDateTime expirationDate) {
        this.token = token;
        this.creationDate = creationDate;
        this.confirmationDate = confirmationDate;
        this.expirationDate = expirationDate;
    }
}
