package com.micropay.user.event;

import com.micropay.user.dto.UserCreatedEvent;
import com.micropay.user.model.NotificationPreference;
import com.micropay.user.repository.NotificationPreferenceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventConsumer {

    private final NotificationPreferenceRepository preferenceRepository;

    @KafkaListener(topics = "user.created", groupId = "user-service-group")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received user creation event for: {}", event.getEmail());
        
        NotificationPreference prefs = new NotificationPreference();
        prefs.setUserId(event.getUserId());
        prefs.setNotificationType("GENERAL");
        prefs.setEmailEnabled(true);
        prefs.setSmsEnabled(false);
        prefs.setPushEnabled(true);
        
        preferenceRepository.save(prefs);
        log.info("Default notification preferences created for user ID: {}", event.getUserId());
    }
}
