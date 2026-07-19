package com.aiinterview.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StartInterviewRequest {
    @NotBlank(message = "面试职位不能为空")
    private String position;

    @NotBlank(message = "面试难度不能为空")
    private String difficulty;
}
