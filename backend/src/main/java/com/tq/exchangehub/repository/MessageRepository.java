package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Trade;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByTradeOrderByCreatedAtAsc(Trade trade);
}
