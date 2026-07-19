package com.aiinterview.controller;

import com.aiinterview.model.entity.User;
import com.aiinterview.model.vo.ApiResult;
import com.aiinterview.repository.UserRepository;
import com.aiinterview.config.JwtAuthFilter.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping("/api-key")
    public ResponseEntity<ApiResult<Map<String, Object>>> saveApiKey(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody Map<String, String> body) {
        String apiKey = body.get("apiKey");
        if (apiKey == null || apiKey.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResult.error("API Key 不能为空"));
        }
        if (!apiKey.startsWith("sk-")) {
            return ResponseEntity.badRequest().body(ApiResult.error("API Key 格式不正确，应以 sk- 开头"));
        }

        User u = userRepository.findById(user.userId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        u.setApiKey(apiKey);
        userRepository.save(u);

        return ResponseEntity.ok(ApiResult.success("API Key 已保存", Map.of(
                "saved", true,
                "masked", apiKey.substring(0, 6) + "..." + apiKey.substring(apiKey.length() - 4)
        )));
    }

    @GetMapping("/api-key")
    public ResponseEntity<ApiResult<Map<String, Object>>> getApiKey(
            @AuthenticationPrincipal UserPrincipal user) {
        User u = userRepository.findById(user.userId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        boolean hasKey = u.getApiKey() != null && !u.getApiKey().isBlank();
        Map<String, Object> data;
        if (hasKey) {
            String key = u.getApiKey();
            data = Map.of(
                    "hasKey", true,
                    "masked", key.substring(0, 6) + "..." + key.substring(key.length() - 4)
            );
        } else {
            data = Map.of("hasKey", false);
        }
        return ResponseEntity.ok(ApiResult.success(data));
    }
}
