package com.micropay.user.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String eventType;
    private LocalDateTime timestamp;
    private UUID userId;
    private String email;
    private String firstName;
    private String lastName;
    private LocalDateTime createdAt;
}
