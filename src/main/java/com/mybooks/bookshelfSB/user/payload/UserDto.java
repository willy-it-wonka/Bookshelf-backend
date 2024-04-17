package com.mybooks.bookshelfSB.user.payload;

import lombok.Getter;

// Data Transfer Object is used to transfer data between different layers of an application.
@Getter
public class UserDto {
    private final String nick;
    private final String email;
    private final String password;

    public UserDto(String nick, String email, String password) {
        this.nick = nick;
        this.email = email;
        this.password = password;
    }
}
