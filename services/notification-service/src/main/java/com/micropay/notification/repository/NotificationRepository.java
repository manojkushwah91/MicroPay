package com.micropay.notification.repository;

import com.micropay.notification.model.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository interface for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for a user, ordered by creation date descending
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    /**
     * Find notifications by user ID and status
     */
    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, 
                                                                  com.micropay.notification.model.NotificationStatus status, 
                                                                  Pageable pageable);
}







