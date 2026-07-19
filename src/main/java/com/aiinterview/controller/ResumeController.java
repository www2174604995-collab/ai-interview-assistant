package com.aiinterview.controller;

import com.aiinterview.model.vo.ApiResult;
import com.aiinterview.service.ResumeService;
import com.aiinterview.config.JwtAuthFilter.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/upload")
    public ResponseEntity<ApiResult<Map<String, Object>>> upload(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam("file") MultipartFile file) {
        String text = resumeService.uploadResume(user.userId(), file);
        return ResponseEntity.ok(ApiResult.success("简历上传成功", Map.of(
                "chars", text.length(),
                "preview", text.substring(0, Math.min(200, text.length()))
        )));
    }


    @DeleteMapping
    public ResponseEntity<ApiResult<String>> deleteResume(
            @AuthenticationPrincipal UserPrincipal user) {
        resumeService.deleteResume(user.userId());
        return ResponseEntity.ok(ApiResult.success("简历已删除"));
    }

    @GetMapping
    public ResponseEntity<ApiResult<Map<String, Object>>> getResume(
            @AuthenticationPrincipal UserPrincipal user) {
        String text = resumeService.getResumeText(user.userId());
        if (text == null) {
            return ResponseEntity.ok(ApiResult.success(Map.of("hasResume", false)));
        }
        return ResponseEntity.ok(ApiResult.success(Map.of(
                "hasResume", true,
                "chars", text.length(),
                "preview", text.substring(0, Math.min(200, text.length()))
        )));
    }
}
