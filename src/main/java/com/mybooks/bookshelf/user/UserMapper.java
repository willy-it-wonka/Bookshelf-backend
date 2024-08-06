package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.user.payload.RegisterRequest;

public class UserMapper {

    private UserMapper() {
    }

    static User mapToEntity(RegisterRequest request, String encodedPassword) {
        return new User(
                request.nick(),
                request.email(),
                encodedPassword,
                UserRole.USER);
    }

}
