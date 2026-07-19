package com.aiinterview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SessionSummary {
    private Long id;
    private String position;
    private String difficulty;
    private String status;
    private Integer totalQuestions;
    private Integer totalTurns;
    private Long messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
