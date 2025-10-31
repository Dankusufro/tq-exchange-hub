package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.CreateTradeRequest;
import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.TradeService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Trades")
@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @Operation(
            summary = "Create a trade request",
            description = "Creates a trade between the authenticated user and the owner of the target item.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trade created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid trade payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "404", description = "Referenced item or profile not found.")
    })
    @PostMapping
    public ResponseEntity<TradeDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateTradeRequest request) {
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.create(request, profileId));
    }

    @Operation(
            summary = "List trades",
            description = "Returns the trades involving the authenticated profile, optionally filtered by status.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trades retrieved successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid status filter supplied."),
        @ApiResponse(responseCode = "401", description = "Authentication required.")
    })
    @GetMapping
    public ResponseEntity<List<TradeDto>> listTrades(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(name = "status", required = false) List<String> status) {
        UUID profileId = extractProfileId(principal);

        List<TradeStatus> statuses =
                status == null
                        ? List.of()
                        : status.stream()
                                .filter(Objects::nonNull)
                                .map(String::trim)
                                .filter(s -> !s.isEmpty())
                                .map(s -> parseStatus(s))
                                .distinct()
                                .toList();
        return ResponseEntity.ok(tradeService.listTrades(profileId, statuses));
    }

    @Operation(
            summary = "Get trade details",
            description = "Returns full information for the specified trade.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trade retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user is not part of this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TradeDto> getTrade(@PathVariable UUID id) {
        return ResponseEntity.ok(tradeService.getTrade(id));
    }

    @Operation(
            summary = "Update trade status",
            description = "Changes the status of the trade to the provided value.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trade status updated successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid status transition."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot modify this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @PostMapping("/{id}/status")
    public ResponseEntity<TradeDto> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody TradeStatusUpdateRequest request) {
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.updateStatus(id, request, profileId));
    }

    @Operation(
            summary = "Accept a trade",
            description = "Convenience endpoint that sets the trade status to ACCEPTED.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trade accepted successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot accept this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @PostMapping("/{id}/accept")
    public ResponseEntity<TradeDto> accept(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.ACCEPTED);
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.updateStatus(id, request, profileId));
    }

    @Operation(
            summary = "Reject a trade",
            description = "Convenience endpoint that sets the trade status to REJECTED.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trade rejected successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot reject this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<TradeDto> reject(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.REJECTED);
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.updateStatus(id, request, profileId));
    }

    @Operation(
            summary = "Cancel a trade",
            description = "Convenience endpoint that sets the trade status to CANCELLED.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Trade cancelled successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot cancel this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<TradeDto> cancel(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.cancel(id, profileId));
    }

    private UUID extractProfileId(UserPrincipal principal) {
        if (principal == null || principal.getUserAccount() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return principal.getUserAccount().getProfile().getId();
    }

    private TradeStatus parseStatus(String status) {
        try {
            return TradeStatus.valueOf(status.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unknown trade status: " + status);
        }
    }
}
