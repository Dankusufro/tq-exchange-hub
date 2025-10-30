package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.dto.ItemRequest;
import com.tq.exchangehub.dto.ItemSummaryDto;
import com.tq.exchangehub.service.ItemService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Items")
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @Operation(summary = "List all items", description = "Returns every item available in the marketplace.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Items retrieved successfully."),
        @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    @GetMapping
    public ResponseEntity<List<ItemDto>> findAll() {
        return ResponseEntity.ok(itemService.findAll());
    }

    @Operation(summary = "Get item details", description = "Retrieves a single item by its identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item retrieved successfully."),
        @ApiResponse(responseCode = "404", description = "Item not found."),
        @ApiResponse(responseCode = "400", description = "Invalid item identifier supplied.")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ItemDto> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(itemService.findById(id));
    }

    @Operation(summary = "List items by owner", description = "Returns all items published by the specified owner profile.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Items retrieved successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid owner identifier supplied."),
        @ApiResponse(responseCode = "404", description = "Owner profile not found.")
    })
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<ItemDto>> findByOwner(@PathVariable UUID ownerId) {
        return ResponseEntity.ok(itemService.findByOwner(ownerId));
    }

    @Operation(
            summary = "List highlighted items",
            description = "Provides a paginated list of highlighted items for landing page sections.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Highlighted items retrieved successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid pagination parameters.")
    })
    @GetMapping("/highlighted")
    public ResponseEntity<Page<ItemSummaryDto>> findHighlighted(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(itemService.findHighlighted(page, size));
    }

    @Operation(
            summary = "Create an item",
            description = "Publishes a new item on behalf of the authenticated user.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid item payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to create items.")
    })
    @PostMapping
    public ResponseEntity<ItemDto> create(@Valid @RequestBody ItemRequest request) {
        return ResponseEntity.ok(itemService.create(request));
    }
}
