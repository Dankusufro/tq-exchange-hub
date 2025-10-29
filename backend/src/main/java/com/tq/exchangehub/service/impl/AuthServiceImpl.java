package com.tq.exchangehub.service.impl;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.ForgotPasswordRequest;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.dto.RefreshTokenRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.ResetPasswordRequest;
import com.tq.exchangehub.entity.PasswordResetToken;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.RefreshToken;
import com.tq.exchangehub.entity.Role;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.repository.PasswordResetTokenRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.RefreshTokenRepository;
import com.tq.exchangehub.repository.UserAccountRepository;
import com.tq.exchangehub.service.AuthService;
import com.tq.exchangehub.util.DtoMapper;
import com.tq.exchangehub.util.JwtTokenProvider;
import com.tq.exchangehub.util.JwtTokenProvider.TokenPair;
import com.tq.exchangehub.util.PasswordStrengthValidator;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.MailException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserAccountRepository userAccountRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JavaMailSender mailSender;
    private final PasswordStrengthValidator passwordStrengthValidator;
    private final long passwordResetTokenExpiration;

    public AuthServiceImpl(
            UserAccountRepository userAccountRepository,
            ProfileRepository profileRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenProvider tokenProvider,
            RefreshTokenRepository refreshTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JavaMailSender mailSender,
            PasswordStrengthValidator passwordStrengthValidator,
            @Value("${application.security.password-reset-token-expiration:900000}")
                    long passwordResetTokenExpiration) {
        this.userAccountRepository = userAccountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.mailSender = mailSender;
        this.passwordStrengthValidator = passwordStrengthValidator;
        this.passwordResetTokenExpiration = passwordResetTokenExpiration;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        passwordStrengthValidator.validate(request.getPassword());

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
        account.setRole(Role.USER);
        userAccountRepository.save(account);
        savedProfile.setAccount(account);
        profileRepository.save(savedProfile);

        ProfileDto profileDto = DtoMapper.toProfileDto(savedProfile);
        return issueTokenPair(account, profileDto);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        UserAccount account =
                userAccountRepository
                        .findByEmailIgnoreCase(request.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        ProfileDto profileDto = DtoMapper.toProfileDto(account.getProfile());
        refreshTokenRepository.revokeAllForUser(account);
        refreshTokenRepository.deleteExpiredOrRevoked();
        return issueTokenPair(account, profileDto);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!StringUtils.hasText(request.getRefreshToken())) {
            throw new IllegalArgumentException("Refresh token must not be empty");
        }

        RefreshToken refreshToken =
                refreshTokenRepository
                        .findByToken(request.getRefreshToken())
                        .orElseThrow(() -> new IllegalArgumentException("Refresh token inválido"));

        if (refreshToken.isRevoked() || refreshToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token inválido o expirado");
        }

        String email = tokenProvider.extractEmailFromRefreshToken(request.getRefreshToken());
        UserAccount account = refreshToken.getUserAccount();

        if (!account.getEmail().equalsIgnoreCase(email)) {
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException("Refresh token inválido");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        refreshTokenRepository.deleteExpiredOrRevoked();

        ProfileDto profileDto = DtoMapper.toProfileDto(account.getProfile());
        return issueTokenPair(account, profileDto);
    }

    @Override
    @Transactional
    public AuthResponse createSession(UserAccount account) {
        ProfileDto profileDto = DtoMapper.toProfileDto(account.getProfile());
        refreshTokenRepository.revokeAllForUser(account);
        refreshTokenRepository.deleteExpiredOrRevoked();
        return issueTokenPair(account, profileDto);
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        userAccountRepository
                .findByEmailIgnoreCase(request.getEmail())
                .ifPresent(this::createAndSendPasswordResetToken);
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        passwordStrengthValidator.validate(request.getNewPassword());

        PasswordResetToken resetToken =
                passwordResetTokenRepository
                        .findByToken(request.getToken())
                        .orElseThrow(() -> new IllegalArgumentException("Token de restablecimiento inválido"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            passwordResetTokenRepository.delete(resetToken);
            throw new IllegalArgumentException("Token de restablecimiento inválido o expirado");
        }

        UserAccount account = resetToken.getUserAccount();
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userAccountRepository.save(account);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        passwordResetTokenRepository.deleteAllForUser(account);
        refreshTokenRepository.revokeAllForUser(account);
        refreshTokenRepository.deleteExpiredOrRevoked();
    }

    private AuthResponse issueTokenPair(UserAccount account, ProfileDto profileDto) {
        TokenPair pair = tokenProvider.rotateTokens(account);
        persistRefreshToken(account, pair.refreshToken());
        return new AuthResponse(pair.accessToken(), pair.refreshToken(), profileDto);
    }

    private void persistRefreshToken(UserAccount account, String refreshToken) {
        RefreshToken token = new RefreshToken();
        token.setToken(refreshToken);
        token.setUserAccount(account);
        token.setExpiresAt(OffsetDateTime.now().plusMillis(tokenProvider.getRefreshTokenExpirationMillis()));
        refreshTokenRepository.save(token);
    }

    private void createAndSendPasswordResetToken(UserAccount account) {
        passwordResetTokenRepository.deleteAllForUser(account);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(UUID.randomUUID().toString());
        resetToken.setUserAccount(account);
        resetToken.setExpiresAt(OffsetDateTime.now().plusMillis(passwordResetTokenExpiration));
        passwordResetTokenRepository.save(resetToken);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(account.getEmail());
        message.setSubject("Recuperación de contraseña - TruequePlus");
        message.setText("Hola,\n\nUtiliza el siguiente token para restablecer tu contraseña: "
                + resetToken.getToken()
                + "\n\nSi no solicitaste este cambio, ignora este correo.\n");
        try {
            mailSender.send(message);
        } catch (MailException ex) {
            log.warn("No se pudo enviar el correo de restablecimiento de contraseña: {}", ex.getMessage());
        }
    }
}
