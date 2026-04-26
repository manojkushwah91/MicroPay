package com.micropay.notification.service;

import com.micropay.events.dto.PasswordResetEvent;
import com.micropay.notification.dto.PaymentCompletedEvent;
import com.micropay.notification.dto.TransactionRecordedEvent;
import com.micropay.notification.model.Notification;
import com.micropay.notification.model.NotificationStatus;
import com.micropay.notification.model.NotificationType;
import com.micropay.notification.model.NotificationChannel;
import com.micropay.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Notification Service Basic Tests")
class NotificationServiceBasicTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private org.springframework.kafka.core.KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Should send password reset notification")
    void sendPasswordResetNotification_Success() {
        // Given
        PasswordResetEvent event = new PasswordResetEvent();
        event.setUserId(UUID.randomUUID());
        event.setEmail("test@example.com");
        event.setToken("reset-token-123");

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getUserId());
        notification.setNotificationType(NotificationType.PASSWORD_RESET);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.sendPasswordResetNotification(event);

        // Then
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should send payment completed notification")
    void sendPaymentCompletedNotification_Success() {
        // Given
        PaymentCompletedEvent event = new PaymentCompletedEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setPayerUserId(UUID.randomUUID());
        event.setPayeeUserId(UUID.randomUUID());
        event.setAmount(new BigDecimal("100.00"));
        event.setCurrency("USD");

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getPayerUserId());
        notification.setNotificationType(NotificationType.PAYMENT_COMPLETED);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.sendPaymentCompletedNotification(event);

        // Then
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should send transaction recorded notification")
    void sendTransactionRecordedNotification_Success() {
        // Given
        TransactionRecordedEvent event = new TransactionRecordedEvent();
        event.setTransactionId(UUID.randomUUID());
        event.setPaymentId(UUID.randomUUID());
        event.setEntries(List.of(new TransactionRecordedEvent.TransactionEntryDto()));

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(UUID.randomUUID());
        notification.setNotificationType(NotificationType.TRANSACTION_RECORDED);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.sendTransactionRecordedNotification(event);

        // Then
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle payment completed event with null payee")
    void sendPaymentCompletedNotification_NullPayee() {
        // Given
        PaymentCompletedEvent event = new PaymentCompletedEvent();
        event.setPaymentId(UUID.randomUUID());
        event.setPayerUserId(UUID.randomUUID());
        event.setPayeeUserId(null); // No payee
        event.setAmount(new BigDecimal("100.00"));
        event.setCurrency("USD");

        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setUserId(event.getPayerUserId());
        notification.setNotificationType(NotificationType.PAYMENT_COMPLETED);
        notification.setChannel(NotificationChannel.EMAIL);
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        // When
        notificationService.sendPaymentCompletedNotification(event);

        // Then
        verify(notificationRepository, atLeastOnce()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should handle transaction recorded event with empty entries")
    void sendTransactionRecordedNotification_EmptyEntries() {
        // Given
        TransactionRecordedEvent event = new TransactionRecordedEvent();
        event.setTransactionId(UUID.randomUUID());
        event.setPaymentId(UUID.randomUUID());
        event.setEntries(List.of()); // Empty entries

        // When
        notificationService.sendTransactionRecordedNotification(event);

        // Then
        verify(notificationRepository, never()).save(any());
    }
}
