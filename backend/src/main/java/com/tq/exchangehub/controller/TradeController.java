package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.TradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

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

    @GetMapping("/{id}")
    public ResponseEntity<TradeDto> getTrade(@PathVariable UUID id) {
        return ResponseEntity.ok(tradeService.getTrade(id));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<TradeDto> updateStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody TradeStatusUpdateRequest request) {
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.updateStatus(id, request, profileId));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<TradeDto> accept(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.ACCEPTED);
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.updateStatus(id, request, profileId));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TradeDto> reject(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.REJECTED);
        UUID profileId = extractProfileId(principal);
        return ResponseEntity.ok(tradeService.updateStatus(id, request, profileId));
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
