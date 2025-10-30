package com.tq.exchangehub.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class FavoriteRequest {

    @NotNull
    private UUID itemId;

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }
}
