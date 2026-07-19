package com.aiinterview.controller;

import com.aiinterview.model.dto.AuthResponse;
import com.aiinterview.model.dto.LoginRequest;
import com.aiinterview.model.dto.RegisterRequest;
import com.aiinterview.model.vo.ApiResult;
import com.aiinterview.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResult<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.success("注册成功", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResult.success("登录成功", response));
    }
}
