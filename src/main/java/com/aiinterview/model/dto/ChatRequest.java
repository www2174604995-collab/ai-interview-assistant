package com.aiinterview.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChatRequest {
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    @NotBlank(message = "消息内容不能为空")
    private String message;
}
