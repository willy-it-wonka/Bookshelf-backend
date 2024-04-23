package com.mybooks.bookshelfSB.user.token;

import com.mybooks.bookshelfSB.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Autowired
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
        Token confirmationToken = getToken(token).orElseThrow(() -> new IllegalStateException("Token not found."));

        if (confirmationToken.getConfirmationDate() != null)
            throw new IllegalStateException("Email already confirmed.");

        // Check if the token is valid.
        LocalDateTime expirationDate = confirmationToken.getExpirationDate();
        if (expirationDate.isBefore(LocalDateTime.now()))
            throw new IllegalStateException("Token expired.");

        // Update "confirmation_date" in DB in table "tokens".
        setConfirmationDate(token);

        // Update "enabled" in DB in table "users".
        enableUser(confirmationToken.getTokenOwner().getEmail());

        return "Token confirmed.";
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
