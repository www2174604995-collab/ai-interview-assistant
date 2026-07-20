# AI 模拟面试助手

基于 Spring Boot 3 + Spring AI + DeepSeek API 构建的智能模拟面试平台。支持多轮技术面试、简历解析、AI 评估报告，帮助求职者高效备战技术面试。

## ✨ 功能

- **用户认证**：注册 / 登录，JWT Token 鉴权，密码 BCrypt 加密
- **简历管理**：上传 PDF/DOCX 简历，自动提取文本
- **AI 面试**：选择岗位 + 难度，AI 基于简历出题，支持多轮对话 + SSE 流式输出
- **评估报告**：面试结束后 AI 自动生成结构化报告（技术能力、沟通、评分）
- **API Key 管理**：用户自行配置 DeepSeek / OpenAI 兼容 API Key
- **面试历史**：查看所有记录，浏览对话详情

## 🛠 技术栈

| 类别 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.5, Spring AI, Spring Security, JPA |
| 认证 | JWT (jjwt 0.12.5) |
| 数据库 | MySQL 8.0 |
| 文件解析 | PDFBox 3.0.1 (PDF), Apache POI 5.2.5 (DOCX) |
| 前端 | 纯 HTML/CSS/JS SPA |
| 部署 | Docker + Docker Compose |
| 构建 | Maven, Java 21 |

## 🚀 快速启动

### 前置条件

- JDK 17+
- Maven 3.6+
- MySQL 8.0+（或使用 H2 内存数据库）
- DeepSeek API Key

### 运行步骤

```bash
# 1. 克隆项目
git clone https://github.com/你的用户名/ai-interview-assistant.git
cd ai-interview-assistant

# 2. 配置 application.yml 中的数据库和 API Key

# 3. 启动 MySQL（或使用 docker-compose）
docker-compose up -d

# 4. 运行项目
mvn spring-boot:run

# 5. 访问
http://localhost:8080

├── src/main/java/com/aiinterview/
│   ├── config/          # JWT, Security, CORS, Spring AI 配置
│   ├── controller/      # 认证、面试、简历、报告 API
│   ├── service/         # 业务逻辑
│   ├── model/           # 实体、DTO、VO
│   ├── repository/      # JPA 数据访问
│   └── exception/       # 全局异常处理
├── src/main/resources/
│   ├── application.yml  # 主配置
│   └── static/          # 前端页面
├── sql/schema.sql       # 建表脚本
├── Dockerfile
├── docker-compose.yml
└── pom.xml

<img width="979" height="481" alt="屏幕截图 2026-07-20 175120" src="https://github.com/user-attachments/assets/91c6d710-ee87-445f-b710-aa19eb08936f" />
<img width="881" height="503" alt="屏幕截图 2026-07-20 175200" src="https://github.com/user-attachments/assets/107ad5d1-4083-4f11-8955-0a6e61b247fd" />
<img width="878" height="500" alt="屏幕截图 2026-07-20 175213" src="https://github.com/user-attachments/assets/844859bc-b12c-4869-af4a-81adc618af1d" />

