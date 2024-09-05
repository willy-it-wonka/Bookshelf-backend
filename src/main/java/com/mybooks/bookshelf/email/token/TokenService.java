package com.mybooks.bookshelf.email.token;

import com.mybooks.bookshelf.exception.TokenException;
import com.mybooks.bookshelf.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenService {

    private static final String TOKEN_NOT_FOUND_ERROR = "token not found.";
    private static final String EMAIL_ALREADY_CONFIRMED_ERROR = "email already confirmed.";
    private static final String TOKEN_EXPIRED_ERROR = "token expired.";
    private static final String CONFIRM_DATE_ERROR = "failed to update confirmation_date in the database.";
    private static final String ENABLE_USER_ERROR = "failed to activate email (update enabled in the database).";
    private static final String TOKEN_CONFIRMED_MESSAGE = "Token confirmed.";

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public TokenService(TokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    public void saveToken(Token token) {
        tokenRepository.save(token);
    }

    @Transactional
    public String confirmToken(String confirmationToken) {
        Token token = getToken(confirmationToken);

        if (token.getConfirmationDate() != null)
            throw new TokenException(EMAIL_ALREADY_CONFIRMED_ERROR);

        LocalDateTime expirationDate = token.getExpirationDate();
        if (expirationDate.isBefore(LocalDateTime.now()))
            throw new TokenException(TOKEN_EXPIRED_ERROR);

        // Update “confirmation_date” in the “tokens” table of the database.
        if (setConfirmationDate(confirmationToken) == 0)
            throw new TokenException(CONFIRM_DATE_ERROR);

        // Update “enabled” in the “users” table of the database.
        if (enableUser(token.getTokenOwner().getEmail()) == 0)
            throw new TokenException(ENABLE_USER_ERROR);

        return TOKEN_CONFIRMED_MESSAGE;
    }

    // Get the full Token entity by confirmationToken, throw TokenException if not found in the database.
    private Token getToken(String confirmationToken) {
        return tokenRepository.findByConfirmationToken(confirmationToken)
                .orElseThrow(() -> new TokenException(TOKEN_NOT_FOUND_ERROR));
    }

    // int → returns 0 if no modifications; >0 if the database has been updated.
    private int setConfirmationDate(String token) {
        return tokenRepository.updateConfirmationDate(token, LocalDateTime.now());
    }

    private int enableUser(String email) {
        return userRepository.updateEnabled(email);
    }

}
