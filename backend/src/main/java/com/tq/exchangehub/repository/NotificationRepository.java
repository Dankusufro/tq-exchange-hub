package com.tq.exchangehub.repository;

import com.tq.exchangehub.entity.Notification;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByRecipientIdOrderByCreatedAtDesc(UUID recipientId);

    List<Notification> findByIdInAndRecipientId(List<UUID> ids, UUID recipientId);
}
