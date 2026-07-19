package com.aiinterview.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatResponse {
    private Long sessionId;
    private String reply;
    private boolean completed;
}
