package com.tq.exchangehub.controller;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tq.exchangehub.config.JwtService;
import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.SessionResponse;
import com.tq.exchangehub.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final boolean cookieEnabled;
    private final String cookieName;
    private final long cookieMaxAgeSeconds;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cookieSameSite;

    public AuthController(AuthService authService, JwtService jwtService,
            @Value("${app.security.jwt.cookie-enabled:true}") boolean cookieEnabled,
            @Value("${app.security.jwt.cookie-name:auth_token}") String cookieName,
            @Value("${app.security.jwt.cookie-max-age-seconds:0}") long cookieMaxAgeSeconds,
            @Value("${app.security.jwt.cookie-secure:false}") boolean cookieSecure,
            @Value("${app.security.jwt.cookie-domain:}") String cookieDomain,
            @Value("${app.security.jwt.cookie-same-site:Lax}") String cookieSameSite) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.cookieEnabled = cookieEnabled;
        this.cookieName = cookieName;
        this.cookieMaxAgeSeconds = cookieMaxAgeSeconds;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return buildAuthResponse(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return buildAuthResponse(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (cookieEnabled) {
            builder.header(HttpHeaders.SET_COOKIE, buildExpiryCookie().toString());
        }
        return builder.body(Map.of("message", "Logged out"));
    }

    @GetMapping("/session")
    public ResponseEntity<SessionResponse> session() {
        SessionResponse response = authService.getCurrentSession();
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<AuthResponse> buildAuthResponse(AuthResponse response) {
        if (!cookieEnabled) {
            return ResponseEntity.ok(response);
        }

        ResponseCookie authCookie = buildTokenCookie(response.token());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authCookie.toString())
                .body(response);
    }

    private ResponseCookie buildTokenCookie(String token) {
        Duration maxAge = cookieMaxAgeSeconds > 0
                ? Duration.ofSeconds(cookieMaxAgeSeconds)
                : Duration.ofMillis(jwtService.getExpirationMillis());

        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(maxAge);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }

    private ResponseCookie buildExpiryCookie() {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .sameSite(cookieSameSite)
                .maxAge(Duration.ZERO);

        if (cookieDomain != null && !cookieDomain.isBlank()) {
            builder.domain(cookieDomain);
        }

        return builder.build();
    }
}
