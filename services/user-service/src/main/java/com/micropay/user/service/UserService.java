package com.micropay.user.service;

import com.micropay.user.model.NotificationPreference;
import com.micropay.user.repository.NotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public UserService(NotificationPreferenceRepository notificationPreferenceRepository) {
        this.notificationPreferenceRepository = notificationPreferenceRepository;
    }

    public List<NotificationPreference> getNotificationPreferences(UUID userId) {
        return notificationPreferenceRepository.findByUserId(userId);
    }

    public NotificationPreference updateNotificationPreference(NotificationPreference preference) {
        return notificationPreferenceRepository.save(preference);
    }
}
