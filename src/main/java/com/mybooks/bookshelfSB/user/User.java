package com.mybooks.bookshelfSB.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nick;
    private String email;
    private String password;

    public User() {}

    public User(Long id, String nick, String email, String password) {
        this.id = id;
        this.nick = nick;
        this.email = email;
        this.password = password;
    }
}
