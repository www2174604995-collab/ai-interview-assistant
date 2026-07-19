package com.aiinterview.controller;

import com.aiinterview.model.dto.*;
import com.aiinterview.model.vo.ApiResult;
import com.aiinterview.service.InterviewService;
import com.aiinterview.config.JwtAuthFilter.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interview")
@RequiredArgsConstructor
public class InterviewController {
    private final InterviewService interviewService;

    @PostMapping("/start")
    public ResponseEntity<ApiResult<ChatResponse>> start(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody StartInterviewRequest request) {
        ChatResponse response = interviewService.startInterview(user.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("面试开始", response));
    }

    @PostMapping("/chat")
    public ResponseEntity<ApiResult<ChatResponse>> chat(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChatRequest request) {
        ChatResponse response = interviewService.chat(user.userId(), request);
        return ResponseEntity.ok(ApiResult.success(response));
    }

    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(
            @AuthenticationPrincipal UserPrincipal user,
            @Valid @RequestBody ChatRequest request) {
        SseEmitter emitter = new SseEmitter(180000L);
        interviewService.chatStream(user.userId(), request, emitter);
        return emitter;
    }

    @PostMapping("/end/{sessionId}")
    public ResponseEntity<ApiResult<Map<String, Object>>> end(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        interviewService.endInterview(user.userId(), sessionId);
        return ResponseEntity.ok(ApiResult.success("面试已结束", Map.of("sessionId", sessionId, "status", "COMPLETED")));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResult<List<SessionSummary>>> sessions(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResult.success(interviewService.getSessions(user.userId())));
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResult<List<SessionSummary>>> history(
            @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(ApiResult.success(interviewService.getHistory(user.userId())));
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<ApiResult<SessionDetailResponse>> detail(
            @AuthenticationPrincipal UserPrincipal user,
            @PathVariable Long sessionId) {
        return ResponseEntity.ok(ApiResult.success(interviewService.getSessionDetail(user.userId(), sessionId)));
    }
}
