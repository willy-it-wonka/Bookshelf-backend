package com.mybooks.bookshelf.user.token;

import com.mybooks.bookshelf.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenService {

    private static final String TOKEN_NOT_FOUND_ERROR = "Token not found.";
    private static final String EMAIL_ALREADY_CONFIRMED_ERROR = "Email already confirmed.";
    private static final String TOKEN_EXPIRED_ERROR = "Token expired.";
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
    public String confirmToken(String token) {
        // Get token from DB.
        Token confirmationToken = getToken(token).orElseThrow(() -> new IllegalStateException(TOKEN_NOT_FOUND_ERROR));

        if (confirmationToken.getConfirmationDate() != null)
            throw new IllegalStateException(EMAIL_ALREADY_CONFIRMED_ERROR);

        // Check if the token is valid.
        LocalDateTime expirationDate = confirmationToken.getExpirationDate();
        if (expirationDate.isBefore(LocalDateTime.now()))
            throw new IllegalStateException(TOKEN_EXPIRED_ERROR);

        // Update "confirmation_date" in DB in table "tokens".
        setConfirmationDate(token);

        // Update "enabled" in DB in table "users".
        enableUser(confirmationToken.getTokenOwner().getEmail());

        return TOKEN_CONFIRMED_MESSAGE;
    }

    private Optional<Token> getToken(String token) {
        return tokenRepository.findByToken(token);
    }

    // int → returns 0 if no modifications; >0 if updates DB
    private int setConfirmationDate(String token) {
        return tokenRepository.updateConfirmationDate(token, LocalDateTime.now());
    }

    private int enableUser(String email) {
        return userRepository.updateEnabled(email);
    }
}
