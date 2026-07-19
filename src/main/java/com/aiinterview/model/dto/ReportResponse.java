package com.aiinterview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportResponse {
    private Long sessionId;
    private String position;
    private String difficulty;
    private Integer totalQuestions;
    private Integer totalTurns;
    private String report;
    private LocalDateTime createdAt;
}
