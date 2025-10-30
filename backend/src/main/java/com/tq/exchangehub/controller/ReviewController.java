package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.ReviewDto;
import com.tq.exchangehub.dto.ReviewRequest;
import com.tq.exchangehub.service.ReviewService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Reviews")
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @Operation(
            summary = "List reviews for a profile",
            description = "Returns all reviews received by the specified profile.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "404", description = "Profile not found.")
    })
    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForProfile(@PathVariable UUID profileId) {
        return ResponseEntity.ok(reviewService.getReviewsForProfile(profileId));
    }

    @Operation(
            summary = "List reviews for a trade",
            description = "Returns all reviews associated with the specified trade.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForTrade(@PathVariable UUID tradeId) {
        return ResponseEntity.ok(reviewService.getReviewsForTrade(tradeId));
    }

    @Operation(
            summary = "Create a review",
            description = "Submits a review for a trade partner.",
            security = {@SecurityRequirement(name = "bearerAuth")})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review created successfully."),
        @ApiResponse(responseCode = "400", description = "Invalid review payload."),
        @ApiResponse(responseCode = "401", description = "Authentication required."),
        @ApiResponse(responseCode = "403", description = "The authenticated user cannot review this trade."),
        @ApiResponse(responseCode = "404", description = "Trade not found.")
    })
    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.create(request));
    }
}
