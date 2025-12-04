package com.tq.exchangehub.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tq.exchangehub.dto.TradeStatusUpdateRequest;
import com.tq.exchangehub.entity.Item;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.ItemRepository;
import com.tq.exchangehub.repository.MessageRepository;
import com.tq.exchangehub.repository.ProfileRepository;
import com.tq.exchangehub.repository.TradeRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock private TradeRepository tradeRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private ProfileRepository profileRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private NotificationService notificationService;

    private TradeService tradeService;

    @BeforeEach
    void setUp() {
        tradeService =
                new TradeService(
                        tradeRepository,
                        itemRepository,
                        profileRepository,
                        messageRepository,
                        eventPublisher,
                        messagingTemplate,
                        notificationService);

        when(tradeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldMarkItemsUnavailableWhenTradeAccepted() {
        UUID tradeId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Profile owner = new Profile();
        owner.setId(ownerId);

        Profile requester = new Profile();
        requester.setId(requesterId);

        Item ownerItem = new Item();
        ownerItem.setAvailable(true);
        ownerItem.setOwner(owner);

        Item requesterItem = new Item();
        requesterItem.setAvailable(true);
        requesterItem.setOwner(requester);

        Trade trade = new Trade();
        trade.setId(tradeId);
        trade.setStatus(TradeStatus.PENDING);
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setRequesterItem(requesterItem);

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.ACCEPTED);

        tradeService.updateStatus(tradeId, request, ownerId);

        assertFalse(ownerItem.getAvailable());
        assertFalse(requesterItem.getAvailable());
        verify(itemRepository, times(1)).save(ownerItem);
        verify(itemRepository, times(1)).save(requesterItem);
    }

    @Test
    void shouldMarkItemsUnavailableWhenTradeCompleted() {
        UUID tradeId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Profile owner = new Profile();
        owner.setId(ownerId);

        Profile requester = new Profile();
        requester.setId(requesterId);

        Item ownerItem = new Item();
        ownerItem.setAvailable(true);
        ownerItem.setOwner(owner);

        Item requesterItem = new Item();
        requesterItem.setAvailable(true);
        requesterItem.setOwner(requester);

        Trade trade = new Trade();
        trade.setId(tradeId);
        trade.setStatus(TradeStatus.ACCEPTED);
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setRequesterItem(requesterItem);

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.COMPLETED);

        tradeService.updateStatus(tradeId, request, ownerId);

        assertFalse(ownerItem.getAvailable());
        assertFalse(requesterItem.getAvailable());
        verify(itemRepository, times(1)).save(ownerItem);
        verify(itemRepository, times(1)).save(requesterItem);
    }

    @Test
    void shouldRestoreAvailabilityWhenTradeRejected() {
        UUID tradeId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();

        Profile owner = new Profile();
        owner.setId(ownerId);

        Profile requester = new Profile();
        requester.setId(requesterId);

        Item ownerItem = new Item();
        ownerItem.setAvailable(false);
        ownerItem.setOwner(owner);

        Item requesterItem = new Item();
        requesterItem.setAvailable(false);
        requesterItem.setOwner(requester);

        Trade trade = new Trade();
        trade.setId(tradeId);
        trade.setStatus(TradeStatus.PENDING);
        trade.setOwner(owner);
        trade.setRequester(requester);
        trade.setOwnerItem(ownerItem);
        trade.setRequesterItem(requesterItem);

        when(tradeRepository.findById(tradeId)).thenReturn(Optional.of(trade));

        TradeStatusUpdateRequest request = new TradeStatusUpdateRequest();
        request.setStatus(TradeStatus.REJECTED);

        tradeService.updateStatus(tradeId, request, ownerId);

        assertTrue(ownerItem.getAvailable());
        assertTrue(requesterItem.getAvailable());
        verify(itemRepository, times(1)).save(ownerItem);
        verify(itemRepository, times(1)).save(requesterItem);
    }
}
