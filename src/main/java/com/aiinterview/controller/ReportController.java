package com.aiinterview.controller;

import com.aiinterview.model.dto.ReportResponse;
import com.aiinterview.model.vo.ApiResult;
import com.aiinterview.service.ReportService;
import com.aiinterview.config.JwtAuthFilter.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/generate/{sessionId}")
    public ResponseEntity<ApiResult<ReportResponse>> generate(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResult.success("报告生成成功",
                reportService.generateReport(user.userId(), sessionId)));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<ApiResult<ReportResponse>> get(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResult.success(reportService.getReport(user.userId(), sessionId)));
    }
}
