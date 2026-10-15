package com.agrocycle.agrocycle.controller;

import com.agrocycle.agrocycle.dto.NotificationDto;
import com.agrocycle.agrocycle.notification.NotificationService;
import com.agrocycle.agrocycle.repository.UserRepository;
import com.agrocycle.agrocycle.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificationDto>> list(@RequestParam(defaultValue = "20") int limit,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = getUserId(userDetails.getUsername());
        return ResponseEntity.ok(notificationService.getNotifications(userId, limit));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(Map.of("count", notificationService.getUnreadCount(getUserId(userDetails.getUsername()))));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, String>> markRead(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAsRead(id, getUserId(userDetails.getUsername()));
        return ResponseEntity.ok(Map.of("message", "Marked as read"));
    }

    @PostMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllRead(@AuthenticationPrincipal UserDetails userDetails) {
        notificationService.markAllAsRead(getUserId(userDetails.getUsername()));
        return ResponseEntity.ok(Map.of("message", "All marked as read"));
    }

    private Long getUserId(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found")).getId();
    }
}
