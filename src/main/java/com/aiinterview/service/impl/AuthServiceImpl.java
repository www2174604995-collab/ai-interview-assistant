package com.aiinterview.service.impl;

import com.aiinterview.config.JwtUtil;
import com.aiinterview.model.dto.AuthResponse;
import com.aiinterview.model.dto.LoginRequest;
import com.aiinterview.model.dto.RegisterRequest;
import com.aiinterview.model.entity.User;
import com.aiinterview.repository.UserRepository;
import com.aiinterview.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname() != null ? request.getNickname() : request.getUsername())
                .build();
        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token).userId(user.getId())
                .username(user.getUsername()).nickname(user.getNickname())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token).userId(user.getId())
                .username(user.getUsername()).nickname(user.getNickname())
                .build();
    }
}
