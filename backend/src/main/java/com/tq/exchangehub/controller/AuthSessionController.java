package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.util.DtoMapper;
import com.tq.exchangehub.util.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthSessionController {

    private final JwtTokenProvider tokenProvider;

    public AuthSessionController(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @GetMapping("/session")
    public ResponseEntity<AuthResponse> getSession(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAccount account = principal.getUserAccount();
        ProfileDto profileDto = DtoMapper.toProfileDto(account.getProfile());
        String accessToken = tokenProvider.generateAccessToken(account);
        String refreshToken = tokenProvider.generateRefreshToken(account);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, profileDto));
    }

    /**
     * Stateless logout endpoint. The backend does not keep server-side sessions, so the client is
     * responsible for discarding its stored tokens when receiving this response.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
