package com.aiinterview.service.impl;

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
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new RuntimeException("文件名不能为空");
        }

        String ext = "";
        int dotIdx = filename.lastIndexOf('.');
        if (dotIdx > 0) ext = filename.substring(dotIdx).toLowerCase();

        if (!ext.equals(".pdf") && !ext.equals(".docx")) {
            throw new RuntimeException("仅支持 PDF 和 DOCX 格式");
        }

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

            log.info("简历上传成功: userId={}, file={}, chars={}", userId, filename, text.length());
            return text;
        } catch (IOException e) {
            throw new RuntimeException("文件处理失败: " + e.getMessage());
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
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        user.setResumeText(null);
        user.setResumeFileName(null);
        userRepository.save(user);
        log.info("简历已删除 userId={}", userId);
    }

    private String extractText(MultipartFile file, String ext) throws IOException {
        byte[] bytes = file.getBytes();
        String text;

        if (ext.equals(".pdf")) {
            text = extractPdfText(bytes);
        } else if (ext.equals(".docx")) {
            text = extractDocxText(bytes);
        } else {
            throw new RuntimeException("不支持的文件格式: " + ext);
        }

        if (text.isBlank()) {
            throw new RuntimeException("无法提取到文本内容，请检查文件");
        }

        // 限制文本长度，避免超出 token 限制
        if (text.length() > 5000) {
            text = text.substring(0, 5000);
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
        try (XWPFDocument doc = new XWPFDocument(new java.io.ByteArrayInputStream(bytes));
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }
}
