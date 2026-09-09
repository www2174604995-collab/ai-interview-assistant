package com.aiinterview.exception;

import org.springframework.http.HttpStatus;

/**
 * 文件处理异常
 */
public class FileProcessException extends BusinessException {
    public FileProcessException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "FILE_PROCESS_ERROR");
    }

    public FileProcessException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, "FILE_PROCESS_ERROR", cause);
    }
}
