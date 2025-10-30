package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.TradeReceipt;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeReceiptRepository extends JpaRepository<TradeReceipt, UUID> {
    Optional<TradeReceipt> findByTradeId(UUID tradeId);
}
