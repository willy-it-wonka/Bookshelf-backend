package com.mybooks.bookshelf.user;

import com.mybooks.bookshelf.email.EmailService;
import com.mybooks.bookshelf.email.token.Token;
import com.mybooks.bookshelf.email.token.TokenService;
import com.mybooks.bookshelf.exception.*;
import com.mybooks.bookshelf.security.JsonWebToken;
import com.mybooks.bookshelf.user.payload.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {

    private static final String EMAIL_ALREADY_TAKEN_ERROR = "This email is already associated with some account.";
    private static final String TOO_SOON_ERROR = "You can request a new confirmation email in %d minutes and %d seconds.";
    private static final String CHANGE_FAILURE_MESSAGE = "Failed to change user details.";
    private static final String EMAIL_CHANGE_SUCCESS_MESSAGE = "Your email has been successfully changed.";
    private static final String PASSWORD_CHANGE_SUCCESS_MESSAGE = "Your password has been successfully changed.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final JsonWebToken jsonWebToken;

    @Value("${email.confirmation.endpoint}")
    private String emailConfirmationEndpoint;
    @Value("${email.confirmation.path}")
    private String emailConfirmationPath;
    @Value("${password.reset.endpoint}")
    private String passwordResetEndpoint;
    @Value("${password.reset.path}")
    private String passwordResetPath;

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

        saveUserToDatabase(user);

        Token token = tokenService.createConfirmationToken(user);
        sendConfirmationEmail(token, request.email(), request.nick());

        return new RegisterResponse(user.getNick(), token.getConfirmationToken());
    }

    private void saveUserToDatabase(User user) {
        if (isEmailAlreadyTaken(user.getEmail()))
            throw new EmailException(EMAIL_ALREADY_TAKEN_ERROR);
        userRepository.save(user);
    }

    private boolean isEmailAlreadyTaken(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private void sendConfirmationEmail(Token token, String addressee, String nick) {
        String link = emailConfirmationEndpoint + token.getConfirmationToken();
        emailService.sendConfirmationEmail(addressee, emailService.buildEmail(emailConfirmationPath, nick, link));
    }


    //    LOGIN
    @Override
    public UserDetails loadUserByUsername(String email) throws UserNotFoundException {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User loadUserById(Long id) throws UserNotFoundException {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public LoginResponse loginUser(LoginRequest request) {
        UserDetails userDetails = loadUserByUsername(request.email());
        String encodedPassword = userDetails.getPassword();

        if (!passwordEncoder.matches(request.password(), encodedPassword))
            throw new IncorrectPasswordException();

        return new LoginResponse(generateJWT((User) userDetails), true);
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

        validateEmailResendTime(tokenService.getLatestUserToken(user));

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

    @Override
    public ChangeResponse changeUserPassword(String userId, ChangePasswordRequest request) {
        Long id = Long.parseLong(userId);

        validatePassword(id, request.currentPassword());

        String encodedNewPassword = passwordEncoder.encode(request.newPassword());
        if (userRepository.updatePassword(id, encodedNewPassword) == 0)
            throw new ChangeUserDetailsException(CHANGE_FAILURE_MESSAGE);

        return new ChangeResponse(PASSWORD_CHANGE_SUCCESS_MESSAGE);
    }

    @Override
    public String initiateForgottenPasswordReset(InitiateResetPasswordRequest request) {
        return null;
    }

    @Override
    public String resetForgottenPassword(ResetPasswordRequest request) {
        return null;
    }

    private void validateEmailResendTime(Token latestToken) {
        // If it has been 5 minutes since the last email.
        Duration timeElapsed = Duration.between(latestToken.getCreationDate(), LocalDateTime.now());
        if (timeElapsed.getSeconds() < 300) {
            long minutesLeft = 4 - timeElapsed.toMinutes();
            long secondsLeft = 59 - (timeElapsed.getSeconds() % 60);
            throw new TokenException(String.format(TOO_SOON_ERROR, minutesLeft, secondsLeft));
        }
    }

    private void validatePassword(Long id, String providedPassword) {
        String encodedPassword = loadUserById(id).getPassword();
        if (!passwordEncoder.matches(providedPassword, encodedPassword))
            throw new IncorrectPasswordException();
    }

}
