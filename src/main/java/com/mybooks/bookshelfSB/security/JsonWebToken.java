package com.mybooks.bookshelfSB.security;

import com.mybooks.bookshelfSB.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JsonWebToken {

    private static final String SECRET_KEY = "93721485036254182059368741293054786147565149476321988498446591465";
    private static final long EXPIRATION = 259_200_000; // 3 days

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
        return createJWT(extraClaims, user, EXPIRATION);
    }

    private String createJWT(Map<String, Object> claims, User user, long expiration) {
        JwtBuilder jwtBuilder = Jwts.builder()
                .claims(claims)
                .claim("nick", user.getNick())
                .subject(String.valueOf(user.getId()))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(generateSigningKey());
        return jwtBuilder.compact();
    }

    // Generates an HMAC signature key based on the "secretKey". The key is decoded from Base64
    // format and used for signing JWT tokens to ensure their authenticity.
    private Key generateSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
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
        return Jwts
                .parser()
                .verifyWith((SecretKey) generateSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}