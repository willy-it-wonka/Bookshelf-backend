package com.mybooks.bookshelf.security;

import com.mybooks.bookshelf.exception.JwtAuthenticationException;
import com.mybooks.bookshelf.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JsonWebToken {

    private static final String NICK_CLAIM = "nick";
    private static final String EXPIRED_SESSION_ERROR = "Your session has expired. Log in again.";
    private static final String INVALID_JWT_ERROR = "JWT token is invalid.";

    @Value("${security.jwt.secret}")
    private String secretKey;
    @Value("${security.jwt.expiration}")
    private long expiration;

    public String generateToken(User user) {
        return generateToken(new HashMap<>(), user);
    }

    boolean isTokenValid(String token, User user) {
        final String userId = extractUserId(token);
        return (userId.equals(String.valueOf(user.getId()))) && !isTokenExpired(token);
    }

    String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private String generateToken(Map<String, Object> extraClaims, User user) {
        extraClaims.put(NICK_CLAIM, user.getNick());
        return createJWT(extraClaims, user, expiration);
    }

    private String createJWT(Map<String, Object> claims, User user, long expiration) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(generateSigningKey());
        return jwtBuilder.compact();
    }

    // Generates an HMAC signature key based on the "secretKey". The key is decoded from Base64
    // format and used for signing JWT tokens to ensure their authenticity.
    private Key generateSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith((SecretKey) generateSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new JwtAuthenticationException(EXPIRED_SESSION_ERROR, e);
        } catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException(INVALID_JWT_ERROR, e);
        }
    }

}