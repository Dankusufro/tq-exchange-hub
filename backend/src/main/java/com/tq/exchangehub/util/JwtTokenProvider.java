package com.tq.exchangehub.util;

import com.tq.exchangehub.config.JwtProperties;
import com.tq.exchangehub.entity.UserAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class JwtTokenProvider {

    private final JwtProperties properties;
    private Key key;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void init() {
        if (!StringUtils.hasText(properties.getSecret())) {
            throw new IllegalStateException(
                    "application.security.jwt.secret debe configurarse mediante la variable de entorno JWT_SECRET");
        }
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public TokenPair generateTokenPair(UserAccount account) {
        return new TokenPair(generateAccessToken(account), generateRefreshToken(account));
    }

    public TokenPair rotateTokens(UserAccount account) {
        return generateTokenPair(account);
    }

    public String generateAccessToken(UserAccount account) {
        return buildToken(account.getEmail(), properties.getAccessTokenExpiration(), "access");
    }

    public String generateRefreshToken(UserAccount account) {
        return buildToken(account.getEmail(), properties.getRefreshTokenExpiration(), "refresh");
    }

    public long getAccessTokenExpirationMillis() {
        return properties.getAccessTokenExpiration();
    }

    public long getRefreshTokenExpirationMillis() {
        return properties.getRefreshTokenExpiration();
    }

    private String buildToken(String email, long expirationMillis, String type) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(email)
                .claim("type", type)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMillis)))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateAccessToken(String token) {
        return validateToken(token, "access");
    }

    public String extractEmailFromRefreshToken(String token) {
        Jws<Claims> parsed = parseToken(token);
        if (!"refresh".equals(parsed.getBody().get("type", String.class))) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        return parsed.getBody().getSubject();
    }

    public String extractEmail(String token) {
        Jws<Claims> parsed = parseToken(token);
        return parsed.getBody().getSubject();
    }

    private boolean validateToken(String token, String expectedType) {
        Jws<Claims> parsed = parseToken(token);
        String type = parsed.getBody().get("type", String.class);
        if (!expectedType.equals(type)) {
            throw new IllegalArgumentException("Invalid token type");
        }
        return true;
    }

    private Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token: " + ex.getMessage());
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}
