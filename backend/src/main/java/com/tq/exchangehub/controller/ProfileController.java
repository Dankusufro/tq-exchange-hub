package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.dto.ProfileUpdateRequest;
import com.tq.exchangehub.service.ProfileService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profiles")
@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Get profile details", description = "Fetches the public information for a user profile.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully."),
        @ApiResponse(responseCode = "404", description = "Profile not found."),
        @ApiResponse(responseCode = "400", description = "Invalid profile identifier supplied.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProfileDto> getProfile(@PathVariable UUID id) {
        return ResponseEntity.ok(profileService.getProfile(id));
    }

    @Operation(
            summary = "Update profile",
            description = "Updates profile details for the authenticated owner.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid profile payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot modify this profile."),
        @ApiResponse(responseCode = "404", description = "Profile not found.")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable UUID id, @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(profileService.updateProfile(id, request));
    }
}
