package com.aiinterview.service.impl;

import com.aiinterview.constant.AuthConstants;
import com.aiinterview.constant.InterviewConstants;
import com.aiinterview.exception.BusinessException;
import com.aiinterview.exception.ResourceNotFoundException;
import com.aiinterview.model.dto.ReportResponse;
import com.aiinterview.model.entity.InterviewSession;
import com.aiinterview.model.entity.User;
import com.aiinterview.repository.InterviewMessageRepository;
import com.aiinterview.repository.InterviewSessionRepository;
import com.aiinterview.repository.UserRepository;
import com.aiinterview.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private static final String REPORT_PROMPT = """
你是一位专业的面试评估专家。请根据以下面试对话记录生成详细的面试评估报告。
报告需要包含以下部分：
1. **总体评价**：对候选人整体表现的概括性评价
2. **技术能力评估**：分析技术知识掌握程度，包括深度和广度
3. **沟通表达能力**：评估表达清晰度和逻辑性
4. **优势与亮点**：指出面试中表现突出的方面
5. **待改进之处**：给出具体的改进建议
6. **综合评分**：满分100分，给出具体分数

面试职位：%s
面试难度：%s

面试对话记录：
%s

请用中文输出报告。""";

    private final InterviewSessionRepository sessionRepository;
    private final InterviewMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String modelName;

    private ChatClient buildChatClient(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));
        String apiKey = user.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(AuthConstants.ERROR_API_KEY_NOT_CONFIGURED);
        }
        OpenAiApi api = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(modelName)
                .build();
        OpenAiChatModel chatModel = new OpenAiChatModel(api, options);
        return ChatClient.builder(chatModel).build();
    }

    @Override
    @Transactional
    public ReportResponse generateReport(Long userId, Long sessionId) {
        ChatClient chatClient = buildChatClient(userId);

        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(InterviewConstants.ERROR_SESSION_NOT_FOUND));
        
        if (!InterviewConstants.STATUS_COMPLETED.equals(session.getStatus())) {
            throw new BusinessException(InterviewConstants.ERROR_SESSION_NOT_COMPLETED);
        }

        String conversation = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> (InterviewConstants.ROLE_USER.equals(m.getRole()) ? "候选人" : "面试官") + ": " + m.getContent())
                .collect(Collectors.joining("\n\n"));

        String report = chatClient.prompt()
                .user(String.format(REPORT_PROMPT, session.getPosition(), session.getDifficulty(), conversation))
                .call().content();

        long qCount = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .filter(m -> InterviewConstants.ROLE_ASSISTANT.equals(m.getRole())).count();

        session.setReport(report);
        session.setTotalQuestions((int) qCount);
        sessionRepository.save(session);

        log.info("Report generated for session: sessionId={}", sessionId);

        return ReportResponse.builder()
                .sessionId(session.getId())
                .position(session.getPosition())
                .difficulty(session.getDifficulty())
                .totalQuestions((int) qCount)
                .totalTurns(session.getTotalTurns())
                .report(report)
                .createdAt(session.getCreatedAt())
                .build();
    }

    @Override
    public ReportResponse getReport(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(InterviewConstants.ERROR_SESSION_NOT_FOUND));
        
        if (session.getReport() == null) {
            throw new BusinessException(InterviewConstants.ERROR_REPORT_NOT_GENERATED);
        }
        
        return ReportResponse.builder()
                .sessionId(session.getId())
                .position(session.getPosition())
                .difficulty(session.getDifficulty())
                .totalQuestions(session.getTotalQuestions())
                .totalTurns(session.getTotalTurns())
                .report(session.getReport())
                .createdAt(session.getCreatedAt())
                .build();
    }
}
