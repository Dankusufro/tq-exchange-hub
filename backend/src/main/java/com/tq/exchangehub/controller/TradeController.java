package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.service.TradeService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    private final TradeService tradeService;

    public TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @GetMapping
    public ResponseEntity<List<TradeDto>> listTrades(
            @RequestParam(name = "status", required = false) String status) {
        Optional<TradeStatus> statusFilter = Optional.empty();
        if (status != null && !status.isBlank()) {
            try {
                statusFilter = Optional.of(TradeStatus.valueOf(status.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("Unknown trade status: " + status);
            }
        }
        return ResponseEntity.ok(tradeService.listTrades(statusFilter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TradeDto> getTrade(@PathVariable UUID id) {
        return ResponseEntity.ok(tradeService.getTrade(id));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<TradeDto> updateStatus(
            @PathVariable UUID id, @Valid @RequestBody TradeStatusUpdateRequest request) {
        return ResponseEntity.ok(tradeService.updateStatus(id, request));
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<TradeDto> accept(@PathVariable UUID id) {
        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.ACCEPTED);
        return ResponseEntity.ok(tradeService.updateStatus(id, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<TradeDto> reject(@PathVariable UUID id) {
        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.REJECTED);
        return ResponseEntity.ok(tradeService.updateStatus(id, request));
    }
}
