package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;

    public TradeService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    public List<TradeDto> listTrades(UUID profileId, List<TradeStatus> statuses) {
        List<TradeStatus> normalizedStatuses = normalizeStatuses(statuses);

        List<Trade> trades;
        if (normalizedStatuses.isEmpty()) {
            trades = tradeRepository.findByOwnerIdOrRequesterId(profileId, profileId);
        } else if (normalizedStatuses.size() == 1) {
            trades =
                    tradeRepository.findByOwnerIdOrRequesterIdAndStatus(
                            profileId, profileId, normalizedStatuses.get(0));
        } else {
            trades =
                    tradeRepository.findByOwnerIdOrRequesterIdAndStatusIn(
                            profileId, profileId, normalizedStatuses);
        }
        return trades.stream().map(DtoMapper::toTradeDto).collect(Collectors.toList());
    }

    public TradeDto getTrade(UUID id) {
        Trade trade =
                tradeRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        return DtoMapper.toTradeDto(trade);
    }

    public TradeDto updateStatus(UUID id, TradeStatusUpdateRequest request, UUID profileId) {
        Trade trade =
                tradeRepository
                        .findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        if (!trade.getOwner().getId().equals(profileId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You do not have permission to modify this trade request.");
        }

        trade.setStatus(request.getStatus());
        trade.setUpdatedAt(OffsetDateTime.now());
        Trade saved = tradeRepository.save(trade);
        return DtoMapper.toTradeDto(saved);
    }

    private List<TradeStatus> normalizeStatuses(List<TradeStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(new LinkedHashSet<>(statuses));
    }
}
