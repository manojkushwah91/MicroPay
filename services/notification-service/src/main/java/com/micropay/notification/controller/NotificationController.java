package com.micropay.notification.controller;

import com.micropay.notification.dto.NotificationResponse;
import com.micropay.notification.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for notification operations
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /notifications/{userId}
     * Fetch user notifications
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<NotificationResponse>> getNotificationsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        logger.info("Fetching notifications for user: {} (page: {}, size: {})", userId, page, size);
        List<NotificationResponse> notifications = notificationService.getNotificationsByUserId(userId, page, size);
        return ResponseEntity.ok(notifications);
    }
}







