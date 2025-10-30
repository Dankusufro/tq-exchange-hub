package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.FavoriteDto;
import com.tq.exchangehub.dto.FavoriteRequest;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Favorites")
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @Operation(
            summary = "List favorites",
            description = "Returns the favorites for the authenticated profile ordered from newest to oldest.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Favorites retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
    })
    @GetMapping
    public ResponseEntity<List<FavoriteDto>> list(@AuthenticationPrincipal UserPrincipal principal) {
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(favoriteService.list(profileId));
    }

    @Operation(
            summary = "Add favorite",
            description = "Marks an item as favorite for the authenticated profile.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Favorite created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid favorite payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "404", description = "Profile or item not found."),
        @ApiResponse(responseCode = "409", description = "Favorite already exists."),
    })
    @PostMapping
    public ResponseEntity<FavoriteDto> add(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FavoriteRequest request) {
        UUID profileId = extractProfileId(principal);
        FavoriteDto favorite = favoriteService.add(profileId, request.getItemId());
        return ResponseEntity.status(HttpStatus.CREATED).body(favorite);
    }

    @Operation(
            summary = "Remove favorite",
            description = "Removes the favorite relationship between the authenticated profile and the specified item.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Favorite removed successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "404", description = "Favorite not found for the authenticated profile."),
    })
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> remove(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID itemId) {
        UUID profileId = extractProfileId(principal);
        favoriteService.remove(profileId, itemId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractProfileId(UserPrincipal principal) {
        if (principal == null || principal.getUserAccount() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return principal.getUserAccount().getProfile().getId();
    }
}
