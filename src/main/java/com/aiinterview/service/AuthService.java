package com.aiinterview.service;

import com.aiinterview.model.dto.AuthResponse;
import com.aiinterview.model.dto.LoginRequest;
import com.aiinterview.model.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
