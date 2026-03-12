package com.micropay.user.controller;

import com.micropay.user.model.NotificationPreference;
import com.micropay.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}/notification-preferences")
    public ResponseEntity<List<NotificationPreference>> getNotificationPreferences(@PathVariable UUID userId) {
        return ResponseEntity.ok(userService.getNotificationPreferences(userId));
    }

    @PutMapping("/notification-preferences")
    public ResponseEntity<NotificationPreference> updateNotificationPreference(@RequestBody NotificationPreference preference) {
        return ResponseEntity.ok(userService.updateNotificationPreference(preference));
    }
}
