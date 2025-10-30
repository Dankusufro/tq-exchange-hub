package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.MessageDto;
import com.tq.exchangehub.dto.MessageRequest;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.repository.MessageRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.security.UserPrincipal;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final TradeRepository tradeRepository;
    private final ProfileRepository profileRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public MessageService(
            MessageRepository messageRepository,
            TradeRepository tradeRepository,
            ProfileRepository profileRepository,
            SimpMessagingTemplate messagingTemplate,
            NotificationService notificationService) {
        this.messageRepository = messageRepository;
        this.tradeRepository = tradeRepository;
        this.profileRepository = profileRepository;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
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

    public MessageDto create(MessageRequest request, UserPrincipal principal) {
        if (principal == null || principal.getUserAccount() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        UUID senderProfileId = principal.getUserAccount().getProfile().getId();

        Trade trade =
                tradeRepository
                        .findById(request.getTradeId())
                        .orElseThrow(() -> new IllegalArgumentException("Trade not found"));

        Profile sender =
                profileRepository
                        .findById(senderProfileId)
                        .orElseThrow(() -> new IllegalArgumentException("Profile not found"));

        if (!trade.getOwner().getId().equals(senderProfileId)
                && !trade.getRequester().getId().equals(senderProfileId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "You are not a participant in this trade");
        }

        Message message = new Message();
        message.setTrade(trade);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setCreatedAt(OffsetDateTime.now());
        Message saved = messageRepository.save(message);
        MessageDto dto = DtoMapper.toMessageDto(saved);

        messagingTemplate.convertAndSend(
                "/topic/trades/" + trade.getId() + "/messages",
                dto);

        Profile recipient =
                trade.getOwner().getId().equals(senderProfileId)
                        ? trade.getRequester()
                        : trade.getOwner();
        if (recipient != null && recipient.getId() != null && !recipient.getId().equals(senderProfileId)) {
            notificationService.notifyMessage(recipient, saved);
        }

        return dto;
    }
}
