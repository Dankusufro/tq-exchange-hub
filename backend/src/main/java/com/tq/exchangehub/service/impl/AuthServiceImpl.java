package com.tq.exchangehub.service.impl;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.dto.RefreshTokenRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import com.tq.exchangehub.service.AuthService;
import com.tq.exchangehub.util.DtoMapper;
import com.tq.exchangehub.util.JwtTokenProvider;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserAccountRepository userAccountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    public AuthServiceImpl(
            UserAccountRepository userAccountRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider) {
        this.userAccountRepository = userAccountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        userAccountRepository
                .findByEmailIgnoreCase(request.getEmail())
                .ifPresent(account -> {
                    throw new IllegalArgumentException("Email already registered");
                });

        Profile profile = new Profile();
        profile.setDisplayName(request.getDisplayName());
        profile.setLocation(request.getLocation());
        profile.setPhone(request.getPhone());
        profile.setCreatedAt(OffsetDateTime.now());
        profile.setUpdatedAt(OffsetDateTime.now());
        Profile savedProfile = profileRepository.save(profile);

        UserAccount account = new UserAccount();
        account.setEmail(request.getEmail().toLowerCase());
        account.setPassword(passwordEncoder.encode(request.getPassword()));
        account.setProfile(savedProfile);
        account.setCreatedAt(OffsetDateTime.now());
        userAccountRepository.save(account);

        ProfileDto profileDto = DtoMapper.toProfileDto(savedProfile);
        String accessToken = tokenProvider.generateAccessToken(account);
        String refreshToken = tokenProvider.generateRefreshToken(account);
        return new AuthResponse(accessToken, refreshToken, profileDto);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        UserAccount account =
                userAccountRepository
                        .findByEmailIgnoreCase(request.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        ProfileDto profileDto = DtoMapper.toProfileDto(account.getProfile());
        String accessToken = tokenProvider.generateAccessToken(account);
        String refreshToken = tokenProvider.generateRefreshToken(account);
        return new AuthResponse(accessToken, refreshToken, profileDto);
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!StringUtils.hasText(request.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token must not be empty");
        }

        String email = tokenProvider.extractEmailFromRefreshToken(request.getRefreshToken());
        UserAccount account =
                userAccountRepository
                        .findByEmailIgnoreCase(email)
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String accessToken = tokenProvider.generateAccessToken(account);
        String refreshToken = tokenProvider.generateRefreshToken(account);
        return new AuthResponse(accessToken, refreshToken, DtoMapper.toProfileDto(account.getProfile()));
    }
}
