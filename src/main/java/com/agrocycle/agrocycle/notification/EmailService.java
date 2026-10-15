package com.agrocycle.agrocycle.notification;

import com.agrocycle.agrocycle.entity.EmailLog;
import com.agrocycle.agrocycle.repository.EmailLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final EmailLogRepository emailLogRepository;

    @Value("${agrocycle.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${agrocycle.mail.from:noreply@agrocycle.com}")
    private String fromAddress;

    @Value("${agrocycle.mail.from-name:AgroCycle Platform}")
    private String fromName;

    public EmailService(JavaMailSender mailSender, EmailLogRepository emailLogRepository) {
        this.mailSender = mailSender;
        this.emailLogRepository = emailLogRepository;
    }

    public void sendNotificationEmail(String toEmail, String subject, String body) {
        EmailLog entry = new EmailLog();
        entry.setRecipientEmail(toEmail);
        entry.setSubject(subject);
        if (!mailEnabled) {
            log.info("[EMAIL DISABLED] Would send to {}: {}", toEmail, subject);
            entry.setStatus(EmailLog.EmailStatus.SKIPPED_NO_CONFIG);
            entry.setErrorMessage("Mail is disabled (MAIL_ENABLED=false)");
            emailLogRepository.save(entry);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromAddress + ">");
            message.setTo(toEmail);
            message.setSubject("[AgroCycle] " + subject);
            message.setText(body + "\n\n---\nAgroCycle - Intelligent Crop Waste Exchange Platform");
            mailSender.send(message);
            entry.setStatus(EmailLog.EmailStatus.SENT);
            entry.setSentAt(LocalDateTime.now());
            log.info("Email sent to {}: {}", toEmail, subject);
        } catch (MailException e) {
            entry.setStatus(EmailLog.EmailStatus.FAILED);
            entry.setErrorMessage(e.getMessage());
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
        } finally {
            emailLogRepository.save(entry);
        }
    }
}
