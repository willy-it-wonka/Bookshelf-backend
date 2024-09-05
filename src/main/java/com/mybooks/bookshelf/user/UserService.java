package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.EmailService;
import com.mybooks.bookshelf.email.token.Token;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.payload.LoginRequest;
import com.mybooks.bookshelf.user.payload.LoginResponse;
import com.mybooks.bookshelf.user.payload.RegisterRequest;
import com.mybooks.bookshelf.user.payload.RegisterResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {

    private static final String EMAIL_ALREADY_EXISTS_ERROR = "is already associated with some account";
    private static final String USER_NOT_FOUND_ERROR = "User not found.";
    private static final String INCORRECT_PASSWORD_MESSAGE = "Incorrect password.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JsonWebToken jsonWebToken;

    @Value("${email.confirmation.endpoint}")
    private String emailConfirmationEndpoint;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService, JsonWebToken jsonWebToken) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.jsonWebToken = jsonWebToken;
    }


    /*
     *    REGISTRATION
     */

    RegisterResponse createUser(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = UserMapper.mapToEntity(request, encodedPassword);

        // Check if the email address is taken.
        if (userExists(user))
            throw new EmailException(EMAIL_ALREADY_EXISTS_ERROR);

        // Save user entity in the DB.
        userRepository.save(user);

        // Create token and save it in the DB.
        Token token = createConfirmationToken(user);

        // Send an email with an account activation token.
        sendConfirmationEmail(token, request.email(), request.nick());

        return new RegisterResponse(user.getNick(), token.getConfirmationToken());
    }

    private boolean userExists(User user) {
        return userRepository.findByEmail(user.getEmail()).isPresent();
    }

    private String createUniversallyUniqueId() {
        return UUID.randomUUID().toString();
    }

    private Token createConfirmationToken(User user) {
        Token token = new Token(createUniversallyUniqueId(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), user);
        tokenService.saveToken(token);
        return token;
    }

    private void sendConfirmationEmail(Token token, String addressee, String nick) {
        String link = emailConfirmationEndpoint + token.getConfirmationToken();
        emailService.send(addressee, emailService.buildEmail(nick, link));
    }

    void sendNewConfirmationEmail(String userId) {
        User user = loadUserById(Long.parseLong(userId));
        Token token = createConfirmationToken(user);
        sendConfirmationEmail(token, user.getEmail(), user.getNick());
    }


    /*
     *    LOGGING IN
     */

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR));
    }

    public User loadUserById(Long id) throws UsernameNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR));
    }

    LoginResponse loginUser(LoginRequest request) {
        try {
            UserDetails userDetails = loadUserByUsername(request.email());

            String encodedPassword = userDetails.getPassword();

            if (passwordEncoder.matches(request.password(), encodedPassword))
                return new LoginResponse(jsonWebToken.generateToken((User) userDetails), true);
            else
                return new LoginResponse(INCORRECT_PASSWORD_MESSAGE, false);
        } catch (UsernameNotFoundException e) {
            return new LoginResponse(USER_NOT_FOUND_ERROR, false);
        }
    }

    boolean isEnabled(String userId) {
        User user = loadUserById(Long.parseLong(userId));
        return user.isEnabled();
    }

}
