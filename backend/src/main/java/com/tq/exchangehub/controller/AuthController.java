package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.dto.ForgotPasswordRequest;
import com.tq.exchangehub.dto.LoginRequest;
import com.tq.exchangehub.dto.RefreshTokenRequest;
import com.tq.exchangehub.dto.RegisterRequest;
import com.tq.exchangehub.dto.ResetPasswordRequest;
import com.tq.exchangehub.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Register a new user",
            description = "Creates a user account and returns an authentication token pair.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Registration completed successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid registration payload."),
        @ApiResponse(responseCode = "409", description = "An account with the provided email already exists.")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @Operation(
            summary = "Authenticate with email and password",
            description = "Validates the user credentials and issues a fresh token pair.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authentication succeeded."),
        @ApiResponse(responseCode = "400", description = "Invalid login payload."),
        @ApiResponse(responseCode = "401", description = "Authentication failed.")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(
            summary = "Refresh an access token",
            description = "Exchanges a valid refresh token for a new access token pair.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid refresh token payload."),
        @ApiResponse(responseCode = "401", description = "Refresh token is invalid or expired.")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(
            summary = "Request a password reset",
            description = "Sends a password reset email to the user if the account exists.")
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Reset instructions were queued for delivery."),
        @ApiResponse(responseCode = "400", description = "Invalid request payload.")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.accepted().build();
    }

    @Operation(
            summary = "Reset a password",
            description = "Updates the account password using a previously issued reset token.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password reset successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid reset payload."),
        @ApiResponse(responseCode = "404", description = "Reset token not found or expired.")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.noContent().build();
    }
}
