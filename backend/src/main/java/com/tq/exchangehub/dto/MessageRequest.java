package com.tq.exchangehub.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class MessageRequest {

    @NotNull
    private UUID tradeId;

    @NotBlank
    private String content;

    public UUID getTradeId() {
        return tradeId;
    }

    public void setTradeId(UUID tradeId) {
        this.tradeId = tradeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
