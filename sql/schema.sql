-- ============================================
-- AI Interview Assistant Database Schema
-- ============================================

CREATE DATABASE IF NOT EXISTS ai_interview DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ai_interview;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(50),
    resume_text TEXT COMMENT '简历文本内容',
    resume_file_name VARCHAR(255) COMMENT '简历文件名',
    api_key VARCHAR(255) COMMENT 'DeepSeek API Key',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 面试会话表
CREATE TABLE IF NOT EXISTS interview_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    position VARCHAR(100) NOT NULL COMMENT '面试职位',
    difficulty VARCHAR(20) NOT NULL COMMENT '面试难度',
    status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' COMMENT 'IN_PROGRESS / COMPLETED',
    total_questions INT DEFAULT 0 COMMENT '问题总数',
    total_turns INT DEFAULT 0 COMMENT '对话轮次',
    report TEXT COMMENT '评估报告',
    started_at DATETIME COMMENT '面试开始时间',
    ended_at DATETIME COMMENT '面试结束时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 面试消息表
CREATE TABLE IF NOT EXISTS interview_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL COMMENT 'user / assistant',
    content TEXT NOT NULL COMMENT '消息内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
