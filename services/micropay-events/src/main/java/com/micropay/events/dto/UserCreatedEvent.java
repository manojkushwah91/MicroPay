package com.micropay.events.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class UserCreatedEvent {

    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;

    // Constructors
    public UserCreatedEvent() {
    }

    public UserCreatedEvent(UUID userId, String email, String firstName, String lastName, LocalDateTime createdAt) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = "user.created";
        this.timestamp = LocalDateTime.now();
        this.userId = userId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

