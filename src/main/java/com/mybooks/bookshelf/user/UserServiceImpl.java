package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.EmailService;
import com.mybooks.bookshelf.email.token.Token;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.ChangeUserDetailsException;
import com.mybooks.bookshelf.exception.EmailException;
import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.payload.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final String EMAIL_ALREADY_TAKEN_ERROR = "This email is already associated with some account.";
    private static final String TOO_SOON_ERROR = "You can request a new confirmation email in %d minutes and %d seconds.";
    private static final String USER_NOT_FOUND_ERROR = "User not found.";
    private static final String INCORRECT_PASSWORD_MESSAGE = "Incorrect password.";
    private static final String CHANGE_FAILURE_MESSAGE = "Failed to change user details.";
    private static final String EMAIL_CHANGE_SUCCESS_MESSAGE = "Your email has been successfully changed.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JsonWebToken jsonWebToken;

    @Value("${email.confirmation.endpoint}")
    private String emailConfirmationEndpoint;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, TokenService tokenService, EmailService emailService, JsonWebToken jsonWebToken) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailService = emailService;
        this.jsonWebToken = jsonWebToken;
    }


    //    REGISTRATION
    @Override
    public RegisterResponse createUser(RegisterRequest request) {
        String encodedPassword = passwordEncoder.encode(request.password());
        User user = UserMapper.mapToEntity(request, encodedPassword);

        // Check if the email address is taken.
        if (isEmailAlreadyTaken(user.getEmail()))
            throw new EmailException(EMAIL_ALREADY_TAKEN_ERROR);

        // Save the user entity in the DB.
        userRepository.save(user);

        // Create a token and save it in the DB.
        Token token = tokenService.createConfirmationToken(user);

        // Send an email with an account activation token.
        sendConfirmationEmail(token, request.email(), request.nick());

        return new RegisterResponse(user.getNick(), token.getConfirmationToken());
    }

    private boolean isEmailAlreadyTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private void sendConfirmationEmail(Token token, String addressee, String nick) {
        String link = emailConfirmationEndpoint + token.getConfirmationToken();
        emailService.send(addressee, emailService.buildEmail(nick, link));
    }


    //    LOGIN
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR));
    }

    @Override
    public User loadUserById(Long id) throws UsernameNotFoundException {
        return userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND_ERROR));
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        try {
            UserDetails userDetails = loadUserByUsername(request.email());

            String encodedPassword = userDetails.getPassword();

            if (passwordEncoder.matches(request.password(), encodedPassword))
                return new LoginResponse(generateJWT((User) userDetails), true);
            else
                return new LoginResponse(INCORRECT_PASSWORD_MESSAGE, false);
        } catch (UsernameNotFoundException e) {
            return new LoginResponse(USER_NOT_FOUND_ERROR, false);
        }
    }

    private String generateJWT(User user) {
        return jsonWebToken.generateToken(user);
    }


    //    ACCOUNT MANAGEMENT
    @Override
    public boolean isEnabled(String userId) {
        User user = loadUserById(Long.parseLong(userId));
        return user.isEnabled();
    }

    @Override
    public int enableUser(String email) {
        return userRepository.updateEnabled(email);
    }

    @Override
    public void sendNewConfirmationEmail(String userId) {
        User user = loadUserById(Long.parseLong(userId));
        Token latestToken = tokenService.getLatestUserToken(user);

        // If it has been 5 minutes since the last email.
        Duration timeElapsed = Duration.between(latestToken.getCreationDate(), LocalDateTime.now());
        if (timeElapsed.getSeconds() < 300) {
            long minutesLeft = 4 - timeElapsed.toMinutes();
            long secondsLeft = 59 - (timeElapsed.getSeconds() % 60);
            throw new TokenException(String.format(TOO_SOON_ERROR, minutesLeft, secondsLeft), false);
        }

        Token newToken = tokenService.createConfirmationToken(user);
        sendConfirmationEmail(newToken, user.getEmail(), user.getNick());
    }

    @Override
    public ChangeResponse changeUserNick(String userId, ChangeNickRequest request) {
        Long id = Long.parseLong(userId);

        validatePassword(id, request.password());

        if (userRepository.updateNick(id, request.nick()) == 0)
            throw new ChangeUserDetailsException(CHANGE_FAILURE_MESSAGE);

        // Returns JWT because it contains the user's current nick displayed in the navbar.
        return new ChangeResponse(generateJWT(loadUserById(id)));
    }

    @Override
    public ChangeResponse changeUserEmail(String userId, ChangeEmailRequest request) {
        if (isEmailAlreadyTaken(request.email()))
            throw new EmailException(EMAIL_ALREADY_TAKEN_ERROR);

        Long id = Long.parseLong(userId);

        validatePassword(id, request.password());

        if (userRepository.updateEmailAndDisableUser(id, request.email()) == 0)
            throw new ChangeUserDetailsException(CHANGE_FAILURE_MESSAGE);

        return new ChangeResponse(EMAIL_CHANGE_SUCCESS_MESSAGE);
    }

    private void validatePassword(Long id, String providedPassword) {
        String encodedPassword = loadUserById(id).getPassword();
        if (!passwordEncoder.matches(providedPassword, encodedPassword))
            throw new ChangeUserDetailsException(INCORRECT_PASSWORD_MESSAGE);
    }

}
