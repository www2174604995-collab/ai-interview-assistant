package com.aiinterview.service.impl;

import com.aiinterview.model.dto.*;
import com.aiinterview.model.entity.InterviewMessage;
import com.aiinterview.model.entity.InterviewSession;
import com.aiinterview.model.entity.User;
import com.aiinterview.repository.InterviewMessageRepository;
import com.aiinterview.repository.InterviewSessionRepository;
import com.aiinterview.repository.UserRepository;
import com.aiinterview.service.InterviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewServiceImpl implements InterviewService {
    private static final String SYSTEM_PROMPT = """
?????????????????%s??????????%s??
??????????
1. ??????????????
2. ??????????????????????
3. ??????????????????????
4. ???????????
5. ??????????5-8???????????????????????
6. ????????"??????"???
????????""";


    private final ChatMemory chatMemory;
    private final InterviewSessionRepository sessionRepository;
    private final InterviewMessageRepository messageRepository;
    private final UserRepository userRepository;

    @Value("${spring.ai.openai.base-url:https://api.deepseek.com}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model:deepseek-v4-flash}")
    private String modelName;

    private ChatClient buildChatClient(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("?????"));
        String apiKey = user.getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new RuntimeException("?????????????DeepSeek API Key");
        }
        OpenAiApi api = new OpenAiApi(baseUrl, apiKey);
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withModel(modelName)
                .build();
        OpenAiChatModel chatModel = new OpenAiChatModel(api, options);
        return ChatClient.builder(chatModel).build();
    }

    private String buildSystemPrompt(InterviewSession session, Long userId) {
        String resumeText = userRepository.findById(userId)
                .map(User::getResumeText).orElse(null);
        if (resumeText != null && !resumeText.isBlank()) {
            return "????????????\n" + resumeText + "\n\n" +
                    String.format(SYSTEM_PROMPT, session.getPosition(), session.getDifficulty()) +
                    "\n?????????????????????????????";
        }
        return String.format(SYSTEM_PROMPT, session.getPosition(), session.getDifficulty());
    }

    private void ensureConversationLoaded(InterviewSession session, Long userId, String convId) {
        List<Message> existing = chatMemory.get(convId, 1);
        if (!existing.isEmpty()) {
            return;
        }
        chatMemory.add(convId, new UserMessage(buildSystemPrompt(session, userId)));
        messageRepository.findBySessionIdOrderByCreatedAtAsc(session.getId()).forEach(m -> {
            if ("user".equals(m.getRole())) {
                chatMemory.add(convId, new UserMessage(m.getContent()));
            } else {
                chatMemory.add(convId, new AssistantMessage(m.getContent()));
            }
        });
    }

    @Override
    @Transactional
    public ChatResponse startInterview(Long userId, StartInterviewRequest request) {
        ChatClient chatClient = buildChatClient(userId);

        InterviewSession session = InterviewSession.builder()
                .userId(userId).position(request.getPosition())
                .difficulty(request.getDifficulty()).status("IN_PROGRESS")
                .totalQuestions(0).totalTurns(0).build();
        sessionRepository.save(session);

        String convId = "interview:" + session.getId();
        chatMemory.add(convId, new UserMessage(buildSystemPrompt(session, userId)));

        String reply = chatClient.prompt()
                .messages(chatMemory.get(convId, 20))
                .call()
                .content();

        chatMemory.add(convId, new AssistantMessage(reply));
        messageRepository.save(InterviewMessage.builder()
                .sessionId(session.getId()).role("assistant").content(reply).build());

        session.setTotalTurns(1);
        boolean completed = reply.contains("??????");
        if (completed) {
            session.setStatus("COMPLETED");
            session.setEndedAt(LocalDateTime.now());
        }
        sessionRepository.save(session);

        return ChatResponse.builder()
                .sessionId(session.getId()).reply(reply).completed(completed).build();
    }

    @Override
    @Transactional
    public ChatResponse chat(Long userId, ChatRequest request) {
        ChatClient chatClient = buildChatClient(userId);

        InterviewSession session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                .orElseThrow(() -> new RuntimeException("???????"));
        if ("COMPLETED".equals(session.getStatus())) {
            throw new RuntimeException("?????");
        }

        String convId = "interview:" + session.getId();
        ensureConversationLoaded(session, userId, convId);
        chatMemory.add(convId, new UserMessage(request.getMessage()));
        messageRepository.save(InterviewMessage.builder()
                .sessionId(session.getId()).role("user").content(request.getMessage()).build());

        String reply = chatClient.prompt()
                .messages(chatMemory.get(convId, 20))
                .call()
                .content();

        chatMemory.add(convId, new AssistantMessage(reply));
        messageRepository.save(InterviewMessage.builder()
                .sessionId(session.getId()).role("assistant").content(reply).build());

        session.setTotalTurns(session.getTotalTurns() + 1);
        boolean completed = reply.contains("??????");
        if (completed) {
            session.setStatus("COMPLETED");
            session.setEndedAt(LocalDateTime.now());
        }
        sessionRepository.save(session);

        return ChatResponse.builder()
                .sessionId(session.getId()).reply(reply).completed(completed).build();
    }

    @Override
    public void chatStream(Long userId, ChatRequest request, SseEmitter emitter) {
        try {
            ChatClient chatClient = buildChatClient(userId);

            InterviewSession session = sessionRepository.findByIdAndUserId(request.getSessionId(), userId)
                    .orElseThrow(() -> new RuntimeException("???????"));
            if ("COMPLETED".equals(session.getStatus())) {
                throw new RuntimeException("?????");
            }

            String convId = "interview:" + session.getId();
            ensureConversationLoaded(session, userId, convId);
            chatMemory.add(convId, new UserMessage(request.getMessage()));
            messageRepository.save(InterviewMessage.builder()
                    .sessionId(session.getId()).role("user").content(request.getMessage()).build());

            StringBuilder fullReply = new StringBuilder();

            chatClient.prompt()
                    .messages(chatMemory.get(convId, 20))
                    .stream()
                    .content()
                    .doOnNext(chunk -> {
                        fullReply.append(chunk);
                        try {
                            emitter.send(SseEmitter.event().name("chunk").data(chunk));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .doOnComplete(() -> {
                        try {
                            String reply = fullReply.toString();
                            chatMemory.add(convId, new AssistantMessage(reply));
                            messageRepository.save(InterviewMessage.builder()
                                    .sessionId(session.getId()).role("assistant").content(reply).build());

                            session.setTotalTurns(session.getTotalTurns() + 1);
                            boolean completed = reply.contains("??????");
                            if (completed) {
                                session.setStatus("COMPLETED");
                                session.setEndedAt(LocalDateTime.now());
                            }
                            sessionRepository.save(session);
                            emitter.send(SseEmitter.event().name("done")
                                    .data("{\"completed\":" + completed + ",\"sessionId\":" + session.getId() + "}"));
                            emitter.complete();
                        } catch (IOException e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .doOnError(e -> {
                        try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); }
                        catch (IOException ex) { /* ignore */ }
                        emitter.completeWithError(e);
                    })
                    .subscribe();
        } catch (Exception e) {
            try { emitter.send(SseEmitter.event().name("error").data(e.getMessage())); }
            catch (IOException ex) { /* ignore */ }
            emitter.completeWithError(e);
        }
    }

    @Override
    @Transactional
    public void endInterview(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("???????"));
        if ("COMPLETED".equals(session.getStatus())) {
            throw new RuntimeException("?????");
        }
        session.setStatus("COMPLETED");
        session.setEndedAt(LocalDateTime.now());
        sessionRepository.save(session);
    }

    @Override
    public List<SessionSummary> getHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(s -> SessionSummary.builder().id(s.getId())
                        .position(s.getPosition()).difficulty(s.getDifficulty())
                        .status(s.getStatus()).totalQuestions(s.getTotalQuestions())
                        .totalTurns(s.getTotalTurns()).createdAt(s.getCreatedAt())
                        .endedAt(s.getEndedAt())
                        .messageCount(messageRepository.countBySessionIdAndRole(s.getId(), "assistant")
                                + messageRepository.countBySessionIdAndRole(s.getId(), "user"))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<SessionSummary> getSessions(Long userId) {
        return getHistory(userId);
    }

    @Override
    public SessionDetailResponse getSessionDetail(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new RuntimeException("???????"));
        List<MessageDto> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(m -> MessageDto.builder()
                        .id(m.getId()).role(m.getRole()).content(m.getContent())
                        .createdAt(m.getCreatedAt()).build())
                .collect(Collectors.toList());
        return SessionDetailResponse.builder()
                .sessionId(session.getId()).position(session.getPosition())
                .difficulty(session.getDifficulty()).status(session.getStatus())
                .totalQuestions(session.getTotalQuestions()).totalTurns(session.getTotalTurns())
                .createdAt(session.getCreatedAt()).messages(messages).build();
    }
}
