package com.tq.exchangehub.controller;

import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Trade receipts")
@RestController
@RequestMapping("/api/trades")
public class TradeReceiptController {

    private final ReceiptService receiptService;

    public TradeReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @Operation(
            summary = "Download a trade receipt",
            description = "Returns the PDF receipt for the accepted trade if the authenticated user is a participant.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Receipt generated successfully."),
        @ApiResponse(responseCode = "400", description = "The trade is not in an accepted status."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user is not part of this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found."),
        @ApiResponse(responseCode = "500", description = "The receipt could not be generated."),
    })
    @GetMapping("/{id}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        UUID profileId = extractProfileId(principal);
        byte[] pdf = receiptService.generateReceipt(id, profileId);
        String filename = "comprobante-trueque-" + id + ".pdf";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @Operation(
            summary = "Email a trade receipt",
            description = "Sends the receipt PDF to the participants if mail delivery is configured.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "202", description = "Receipt email accepted."),
        @ApiResponse(responseCode = "400", description = "The trade is not in an accepted status."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user is not part of this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found."),
        @ApiResponse(responseCode = "500", description = "The receipt could not be emailed."),
    })
    @PostMapping("/{id}/receipt/email")
    public ResponseEntity<Void> emailReceipt(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable UUID id) {
        UUID profileId = extractProfileId(principal);
        receiptService.sendReceiptByEmail(id, profileId);
        return ResponseEntity.accepted().build();
    }

    private UUID extractProfileId(UserPrincipal principal) {
        if (principal == null || principal.getUserAccount() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        return principal.getUserAccount().getProfile().getId();
    }
}
