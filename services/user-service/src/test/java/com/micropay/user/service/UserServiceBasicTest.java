package com.micropay.user.service;

import com.micropay.user.model.NotificationPreference;
import com.micropay.user.repository.NotificationPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("User Service Basic Tests")
class UserServiceBasicTest {

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private NotificationPreference notificationPreference;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        notificationPreference = new NotificationPreference();
        notificationPreference.setId(UUID.randomUUID());
        notificationPreference.setUserId(userId);
        notificationPreference.setNotificationType("PAYMENT");
        notificationPreference.setEmailEnabled(true);
        notificationPreference.setSmsEnabled(false);
        notificationPreference.setPushEnabled(true);
    }

    @Test
    @DisplayName("Should get notification preferences successfully")
    void getNotificationPreferences_Success() {
        // Given
        List<NotificationPreference> preferences = Arrays.asList(notificationPreference);
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(preferences);

        // When
        List<NotificationPreference> result = userService.getNotificationPreferences(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        assertTrue(result.get(0).isEmailEnabled());
        assertFalse(result.get(0).isSmsEnabled());
        assertTrue(result.get(0).isPushEnabled());

        verify(notificationPreferenceRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should return empty list when no preferences found")
    void getNotificationPreferences_EmptyList() {
        // Given
        when(notificationPreferenceRepository.findByUserId(userId)).thenReturn(Arrays.asList());

        // When
        List<NotificationPreference> result = userService.getNotificationPreferences(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(notificationPreferenceRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("Should update notification preference successfully")
    void updateNotificationPreference_Success() {
        // Given
        NotificationPreference updatedPreference = new NotificationPreference();
        updatedPreference.setId(UUID.randomUUID());
        updatedPreference.setUserId(userId);
        updatedPreference.setNotificationType("PAYMENT");
        updatedPreference.setEmailEnabled(false);
        updatedPreference.setSmsEnabled(true);
        updatedPreference.setPushEnabled(false);

        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenReturn(updatedPreference);

        // When
        NotificationPreference result = userService.updateNotificationPreference(updatedPreference);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertFalse(result.isEmailEnabled());
        assertTrue(result.isSmsEnabled());
        assertFalse(result.isPushEnabled());

        verify(notificationPreferenceRepository).save(updatedPreference);
    }

    @Test
    @DisplayName("Should create new notification preference successfully")
    void updateNotificationPreference_CreateNew() {
        // Given
        NotificationPreference newPreference = new NotificationPreference();
        newPreference.setUserId(userId);
        newPreference.setEmailEnabled(true);
        newPreference.setSmsEnabled(true);
        newPreference.setPushEnabled(true);

        NotificationPreference savedPreference = new NotificationPreference();
        savedPreference.setId(UUID.randomUUID());
        savedPreference.setUserId(userId);
        savedPreference.setNotificationType("PAYMENT");
        savedPreference.setEmailEnabled(true);
        savedPreference.setSmsEnabled(true);
        savedPreference.setPushEnabled(true);

        when(notificationPreferenceRepository.save(any(NotificationPreference.class)))
                .thenReturn(savedPreference);

        // When
        NotificationPreference result = userService.updateNotificationPreference(newPreference);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(userId, result.getUserId());
        assertTrue(result.isEmailEnabled());
        assertTrue(result.isSmsEnabled());
        assertTrue(result.isPushEnabled());

        verify(notificationPreferenceRepository).save(newPreference);
    }
}
