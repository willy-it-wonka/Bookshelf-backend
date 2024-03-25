package com.mybooks.bookshelfSB.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {
    private String message;
    private Boolean status;

    public LoginResponse() {
    }

    public LoginResponse(String message, Boolean status) {
        this.message = message;
        this.status = status;
    }
}
