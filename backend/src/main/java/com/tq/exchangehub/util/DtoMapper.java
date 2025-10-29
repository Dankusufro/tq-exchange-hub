package com.tq.exchangehub.util;

import com.tq.exchangehub.dto.CategoryDto;
import com.tq.exchangehub.dto.ItemDto;
import com.tq.exchangehub.dto.ItemSummaryDto;
import com.tq.exchangehub.dto.MessageDto;
import com.tq.exchangehub.dto.ProfileDto;
import com.tq.exchangehub.dto.ReviewDto;
import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.entity.Category;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Review;
import com.tq.exchangehub.entity.Trade;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static ProfileDto toProfileDto(Profile profile) {
        ProfileDto dto = new ProfileDto();
        dto.setId(profile.getId());
        dto.setDisplayName(profile.getDisplayName());
        dto.setBio(profile.getBio());
        dto.setAvatarUrl(profile.getAvatarUrl());
        dto.setLocation(profile.getLocation());
        dto.setPhone(profile.getPhone());
        dto.setRating(profile.getRating());
        dto.setTotalTrades(profile.getTotalTrades());
        dto.setCreatedAt(profile.getCreatedAt());
        dto.setUpdatedAt(profile.getUpdatedAt());
        return dto;
    }

    public static ItemDto toItemDto(Item item) {
        ItemDto dto = new ItemDto();
        dto.setId(item.getId());
        dto.setOwnerId(item.getOwner().getId());
        dto.setCategoryId(item.getCategory().getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setCondition(item.getCondition());
        dto.setEstimatedValue(item.getEstimatedValue());
        dto.setAvailable(item.getAvailable());
        dto.setService(item.getService());
        dto.setLocation(item.getLocation());
        dto.setImages(item.getImages());
        dto.setWishlist(item.getWishlist());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        return dto;
    }

    public static ItemSummaryDto toItemSummaryDto(Item item) {
        ItemSummaryDto dto = new ItemSummaryDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setCondition(item.getCondition());
        dto.setLocation(item.getLocation());
        dto.setCategoryName(item.getCategory().getName());
        dto.setOwner(toProfileDto(item.getOwner()));
        dto.setRating(item.getOwner().getRating());
        dto.setWishlist(item.getWishlist());
        dto.setMainImageUrl(item.getImages().isEmpty() ? null : item.getImages().get(0));
        dto.setAvailable(item.getAvailable());
        dto.setService(item.getService());
        return dto;
    }

    public static CategoryDto toCategoryDto(Category category) {
        return toCategoryDto(category, 0L);
    }

    public static CategoryDto toCategoryDto(Category category, long itemsCount) {
        CategoryDto dto = new CategoryDto();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setItemsCount(itemsCount);
        return dto;
    }

    public static TradeDto toTradeDto(Trade trade) {
        TradeDto dto = new TradeDto();
        dto.setId(trade.getId());
        dto.setOwnerId(trade.getOwner().getId());
        dto.setRequesterId(trade.getRequester().getId());
        dto.setOwnerItemId(trade.getOwnerItem().getId());
        if (trade.getRequesterItem() != null) {
            dto.setRequesterItemId(trade.getRequesterItem().getId());
        }
        dto.setMessage(trade.getMessage());
        dto.setStatus(trade.getStatus());
        dto.setCreatedAt(trade.getCreatedAt());
        dto.setUpdatedAt(trade.getUpdatedAt());
        return dto;
    }

    public static MessageDto toMessageDto(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setTradeId(message.getTrade().getId());
        dto.setSenderId(message.getSender().getId());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }

    public static ReviewDto toReviewDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setTradeId(review.getTrade().getId());
        dto.setReviewerId(review.getReviewer().getId());
        dto.setReviewedId(review.getReviewed().getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}
