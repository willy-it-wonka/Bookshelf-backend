package com.mybooks.bookshelfSB.user;

import com.mybooks.bookshelfSB.exception.EmailIssueException;
import com.mybooks.bookshelfSB.user.token.Token;
import com.mybooks.bookshelfSB.user.token.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
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

        return String.format("nick: %s\ntoken: %s", user.getNick(), token.getToken());
    }

    // Returns true if the email address is already taken.
    public boolean userExists(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent();
    }

    // It checks that the email is correct before registration.
    public boolean isEmailValid(String email) {
        String regex = "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}";
        return email.matches(regex);
    }

    public String createConfirmationToken() {
        return UUID.randomUUID().toString();
    }

    @Transactional
    public String confirmToken(String token) {
        // Get token from DB.
        Token confirmationToken = tokenService.getToken(token)
                .orElseThrow(() -> new IllegalStateException("Token not found."));

        if(confirmationToken.getConfirmationDate() != null)
            throw new IllegalStateException("Email already confirmed.");

        // Check if the token is valid.
        LocalDateTime expirationDate = confirmationToken.getExpirationDate();
        if(expirationDate.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("Token expired.");

        // Update "confirmation_date" in DB in table "tokens".
        tokenService.setConfirmationDate(token);

        // Update "enabled" in DB in table "users".
        enableUser(confirmationToken.getUser().getEmail());

        return "Token confirmed.";
    }

    // int → returns 0 if no modifications; >0 if updates DB
    public int enableUser(String email) {
        return userRepository.updateEnabled(email);
    }
}
