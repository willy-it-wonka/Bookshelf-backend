package com.mybooks.bookshelfSB.user.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    @Autowired
    public TokenService(TokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void saveToken(Token token) {
        tokenRepository.save(token);
    }

    public Optional<Token> getToken(String token) {
        return tokenRepository.findByToken(token);
    }

    // int → returns 0 if no modifications; >0 if updates DB
    public int setConfirmationDate(String token) {
        return tokenRepository.updateConfirmationDate(token, LocalDateTime.now());
    }
}
