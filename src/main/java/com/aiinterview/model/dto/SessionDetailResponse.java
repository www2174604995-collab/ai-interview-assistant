package com.aiinterview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SessionDetailResponse {
    private Long sessionId;
    private String position;
    private String difficulty;
    private String status;
    private Integer totalQuestions;
    private Integer totalTurns;
    private LocalDateTime createdAt;
    private List<MessageDto> messages;
}
