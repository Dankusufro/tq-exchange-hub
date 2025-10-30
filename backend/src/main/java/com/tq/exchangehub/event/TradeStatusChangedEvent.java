package com.tq.exchangehub.event;

import com.tq.exchangehub.entity.Trade;
import java.util.UUID;

public class TradeStatusChangedEvent {

    private final Trade trade;
    private final UUID actorProfileId;

    public TradeStatusChangedEvent(Trade trade, UUID actorProfileId) {
        this.trade = trade;
        this.actorProfileId = actorProfileId;
    }

    public Trade getTrade() {
        return trade;
    }

    public UUID getActorProfileId() {
        return actorProfileId;
    }
}
