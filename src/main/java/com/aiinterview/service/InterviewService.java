package com.aiinterview.service;

import com.aiinterview.model.dto.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;

public interface InterviewService {
    ChatResponse startInterview(Long userId, StartInterviewRequest request);
    ChatResponse chat(Long userId, ChatRequest request);
    void chatStream(Long userId, ChatRequest request, SseEmitter emitter);
    void endInterview(Long userId, Long sessionId);
    List<SessionSummary> getHistory(Long userId);
    List<SessionSummary> getSessions(Long userId);
    SessionDetailResponse getSessionDetail(Long userId, Long sessionId);
}
