package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.MessageDto;
import com.tq.exchangehub.dto.MessageRequest;
import com.tq.exchangehub.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable UUID tradeId) {
        return ResponseEntity.ok(messageService.getMessagesForTrade(tradeId));
    }

    @PostMapping
    public ResponseEntity<MessageDto> create(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.ok(messageService.create(request));
    }
}
