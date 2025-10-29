package com.tq.exchangehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CreateTradeRequest {

    @NotNull
    private UUID ownerItemId;

    private UUID requesterItemId;

    @NotBlank
    private String message;

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
}
