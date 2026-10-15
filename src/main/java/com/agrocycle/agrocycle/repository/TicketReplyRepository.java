package com.agrocycle.agrocycle.repository;

import com.agrocycle.agrocycle.entity.TicketReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TicketReplyRepository extends JpaRepository<TicketReply, Long> {
    List<TicketReply> findByTicketIdOrderByCreatedAtAsc(Long ticketId);
}
