package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.ReviewDto;
import com.tq.exchangehub.dto.ReviewRequest;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Review;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.ReviewRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TradeRepository tradeRepository;
    private final ProfileRepository profileRepository;

    public ReviewService(
            ReviewRepository reviewRepository,
            TradeRepository tradeRepository,
            ProfileRepository profileRepository) {
        this.reviewRepository = reviewRepository;
        this.tradeRepository = tradeRepository;
        this.profileRepository = profileRepository;
    }

    public List<ReviewDto> getReviewsForProfile(UUID profileId) {
        Profile profile =
                profileRepository
                        .findById(profileId)
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        return reviewRepository.findByReviewed(profile).stream()
                .map(DtoMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public List<ReviewDto> getReviewsForTrade(UUID tradeId) {
        Trade trade =
                tradeRepository
                        .findById(tradeId)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        return reviewRepository.findByTrade(trade).stream()
                .map(DtoMapper::toReviewDto)
                .collect(Collectors.toList());
    }

    public ReviewDto create(ReviewRequest request) {
        Trade trade =
                tradeRepository
                        .findById(request.getTradeId())
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        Profile reviewer =
                profileRepository
                        .findById(request.getReviewerId())
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));
        Profile reviewed =
                profileRepository
                        .findById(request.getReviewedId())
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Review review = new Review();
        review.setTrade(trade);
        review.setReviewer(reviewer);
        review.setReviewed(reviewed);
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setCreatedAt(OffsetDateTime.now());
        Review saved = reviewRepository.save(review);
        return DtoMapper.toReviewDto(saved);
    }
}
