package com.micropay.events.dto;

import java.util.UUID;

public class PasswordResetEvent {
    private UUID userId;
    private String email;
    private String resetToken;

    public PasswordResetEvent() {
    }

    public PasswordResetEvent(UUID userId, String email, String resetToken) {
        this.userId = userId;
        this.email = email;
        this.resetToken = resetToken;
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

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }
}
