package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.NotificationDto;
import com.tq.exchangehub.dto.NotificationReadRequest;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.NotificationService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Notifications")
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Operation(
            summary = "List notifications",
            description = "Returns the notifications for the authenticated profile in reverse chronological order.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Notifications retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
    })
    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(
            @AuthenticationPrincipal UserPrincipal principal) {
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(notificationService.list(profileId));
    }

    @Operation(
            summary = "Mark notifications as read",
            description = "Marks the provided notifications as read for the authenticated profile.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notifications updated successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid request payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
    })
    @PostMapping("/read")
    public ResponseEntity<Void> markAsRead(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody NotificationReadRequest request) {
        UUID profileId = extractProfileId(principal);
        notificationService.markAsRead(profileId, request.getIds());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Marks every notification as read for the authenticated profile.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Notifications updated successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
    })
    @PostMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal UserPrincipal principal) {
        UUID profileId = extractProfileId(principal);
        notificationService.markAllAsRead(profileId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractProfileId(UserPrincipal principal) {
        if (principal == null || principal.getUserAccount() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return principal.getUserAccount().getProfile().getId();
    }
}
