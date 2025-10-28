package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, UUID> {
    List<Trade> findByStatus(TradeStatus status);
}
