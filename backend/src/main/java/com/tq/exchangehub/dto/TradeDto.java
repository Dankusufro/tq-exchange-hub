package com.tq.exchangehub.dto;

import com.tq.exchangehub.entity.TradeStatus;
import java.time.OffsetDateTime;
import java.util.UUID;

public class TradeDto {

    private UUID id;
    private UUID ownerId;
    private UUID requesterId;
    private UUID ownerItemId;
    private UUID requesterItemId;
    private String message;
    private TradeStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public UUID getOwnerItemId() {
        return ownerItemId;
    }

    public void setOwnerItemId(UUID ownerItemId) {
        this.ownerItemId = ownerItemId;
    }

    public UUID getRequesterItemId() {
        return requesterItemId;
    }

    public void setRequesterItemId(UUID requesterItemId) {
        this.requesterItemId = requesterItemId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
