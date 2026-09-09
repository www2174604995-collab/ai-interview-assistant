package com.aiinterview.constant;

/**
 * 面试相关常量
 */
public class InterviewConstants {
    private InterviewConstants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    // 面试状态
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";

    // 消息角色
    public static final String ROLE_USER = "user";
    public static final String ROLE_ASSISTANT = "assistant";

    // 面试标记
    public static final String INTERVIEW_END_MARKER = "面试结束";

    // 错误消息
    public static final String ERROR_SESSION_NOT_FOUND = "面试会话不存在";
    public static final String ERROR_SESSION_NOT_COMPLETED = "面试未结束，无法生成报告";
    public static final String ERROR_SESSION_ALREADY_COMPLETED = "面试已结束，无法继续对话";
    public static final String ERROR_REPORT_NOT_GENERATED = "报告尚未生成";
}
