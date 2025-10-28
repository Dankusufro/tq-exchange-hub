package com.tq.exchangehub.dto;

import com.tq.exchangehub.entity.TradeStatus;
import jakarta.validation.constraints.NotNull;

public class TradeStatusUpdateRequest {

    @NotNull
    private TradeStatus status;

    public TradeStatus getStatus() {
        return status;
    }

    public void setStatus(TradeStatus status) {
        this.status = status;
    }
}
