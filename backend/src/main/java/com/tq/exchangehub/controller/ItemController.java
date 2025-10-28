package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.dto.ItemRequest;
import com.tq.exchangehub.service.ItemService;
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
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public ResponseEntity<List<ItemDto>> findAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ItemDto>> findByOwner(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(itemService.findByOwner(ownerId));
    }

    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.create(request));
    }
}
