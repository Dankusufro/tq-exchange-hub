package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.MessageDto;
import com.tq.exchangehub.dto.MessageRequest;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.service.MessageService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Messages")
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(
            summary = "List messages for a trade",
            description = "Returns the chronological conversation for the specified trade.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Messages retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot view this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable UUID tradeId) {
        return ResponseEntity.ok(messageService.getMessagesForTrade(tradeId));
    }

    @Operation(
            summary = "Post a trade message",
            description = "Adds a new message to the trade conversation.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Message created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid message payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot post to this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @PostMapping
    public ResponseEntity<MessageDto> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.create(request, principal));
    }
}
