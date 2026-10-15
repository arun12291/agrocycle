package com.agrocycle.agrocycle.notification;

import com.agrocycle.agrocycle.dto.NotificationDto;
import com.agrocycle.agrocycle.entity.*;
import com.agrocycle.agrocycle.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Notification sendNotification(User recipient, Notification.NotificationType type,
                                          String title, String message,
                                          Long relatedOrderId, Long relatedListingId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedOrderId(relatedOrderId);
        n.setRelatedListingId(relatedListingId);
        n.setRead(false);
        Notification saved = notificationRepository.save(n);
        log.info("Notification created for user {}: {}", recipient.getId(), title);
        emailService.sendNotificationEmail(recipient.getEmail(), title, message);
        return saved;
    }

    @Transactional
    public Notification sendTicketNotification(User recipient, Notification.NotificationType type,
                                                String title, String message, Long ticketId) {
        Notification n = new Notification();
        n.setRecipient(recipient);
        n.setType(type);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedTicketId(ticketId);
        n.setRead(false);
        Notification saved = notificationRepository.save(n);
        emailService.sendNotificationEmail(recipient.getEmail(), title, message);
        return saved;
    }

    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientIdAndIsReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(Long userId, int limit) {
        return notificationRepository.findRecentByUser(userId, PageRequest.of(0, limit))
            .stream().map(NotificationDto::from).collect(Collectors.toList());
    }

    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        notificationRepository.markAsRead(notificationId, userId);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }
}
