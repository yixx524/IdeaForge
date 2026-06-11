package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.AiSuggestionResult;
import com.exam.ideaforge.dto.IdeaProcessRequest;
import com.exam.ideaforge.dto.IdeaProcessResponse;
import com.exam.ideaforge.exception.IdeaProcessingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * AI 整理服务：调用 DeepSeek 将原始想法转为结构化建议。
 * 本阶段仅返回结果，不写入数据库（两阶段流程的第一步）。
 */
@Service
public class IdeaProcessService {

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是个人知识整理助手。根据用户提供的原始想法，生成结构化建议。
            类别必须是以下 code 之一：%s。
            标签 3-5 个，简洁中文。摘要一句话，不超过 80 字。标题简洁有力。
            同时输出 suggestedContent：对原始正文做排版整理，使用轻量 Markdown：
            - 空行分段
            - 小节标题用 ## 开头
            - 列表项用 - 开头
            - 修正多余空行与乱码空格，不删改原意，保持内容完整
            """;

    private final ChatClient chatClient;
    private final CategoryService categoryService;

    public IdeaProcessService(ChatClient.Builder chatClientBuilder, CategoryService categoryService) {
        this.chatClient = chatClientBuilder.build();
        this.categoryService = categoryService;
    }

    /** 调用 DeepSeek，将 AI 响应反序列化为 AiSuggestionResult 后转为 IdeaProcessResponse */
    public IdeaProcessResponse process(IdeaProcessRequest request) {
        List<String> enabledCodes = categoryService.getEnabledCodes();
        if (enabledCodes.isEmpty()) {
            throw new IdeaProcessingException("没有可用的类别，请先在类别管理中新增");
        }

        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(String.join("、", enabledCodes));
        String userPrompt = buildUserPrompt(request);

        AiSuggestionResult result;
        try {
            result = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .entity(AiSuggestionResult.class);
        } catch (Exception ex) {
            throw new IdeaProcessingException(resolveAiErrorMessage(ex), ex);
        }

        if (result == null || result.suggestedTitle() == null || result.suggestedTitle().isBlank()) {
            throw new IdeaProcessingException("AI 返回结果无效，请重试");
        }

        String resolvedCategory = categoryService.resolveCategoryCode(result.suggestedCategory());

        return IdeaProcessResponse.builder()
                .suggestedTitle(result.suggestedTitle().trim())
                .suggestedSummary(result.suggestedSummary() != null ? result.suggestedSummary().trim() : "")
                .suggestedTags(result.suggestedTags() != null ? result.suggestedTags() : List.of())
                .suggestedCategory(resolvedCategory)
                .suggestedContent(result.suggestedContent() != null ? result.suggestedContent().trim() : "")
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

    private String resolveAiErrorMessage(Exception ex) {
        Throwable current = ex;
        while (current != null) {
            String message = current.getMessage();
            if (message != null) {
                if (message.contains("401") || message.contains("authentication_error")) {
                    return "DeepSeek API Key 无效或未配置，请设置环境变量 DEEPSEEK_API_KEY 后重启后端";
                }
                if (message.contains("402") || message.contains("insufficient")) {
                    return "DeepSeek 账户余额不足，请充值后重试";
                }
            }
            current = current.getCause();
        }
        return "AI 整理失败，请稍后重试";
    }
}
