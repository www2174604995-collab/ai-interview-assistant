package com.aiinterview.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务异常 - 用于表示业务逻辑中的异常情况
 */
public class BusinessException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String code;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR");
    }

    public BusinessException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, "BUSINESS_ERROR");
    }

    public BusinessException(String message, HttpStatus httpStatus, String code) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        this(message, HttpStatus.BAD_REQUEST, "BUSINESS_ERROR", cause);
    }

    public BusinessException(String message, HttpStatus httpStatus, String code, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }
}
