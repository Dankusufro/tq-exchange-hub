package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.AuthResponse;
import com.tq.exchangehub.entity.UserAccount;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthSessionController {

    private final AuthService authService;

    public AuthSessionController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Fetch the authenticated session",
            description = "Returns token information for the authenticated principal.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Session retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "The request was not authenticated.")
    })
    @GetMapping("/session")
    public ResponseEntity<AuthResponse> getSession(@AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserAccount account = principal.getUserAccount();
        AuthResponse response = authService.createSession(account);
        return ResponseEntity.ok(response);
    }

    /**
     * Stateless logout endpoint. The backend does not keep server-side sessions, so the client is
     * responsible for discarding its stored tokens when receiving this response.
     */
    @Operation(
            summary = "Invalidate client tokens",
            description = "Signals the client to discard its stored tokens.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Logout acknowledged."),
        @ApiResponse(responseCode = "401", description = "The request was not authenticated.")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent().build();
    }
}
