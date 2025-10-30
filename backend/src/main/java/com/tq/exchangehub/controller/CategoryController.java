package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.CategoryDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.service.CategoryService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Categories")
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "List categories", description = "Returns every available item category.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Categories retrieved successfully."),
        @ApiResponse(responseCode = "500", description = "Unexpected server error.")
    })
    @GetMapping
    public ResponseEntity<List<CategoryDto>> findAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @Operation(
            summary = "Create a category",
            description = "Registers a new category that can be assigned to items.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Category created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid category payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "Authenticated user is not allowed to create categories.")
    })
    @PostMapping
    public ResponseEntity<CategoryDto> create(@Valid @RequestBody Category category) {
        return ResponseEntity.ok(categoryService.create(category));
    }
}
