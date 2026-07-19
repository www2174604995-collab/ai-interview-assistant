package com.aiinterview.repository;

import com.aiinterview.model.entity.InterviewMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InterviewMessageRepository extends JpaRepository<InterviewMessage, Long> {
    List<InterviewMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);
    long countBySessionIdAndRole(Long sessionId, String role);
    void deleteBySessionId(Long sessionId);
}
