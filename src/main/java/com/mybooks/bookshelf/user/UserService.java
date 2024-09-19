package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.user.payload.*;
import org.springframework.security.core.userdetails.UserDetails;

public interface UserService {

    RegisterResponse createUser(RegisterRequest request);

    UserDetails loadUserByUsername(String email);

    User loadUserById(Long id);

    LoginResponse loginUser(LoginRequest request);

    boolean isEnabled(String userId);

    int enableUser(String email);

    void sendNewConfirmationEmail(String userId);

    ChangeResponse changeUserNick(String userId, ChangeNickRequest request);

    ChangeResponse changeUserEmail(String userId, ChangeEmailRequest request);

    ChangeResponse changeUserPassword(String userId, ChangePasswordRequest request);

}
