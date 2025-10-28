package com.tq.exchangehub.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class ReviewRequest {

    @NotNull
    private UUID tradeId;

    @NotNull
    private UUID reviewerId;

    @NotNull
    private UUID reviewedId;

    @Min(1)
    @Max(5)
    private Integer rating;

    private String comment;

    public UUID getTradeId() {
        return tradeId;
    }

    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }

    public UUID getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(UUID reviewerId) {
        this.reviewerId = reviewerId;
    }

    public UUID getReviewedId() {
        return reviewedId;
    }

    public void setReviewedId(UUID reviewedId) {
        this.reviewedId = reviewedId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
