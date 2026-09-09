package com.aiinterview.service.impl;

import com.aiinterview.constant.FileConstants;
import com.aiinterview.exception.FileProcessException;
import com.aiinterview.exception.ResourceNotFoundException;
import com.aiinterview.model.entity.User;
import com.aiinterview.repository.UserRepository;
import com.aiinterview.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {
    private final UserRepository userRepository;

    @Value("${app.upload-dir:uploads/resumes}")
    private String uploadDir;

    @Override
    @Transactional
    public String uploadResume(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(FileConstants.ERROR_USER_NOT_FOUND));

        // 验证文件
        String filename = file.getOriginalFilename();
        validateFile(filename, file.getSize());

        String ext = extractFileExtension(filename);
        validateFileExtension(ext);

        try {
            // 保存文件
            String savedName = userId + "_" + System.currentTimeMillis() + ext;
            Path dir = Paths.get(uploadDir);
            if (!Files.exists(dir)) Files.createDirectories(dir);
            Path target = dir.resolve(savedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            // 提取文本
            String text = extractText(file, ext);

            // 保存到用户
            user.setResumeText(text);
            user.setResumeFileName(filename);
            userRepository.save(user);

            log.info("Resume uploaded successfully: userId={}, file={}, chars={}", userId, filename, text.length());
            return text;
        } catch (IOException e) {
            log.error("Failed to process resume file for userId={}: {}", userId, e.getMessage());
            throw new FileProcessException(FileConstants.ERROR_FILE_PROCESS_FAILED, e);
        }
    }

    @Override
    public String getResumeText(Long userId) {
        return userRepository.findById(userId)
                .map(User::getResumeText)
                .orElse(null);
    }

    @Override
    @Transactional
    public void deleteResume(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(FileConstants.ERROR_USER_NOT_FOUND));
        user.setResumeText(null);
        user.setResumeFileName(null);
        userRepository.save(user);
        log.info("Resume deleted for userId={}", userId);
    }

    private void validateFile(String filename, long fileSize) {
        if (filename == null || filename.isBlank()) {
            throw new FileProcessException(FileConstants.ERROR_FILE_NAME_EMPTY);
        }
        if (fileSize > FileConstants.MAX_FILE_SIZE) {
            throw new FileProcessException(FileConstants.ERROR_FILE_TOO_LARGE);
        }
    }

    private String extractFileExtension(String filename) {
        int dotIdx = filename.lastIndexOf('.');
        return dotIdx > 0 ? filename.substring(dotIdx).toLowerCase() : "";
    }

    private void validateFileExtension(String ext) {
        if (!FileConstants.PDF_EXTENSION.equals(ext) && !FileConstants.DOCX_EXTENSION.equals(ext)) {
            throw new FileProcessException(FileConstants.ERROR_UNSUPPORTED_FILE_FORMAT);
        }
    }

    private String extractText(MultipartFile file, String ext) throws IOException {
        byte[] bytes = file.getBytes();
        String text = FileConstants.PDF_EXTENSION.equals(ext) 
                ? extractPdfText(bytes) 
                : extractDocxText(bytes);

        if (text.isBlank()) {
            throw new FileProcessException(FileConstants.ERROR_NO_TEXT_EXTRACTED);
        }

        // 限制文本长度
        if (text.length() > FileConstants.MAX_RESUME_TEXT_LENGTH) {
            text = text.substring(0, FileConstants.MAX_RESUME_TEXT_LENGTH);
        }
        return text;
    }

    private String extractPdfText(byte[] bytes) throws IOException {
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String extractDocxText(byte[] bytes) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(new ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
