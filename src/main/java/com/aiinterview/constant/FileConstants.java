package com.aiinterview.constant;

/**
 * 文件处理相关常量
 */
public class FileConstants {
    private FileConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // 支持的文件扩展名
    public static final String PDF_EXTENSION = ".pdf";
    public static final String DOCX_EXTENSION = ".docx";

    // 文件大小限制 (5MB)
    public static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // 文本长度限制
    public static final int MAX_RESUME_TEXT_LENGTH = 5000;

    // 错误消息
    public static final String ERROR_USER_NOT_FOUND = "用户不存在";
    public static final String ERROR_FILE_NAME_EMPTY = "文件名不能为空";
    public static final String ERROR_UNSUPPORTED_FILE_FORMAT = "仅支持 PDF 和 DOCX 格式";
    public static final String ERROR_FILE_TOO_LARGE = "文件超过 5MB 限制";
    public static final String ERROR_NO_TEXT_EXTRACTED = "无法提取到文本内容，请检查文件";
    public static final String ERROR_FILE_PROCESS_FAILED = "文件处理失败";
}
