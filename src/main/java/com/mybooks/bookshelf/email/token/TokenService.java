package com.mybooks.bookshelf.email.token;

import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.user.User;
import com.mybooks.bookshelf.user.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenService {

    private static final String TOKEN_NOT_FOUND_ERROR = "Token not found.";
    private static final String EMAIL_ALREADY_CONFIRMED_ERROR = "Email already confirmed.";
    private static final String TOKEN_EXPIRED_ERROR = "Token expired.";
    private static final String DATE_CONFIRMATION_ERROR = "Failed to update confirmation_date in the database.";
    private static final String USER_ACTIVATION_ERROR = "Failed to activate email (update enabled in the database).";
    private static final String TOKEN_USED_ERROR = "Token used. Request a new email with a link to reset your password.";

    private final TokenRepository tokenRepository;
    private final UserService userService;

    public TokenService(TokenRepository tokenRepository, @Lazy UserService userService) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
    }

    public Token createConfirmationToken(User user) {
        Token token = new Token(generateUUID(), LocalDateTime.now(), LocalDateTime.now().plusMinutes(30), user);
        return tokenRepository.save(token);
    }

    @Transactional
    public RedirectView confirmAccountActivationToken(String confirmationToken) {
        try {
            Token token = getToken(confirmationToken);

            if (token.getConfirmationDate() != null)
                throw new TokenException(EMAIL_ALREADY_CONFIRMED_ERROR);

            if (token.getExpirationDate().isBefore(LocalDateTime.now()))
                throw new TokenException(TOKEN_EXPIRED_ERROR);

            // Update “confirmation_date” in the “tokens” table of the database.
            if (setConfirmationDate(confirmationToken) == 0)
                throw new TokenException(DATE_CONFIRMATION_ERROR);

            // Update “enabled” in the “users” table of the database.
            if (userService.enableUser(token.getTokenOwner().getEmail()) == 0)
                throw new TokenException(USER_ACTIVATION_ERROR);

            return new RedirectView("/confirmation-success.html");
        } catch (TokenException e) {
            return new RedirectView("/confirmation-error.html?error=" + e.getMessage());
        }
    }

    @Transactional
    public Token confirmPasswordResetToken(String confirmationToken) {
        Token token = getToken(confirmationToken);

        if (token.getConfirmationDate() != null)
            throw new TokenException(TOKEN_USED_ERROR);

        if (token.getExpirationDate().isBefore(LocalDateTime.now()))
            throw new TokenException(TOKEN_EXPIRED_ERROR);

        if (setConfirmationDate(confirmationToken) == 0)
            throw new TokenException(DATE_CONFIRMATION_ERROR);

        return token;
    }

    public Token getLatestUserToken(User user) {
        return tokenRepository.findTop1ByTokenOwnerOrderByCreationDateDesc(user)
                .orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND_ERROR));
    }

    private Token getToken(String confirmationToken) {
        return tokenRepository.findByConfirmationToken(confirmationToken)
                .orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND_ERROR));
    }

    private int setConfirmationDate(String token) {
        return tokenRepository.updateConfirmationDate(token, LocalDateTime.now());
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

}
