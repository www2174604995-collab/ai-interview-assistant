package com.aiinterview.service;

import com.aiinterview.model.dto.ReportResponse;

public interface ReportService {
    ReportResponse generateReport(Long userId, Long sessionId);
    ReportResponse getReport(Long userId, Long sessionId);
}
