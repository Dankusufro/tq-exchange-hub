package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.MessageDto;
import com.tq.exchangehub.dto.MessageRequest;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.repository.MessageRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final TradeRepository tradeRepository;
    private final ProfileRepository profileRepository;

    public MessageService(
            MessageRepository messageRepository,
            TradeRepository tradeRepository,
            ProfileRepository profileRepository) {
        this.messageRepository = messageRepository;
        this.tradeRepository = tradeRepository;
        this.profileRepository = profileRepository;
    }

    public List<MessageDto> getMessagesForTrade(UUID tradeId) {
        Trade trade =
                tradeRepository
                        .findById(tradeId)
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        return messageRepository.findByTradeOrderByCreatedAtAsc(trade).stream()
                .map(DtoMapper::toMessageDto)
                .collect(Collectors.toList());
    }

    public MessageDto create(MessageRequest request) {
        Trade trade =
                tradeRepository
                        .findById(request.getTradeId())
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));
        Profile sender =
                profileRepository
                        .findById(request.getSenderId())
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        Message message = new Message();
        message.setTrade(trade);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setCreatedAt(OffsetDateTime.now());
        Message saved = messageRepository.save(message);
        return DtoMapper.toMessageDto(saved);
    }
}
