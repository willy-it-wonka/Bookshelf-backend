package com.mybooks.bookshelfSB.user;

import com.mybooks.bookshelfSB.exception.EmailIssueException;
import com.mybooks.bookshelfSB.user.email.EmailService;
import com.mybooks.bookshelfSB.user.token.Token;
import com.mybooks.bookshelfSB.user.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
    }

    public String createUser(UserDto userDto) {
        // Check if the email address is correct.
        if(!isEmailValid(userDto.getEmail()))
            throw new EmailIssueException("is invalid");

        User user = new User(
                userDto.getNick(),
                userDto.getEmail(),
                this.passwordEncoder.encode(userDto.getPassword()),
                UserRole.USER);

        // Check if the email address is taken.
        if(userExists(user))
            throw new EmailIssueException("is already associated with some account");

        // Save user entity in the DB.
        userRepository.save(user);

        // Create token and save it in the DB.
        Token token = new Token(createConfirmationToken(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), user);
        tokenService.saveToken(token);

        // Send an email with an account activation token.
        String link = "http://localhost:8080/api/register/confirm?token=" + token.getToken();
        emailService.send(userDto.getEmail(), emailService.buildEmail(userDto.getNick(), link));

        return String.format("nick: %s\ntoken: %s", user.getNick(), token.getToken());
    }

    // Returns true if the email address is already taken.
    private boolean userExists(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent();
    }

    // It checks that the email is correct before registration.
    private boolean isEmailValid(String email) {
        String regex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";
        return email.matches(regex);
    }

    private String createConfirmationToken() {
        return UUID.randomUUID().toString();
    }

}
