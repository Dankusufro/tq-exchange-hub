package com.tq.exchangehub.service;

import com.tq.exchangehub.dto.NotificationDto;
import com.tq.exchangehub.entity.Message;
import com.tq.exchangehub.entity.Notification;
import com.tq.exchangehub.entity.NotificationType;
import com.tq.exchangehub.entity.Profile;
import com.tq.exchangehub.entity.Trade;
import com.tq.exchangehub.entity.TradeStatus;
import com.tq.exchangehub.repository.NotificationRepository;
import com.tq.exchangehub.util.DtoMapper;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class NotificationService {

    private static final int MESSAGE_PREVIEW_LIMIT = 160;

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository, SimpMessagingTemplate messagingTemplate) {
        this.notificationRepository = notificationRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> list(UUID recipientId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(DtoMapper::toNotificationDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(UUID recipientId, List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        List<Notification> notifications =
                notificationRepository.findByIdInAndRecipientId(ids, recipientId);

        notifications.forEach(notification -> notification.setRead(Boolean.TRUE));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void markAllAsRead(UUID recipientId) {
        List<Notification> notifications =
                notificationRepository.findByRecipientIdOrderByCreatedAtDesc(recipientId);
        notifications.forEach(notification -> notification.setRead(Boolean.TRUE));
        notificationRepository.saveAll(notifications);
    }

    @Transactional
    public void notifyMessage(Profile recipient, Message message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(NotificationType.MESSAGE);
        notification.setTitle("Nuevo mensaje en tu trueque");
        notification.setMessage(truncateMessage(message.getContent()));
        notification.setTradeId(message.getTrade().getId());
        notification.setMessageId(message.getId());
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setRead(Boolean.FALSE);

        Notification saved = notificationRepository.save(notification);
        dispatch(saved);
    }

    @Transactional
    public void notifyTradeStatusChange(Trade trade, UUID actorProfileId) {
        Notification ownerNotification = buildTradeNotification(trade, trade.getOwner(), actorProfileId);
        Notification requesterNotification = buildTradeNotification(trade, trade.getRequester(), actorProfileId);

        if (ownerNotification != null) {
            dispatch(notificationRepository.save(ownerNotification));
        }

        if (requesterNotification != null) {
            dispatch(notificationRepository.save(requesterNotification));
        }
    }

    private Notification buildTradeNotification(Trade trade, Profile recipient, UUID actorProfileId) {
        if (recipient == null || recipient.getId() == null) {
            return null;
        }

        if (recipient.getId().equals(actorProfileId)) {
            return null;
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(NotificationType.TRADE);
        notification.setTitle("Actualización del trueque");
        notification.setMessage(buildTradeStatusMessage(trade));
        notification.setTradeId(trade.getId());
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setRead(Boolean.FALSE);
        return notification;
    }

    private String buildTradeStatusMessage(Trade trade) {
        TradeStatus status = trade.getStatus();
        String statusLabel = switch (status) {
            case ACCEPTED -> "aceptado";
            case REJECTED -> "rechazado";
            case COMPLETED -> "completado";
            case CANCELLED -> "cancelado";
            default -> status.name().toLowerCase(Locale.ROOT);
        };

        String reference = null;
        if (trade.getOwnerItem() != null && StringUtils.hasText(trade.getOwnerItem().getTitle())) {
            reference = trade.getOwnerItem().getTitle();
        }

        if (reference != null) {
            return "El trueque por \"" + reference + "\" fue " + statusLabel + ".";
        }

        return "El estado del trueque cambió a " + statusLabel + ".";
    }

    private void dispatch(Notification notification) {
        NotificationDto dto = DtoMapper.toNotificationDto(notification);
        String destination = "/topic/profiles/" + notification.getRecipient().getId() + "/notifications";
        messagingTemplate.convertAndSend(destination, dto);
    }

    private String truncateMessage(String content) {
        if (!StringUtils.hasText(content)) {
            return "Tienes un nuevo mensaje.";
        }

        String trimmed = content.trim();
        if (trimmed.length() <= MESSAGE_PREVIEW_LIMIT) {
            return trimmed;
        }

        return trimmed.substring(0, MESSAGE_PREVIEW_LIMIT - 3) + "...";
    }
}
