package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.AiSuggestionResult;
import com.exam.ideaforge.dto.IdeaProcessRequest;
import com.exam.ideaforge.dto.IdeaProcessResponse;
import com.exam.ideaforge.entity.IdeaCategory;
import com.exam.ideaforge.exception.IdeaProcessingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * AI 整理服务：调用 DeepSeek 将原始想法转为结构化建议。
 * 本阶段仅返回结果，不写入数据库（两阶段流程的第一步）。
 */
@Service
public class IdeaProcessService {

    /** 约束 AI 输出格式：标题、摘要、标签、类别 */
    private static final String SYSTEM_PROMPT = """
            你是个人知识整理助手。根据用户提供的原始想法，生成结构化建议。
            类别必须是以下之一：WORK、STUDY、LIFE、INSPIRATION、TODO。
            标签 3-5 个，简洁中文。摘要一句话，不超过 80 字。标题简洁有力。
            """;

    private final ChatClient chatClient;

    public IdeaProcessService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /** 调用 DeepSeek，将 AI 响应反序列化为 AiSuggestionResult 后转为 IdeaProcessResponse */
    public IdeaProcessResponse process(IdeaProcessRequest request) {
        String userPrompt = buildUserPrompt(request);

        AiSuggestionResult result;
        try {
            result = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userPrompt)
                    .call()
                    .entity(AiSuggestionResult.class);
        } catch (Exception ex) {
            throw new IdeaProcessingException("AI 整理失败，请稍后重试", ex);
        }

        if (result == null || result.suggestedTitle() == null || result.suggestedTitle().isBlank()) {
            throw new IdeaProcessingException("AI 返回结果无效，请重试");
        }

        return IdeaProcessResponse.builder()
                .suggestedTitle(result.suggestedTitle().trim())
                .suggestedSummary(result.suggestedSummary() != null ? result.suggestedSummary().trim() : "")
                .suggestedTags(result.suggestedTags() != null ? result.suggestedTags() : java.util.List.of())
                .suggestedCategory(parseCategory(result.suggestedCategory()))
                .build();
    }

    private String buildUserPrompt(IdeaProcessRequest request) {
        StringBuilder prompt = new StringBuilder("请整理以下想法：\n");
        if (request.getOriginalTitle() != null && !request.getOriginalTitle().isBlank()) {
            prompt.append("标题：").append(request.getOriginalTitle().trim()).append("\n");
        }
        prompt.append("正文：").append(request.getOriginalContent().trim());
        return prompt.toString();
    }

    private IdeaCategory parseCategory(String category) {
        if (category == null || category.isBlank()) {
            return IdeaCategory.INSPIRATION;
        }
        try {
            return IdeaCategory.valueOf(category.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IdeaCategory.INSPIRATION;
        }
    }
}
