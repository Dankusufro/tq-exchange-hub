package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.CategoryDto;
import com.tq.exchangehub.dto.ItemSummaryDto;
import com.tq.exchangehub.dto.SearchResultDto;
import com.tq.exchangehub.service.CategoryService;
import com.tq.exchangehub.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Search")
@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final ItemService itemService;
    private final CategoryService categoryService;

    public SearchController(ItemService itemService, CategoryService categoryService) {
        this.itemService = itemService;
        this.categoryService = categoryService;
    }

    @Operation(
            summary = "Search categories and items",
            description = "Performs a combined search across available categories and marketplace items.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Search completed successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters supplied."),
    })
    @GetMapping
    public ResponseEntity<SearchResultDto> search(
            @RequestParam(name = "q", defaultValue = "") String query,
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String normalizedQuery = query == null ? "" : query.trim();
        int resolvedPage = Math.max(page, 0);
        int resolvedSize = Math.max(size, 1);

        Page<ItemSummaryDto> itemsPage = itemService.search(normalizedQuery, categoryId, resolvedPage, resolvedSize);
        List<CategoryDto> categories =
                normalizedQuery.isEmpty() ? List.of() : categoryService.search(normalizedQuery, 5);

        SearchResultDto result = new SearchResultDto();
        result.setQuery(normalizedQuery);
        result.setCategories(categories);
        result.setItems(itemsPage.getContent());
        result.setPage(itemsPage.getNumber());
        result.setSize(itemsPage.getSize());
        result.setTotalItems(itemsPage.getTotalElements());
        result.setTotalPages(itemsPage.getTotalPages());

        return ResponseEntity.ok(result);
    }
}
