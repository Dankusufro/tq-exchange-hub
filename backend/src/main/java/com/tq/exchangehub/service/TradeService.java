package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.CreateTradeRequest;
import com.tq.exchangehub.dto.TradeDto;
import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.event.TradeStatusChangedEvent;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.MessageRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TradeService {

    private final TradeRepository tradeRepository;
    private final ItemRepository itemRepository;
    private final ProfileRepository profileRepository;
    private final MessageRepository messageRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    public TradeService(
            TradeRepository tradeRepository,
            ItemRepository itemRepository,
            ProfileRepository profileRepository,
            MessageRepository messageRepository,
            ApplicationEventPublisher eventPublisher,
            SimpMessagingTemplate messagingTemplate,
            NotificationService notificationService) {
        this.tradeRepository = tradeRepository;
        this.itemRepository = itemRepository;
        this.profileRepository = profileRepository;
        this.messageRepository = messageRepository;
        this.eventPublisher = eventPublisher;
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
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
        eventPublisher.publishEvent(new TradeStatusChangedEvent(saved, profileId));
        return DtoMapper.toTradeDto(saved);
    }

    public TradeDto create(CreateTradeRequest request, UUID requesterProfileId) {
        Item ownerItem =
                itemRepository
                        .findById(request.getOwnerItemId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found"));

        Profile owner = ownerItem.getOwner();
        Profile requester =
                profileRepository
                        .findById(requesterProfileId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        if (owner.getId().equals(requesterProfileId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "You cannot propose a trade for your own item");
        }

        Item requesterItem = null;
        if (request.getRequesterItemId() != null) {
            requesterItem =
                    itemRepository
                            .findById(request.getRequesterItemId())
                            .orElseThrow(
                                    () ->
                                            new ResponseStatusException(
                                                    HttpStatus.NOT_FOUND, "Requested item not found"));
            if (!requesterItem.getOwner().getId().equals(requesterProfileId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "The offered item must belong to the authenticated user");
            }
        }

        Trade trade = new Trade();
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setRequesterItem(requesterItem);
        trade.setMessage(request.getMessage());
        trade.setStatus(TradeStatus.PENDING);
        trade.setCreatedAt(OffsetDateTime.now());
        trade.setUpdatedAt(OffsetDateTime.now());

        Trade saved = tradeRepository.save(trade);

        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            Message message = new Message();
            message.setTrade(saved);
            message.setSender(requester);
            message.setContent(request.getMessage());
            message.setCreatedAt(OffsetDateTime.now());
            Message initialMessage = messageRepository.save(message);
            messagingTemplate.convertAndSend(
                    "/topic/trades/" + saved.getId() + "/messages",
                    DtoMapper.toMessageDto(initialMessage));
            notificationService.notifyMessage(owner, initialMessage);
        }

        return DtoMapper.toTradeDto(saved);
    }

    @EventListener
    public void handleTradeStatusChange(TradeStatusChangedEvent event) {
        Trade trade = event.getTrade();
        messagingTemplate.convertAndSend(
                "/topic/trades/" + trade.getId() + "/status",
                DtoMapper.toTradeDto(trade));
        notificationService.notifyTradeStatusChange(trade, event.getActorProfileId());
    }

    private List<TradeStatus> normalizeStatuses(List<TradeStatus> statuses) {
        if (statuses == null || statuses.isEmpty()) {
            return List.of();
        }

        return new ArrayList<>(new LinkedHashSet<>(statuses));
    }
}
