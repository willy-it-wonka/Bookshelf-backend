package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.user.payload.LoginRequest;
import com.mybooks.bookshelf.user.payload.LoginResponse;
import com.mybooks.bookshelf.user.payload.RegisterRequest;
import com.mybooks.bookshelf.user.payload.RegisterResponse;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    RegisterResponse createUser(RegisterRequest request);

    UserDetails loadUserByUsername(String email);

    User loadUserById(Long id);

    LoginResponse loginUser(LoginRequest request);

    boolean isEnabled(String userId);

    int enableUser(String email);

    void sendNewConfirmationEmail(String userId);

}
