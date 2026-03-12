package com.micropay.events.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class PasswordResetEvent {

    private String eventId;
    private UUID userId;
    private String email;
    private String token;
    private LocalDateTime timestamp;

    // Constructors
    public PasswordResetEvent() {}

    public PasswordResetEvent(UUID userId, String email, String token) {
        this.eventId = UUID.randomUUID().toString();
        this.userId = userId;
        this.email = email;
        this.token = token;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}