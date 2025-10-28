package com.tq.exchangehub.controller;

import com.tq.exchangehub.dto.ReviewDto;
import com.tq.exchangehub.dto.ReviewRequest;
import com.tq.exchangehub.service.ReviewService;
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
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/profile/{profileId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForProfile(@PathVariable UUID profileId) {
        return ResponseEntity.ok(reviewService.getReviewsForProfile(profileId));
    }

    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<ReviewDto>> getReviewsForTrade(@PathVariable UUID tradeId) {
        return ResponseEntity.ok(reviewService.getReviewsForTrade(tradeId));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> create(@Valid @RequestBody ReviewRequest request) {
        return ResponseEntity.ok(reviewService.create(request));
    }
}
