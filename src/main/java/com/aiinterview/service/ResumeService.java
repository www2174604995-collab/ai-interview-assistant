package com.aiinterview.service;

import org.springframework.web.multipart.MultipartFile;

public interface ResumeService {
    String uploadResume(Long userId, MultipartFile file);
    String getResumeText(Long userId);
    void deleteResume(Long userId);
}
