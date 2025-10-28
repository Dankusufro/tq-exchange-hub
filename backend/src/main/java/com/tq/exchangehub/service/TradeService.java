package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<TradeDto> listTrades(Optional<TradeStatus> status) {
        List<Trade> trades =
                status.map(tradeRepository::findByStatus).orElseGet(tradeRepository::findAll);
        return trades.stream().map(DtoMapper::toTradeDto).collect(Collectors.toList());
    }

    public TradeDto getTrade(UUID id) {
        Trade trade =
                tradeRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        return DtoMapper.toTradeDto(trade);
    }

    public TradeDto updateStatus(UUID id, TradeStatusUpdateRequest request) {
        Trade trade =
                tradeRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        trade.setStatus(request.getStatus());
        trade.setUpdatedAt(OffsetDateTime.now());
        Trade saved = tradeRepository.save(trade);
        return DtoMapper.toTradeDto(saved);
    }
}
