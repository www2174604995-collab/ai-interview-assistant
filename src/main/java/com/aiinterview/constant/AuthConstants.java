package com.aiinterview.constant;

/**
 * 认证相关常量
 */
public class AuthConstants {
    private AuthConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // 错误消息
    public static final String ERROR_USERNAME_EXISTS = "用户名已存在";
    public static final String ERROR_EMAIL_EXISTS = "邮箱已被注册";
    public static final String ERROR_INVALID_CREDENTIALS = "用户名或密码错误";
    public static final String ERROR_API_KEY_NOT_CONFIGURED = "请先在「设置」页面配置你的 DeepSeek API Key";
    public static final String ERROR_API_KEY_INVALID = "API Key 格式不正确";
}
