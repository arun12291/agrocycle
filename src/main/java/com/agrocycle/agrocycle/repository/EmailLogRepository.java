package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.EmailLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    List<EmailLog> findTop20ByOrderByCreatedAtDesc();
    Long countByStatus(EmailLog.EmailStatus status);
}
