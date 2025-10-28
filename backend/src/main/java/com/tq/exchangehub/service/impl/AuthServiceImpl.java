package com.tq.exchangehub.service.impl;

import java.security.Principal;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.SessionResponse;
import com.tq.exchangehub.dto.UserDto;
import com.tq.exchangehub.entity.AppUser;
import com.tq.exchangehub.config.JwtService;
import com.tq.exchangehub.exception.InvalidCredentialsException;
import com.tq.exchangehub.repository.AppUserRepository;
import com.tq.exchangehub.service.AuthService;

@Service
public class AuthServiceImpl implements AuthService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthServiceImpl(AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        AppUser user = new AppUser();
        user.setEmail(normalizedEmail);
        user.setDisplayName(request.displayName().trim());
        user.setPassword(passwordEncoder.encode(request.password()));

        AppUser savedUser = userRepository.save(user);
        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(token, toDto(savedUser));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtService.generateToken(normalizedEmail);
            AppUser user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return new AuthResponse(token, toDto(user));
        } catch (AuthenticationException ex) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
    }

    @Override
    public SessionResponse getCurrentSession() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return new SessionResponse(null);
        }

        String email = extractEmail(authentication.getPrincipal());
        return userRepository.findByEmail(email)
                .map(this::toDto)
                .map(SessionResponse::new)
                .orElse(new SessionResponse(null));
    }

    private String extractEmail(Object principal) {
        if (principal instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            return userDetails.getUsername();
        }
        if (principal instanceof Principal p) {
            return p.getName();
        }
        return principal.toString();
    }

    private UserDto toDto(AppUser user) {
        return new UserDto(user.getId(), user.getEmail(), user.getDisplayName());
    }
}
