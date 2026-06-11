package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.AiSuggestionResult;
import com.exam.ideaforge.dto.IdeaProcessRequest;
import com.exam.ideaforge.dto.IdeaProcessResponse;
import com.exam.ideaforge.exception.IdeaProcessingException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 整理服务：调用 DeepSeek 将原始想法转为结构化建议。
 * 本阶段仅返回结果，不写入数据库（两阶段流程的第一步）。
 */
@Service
public class IdeaProcessService {

    private static final long SSE_TIMEOUT_MS = 300_000L;

    private static final Pattern STRING_FIELD_PATTERN = Pattern.compile(
            "\"(?<field>suggestedTitle|suggestedSummary|suggestedCategory|suggestedContent)\"\\s*:\\s*\""
    );

    private static final Pattern TAGS_FIELD_PATTERN = Pattern.compile(
            "\"suggestedTags\"\\s*:\\s*\\[(?<values>.*?)]",
            Pattern.DOTALL
    );

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            你是个人知识整理助手。根据用户提供的原始想法，生成结构化建议。
            类别必须是以下 code 之一：%s。
            标签 3-5 个，简洁中文。摘要一句话，不超过 80 字。标题简洁有力。
            同时输出 suggestedContent：对原始正文做排版整理，使用结构清晰的 Markdown：
            - 空行分段，小节标题与列表层次分明
            - 列表项用 - 开头
            - 修正多余空行与乱码空格，不删改原意，保持内容完整
            - 前端会将 Markdown 转为富文本展示，无需输出 HTML
            """;

    private final ChatClient chatClient;
    private final CategoryService categoryService;
    private final BeanOutputConverter<AiSuggestionResult> outputConverter;

    public IdeaProcessService(ChatClient.Builder chatClientBuilder, CategoryService categoryService) {
        this.chatClient = chatClientBuilder.build();
        this.categoryService = categoryService;
        this.outputConverter = new BeanOutputConverter<>(AiSuggestionResult.class);
    }

    /** 同步整理（保留兼容）；流式场景请使用 {@link #streamProcess(IdeaProcessRequest)} */
    public IdeaProcessResponse process(IdeaProcessRequest request) {
        ProcessContext context = prepareContext(request);

        AiSuggestionResult result;
        try {
            result = chatClient.prompt()
                    .system(context.systemPrompt())
                    .user(context.userPrompt())
                    .call()
                    .entity(AiSuggestionResult.class);
        } catch (Exception ex) {
            throw new IdeaProcessingException(resolveAiErrorMessage(ex), ex);
        }

        return buildResponse(result);
    }

    /**
     * SSE 流式整理：推送 partial / delta / complete 事件，避免长文本阻塞 HTTP。
     * partial：当前可解析的结构化快照；delta：suggestedContent 增量文本；complete：最终校验结果。
     */
    public SseEmitter streamProcess(IdeaProcessRequest request) {
        StreamContext context = prepareStreamContext(request);
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        StringBuilder buffer = new StringBuilder();
        AtomicReference<AiSuggestionResult> latest = new AtomicReference<>();
        AtomicReference<String> lastContentSent = new AtomicReference<>("");

        Disposable subscription = chatClient.prompt()
                .system(context.systemPrompt())
                .user(context.userPrompt())
                .stream()
                .content()
                .subscribe(
                        chunk -> handleStreamChunk(emitter, buffer, latest, lastContentSent, chunk),
                        error -> completeWithError(emitter, resolveAiErrorMessage(error)),
                        () -> completeStream(emitter, buffer.toString(), latest.get())
                );

        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            completeWithError(emitter, "AI 整理超时，请稍后重试");
        });
        emitter.onError(ex -> subscription.dispose());

        return emitter;
    }

    private ProcessContext prepareContext(IdeaProcessRequest request) {
        List<String> enabledCodes = categoryService.getEnabledCodes();
        if (enabledCodes.isEmpty()) {
            throw new IdeaProcessingException("没有可用的类别，请先在类别管理中新增");
        }

        String systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(String.join("、", enabledCodes));
        return new ProcessContext(systemPrompt, buildUserPrompt(request));
    }

    private StreamContext prepareStreamContext(IdeaProcessRequest request) {
        ProcessContext base = prepareContext(request);
        String systemPrompt = base.systemPrompt() + "\n\n" + outputConverter.getFormat();
        return new StreamContext(systemPrompt, base.userPrompt());
    }

    private void handleStreamChunk(
            SseEmitter emitter,
            StringBuilder buffer,
            AtomicReference<AiSuggestionResult> latest,
            AtomicReference<String> lastContentSent,
            String chunk) {
        buffer.append(chunk);
        AiSuggestionResult partial = parsePartialResult(buffer.toString());
        if (partial == null) {
            return;
        }

        latest.set(partial);

        String content = partial.suggestedContent() != null ? partial.suggestedContent() : "";
        String previous = lastContentSent.get();
        if (content.length() > previous.length()) {
            sendEvent(emitter, "delta", Map.of(
                    "field", "suggestedContent",
                    "text", content.substring(previous.length()),
                    "value", content
            ));
            lastContentSent.set(content);
        }

        sendEvent(emitter, "partial", toPartialResponse(partial));
    }

    private void completeStream(SseEmitter emitter, String rawResponse, AiSuggestionResult latestPartial) {
        try {
            AiSuggestionResult result = parseFinalResult(rawResponse, latestPartial);
            sendEvent(emitter, "complete", buildResponse(result));
            emitter.complete();
        } catch (IdeaProcessingException ex) {
            completeWithError(emitter, ex.getMessage());
        }
    }

    private AiSuggestionResult parseFinalResult(String rawResponse, AiSuggestionResult latestPartial) {
        try {
            return outputConverter.convert(extractJsonPayload(rawResponse));
        } catch (Exception ex) {
            if (latestPartial != null
                    && latestPartial.suggestedTitle() != null
                    && !latestPartial.suggestedTitle().isBlank()) {
                return latestPartial;
            }
            throw new IdeaProcessingException("AI 返回结果无效，请重试", ex);
        }
    }

    private AiSuggestionResult parsePartialResult(String rawResponse) {
        String payload = extractJsonPayload(rawResponse);
        if (payload.isBlank()) {
            return null;
        }

        String title = readStringField(payload, "suggestedTitle", false);
        String summary = readStringField(payload, "suggestedSummary", false);
        String category = readStringField(payload, "suggestedCategory", false);
        String content = readStringField(payload, "suggestedContent", true);
        List<String> tags = readTagsField(payload);

        if (title == null && summary == null && category == null && content == null && tags.isEmpty()) {
            return null;
        }

        return new AiSuggestionResult(title, summary, tags, category, content);
    }

    private List<String> readTagsField(String payload) {
        Matcher matcher = TAGS_FIELD_PATTERN.matcher(payload);
        if (!matcher.find()) {
            return List.of();
        }

        String values = matcher.group("values").trim();
        if (values.isEmpty()) {
            return List.of();
        }

        List<String> tags = new ArrayList<>();
        Matcher tagMatcher = Pattern.compile("\"((?:\\\\.|[^\"\\\\])*)\"").matcher(values);
        while (tagMatcher.find()) {
            tags.add(unescapeJsonString(tagMatcher.group(1)));
        }
        return tags;
    }

    private String readStringField(String payload, String fieldName, boolean allowIncomplete) {
        Matcher matcher = STRING_FIELD_PATTERN.matcher(payload);
        while (matcher.find()) {
            if (!fieldName.equals(matcher.group("field"))) {
                continue;
            }

            int valueStart = matcher.end();
            String value = readJsonString(payload, valueStart, allowIncomplete);
            return value.isEmpty() ? null : value;
        }
        return null;
    }

    private String readJsonString(String payload, int openingQuoteIndex, boolean allowIncomplete) {
        if (openingQuoteIndex >= payload.length() || payload.charAt(openingQuoteIndex) != '"') {
            return "";
        }

        StringBuilder value = new StringBuilder();
        int index = openingQuoteIndex + 1;
        while (index < payload.length()) {
            char current = payload.charAt(index);
            if (current == '\\' && index + 1 < payload.length()) {
                value.append(unescapeJsonChar(payload.charAt(index + 1)));
                index += 2;
                continue;
            }
            if (current == '"') {
                return value.toString();
            }
            value.append(current);
            index++;
        }

        return allowIncomplete ? value.toString() : "";
    }

    private static char unescapeJsonChar(char escaped) {
        return switch (escaped) {
            case 'n' -> '\n';
            case 'r' -> '\r';
            case 't' -> '\t';
            case '"' -> '"';
            case '\\' -> '\\';
            default -> escaped;
        };
    }

    private static String unescapeJsonString(String value) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            if (current == '\\' && index + 1 < value.length()) {
                builder.append(unescapeJsonChar(value.charAt(index + 1)));
                index++;
            } else {
                builder.append(current);
            }
        }
        return builder.toString();
    }

    private String extractJsonPayload(String rawResponse) {
        String trimmed = rawResponse.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }

        int firstLineBreak = trimmed.indexOf('\n');
        int lastFence = trimmed.lastIndexOf("```");
        if (firstLineBreak >= 0 && lastFence > firstLineBreak) {
            return trimmed.substring(firstLineBreak + 1, lastFence).trim();
        }
        return trimmed;
    }

    private IdeaProcessResponse buildResponse(AiSuggestionResult result) {
        if (result == null || result.suggestedTitle() == null || result.suggestedTitle().isBlank()) {
            throw new IdeaProcessingException("AI 返回结果无效，请重试");
        }

        String resolvedCategory = categoryService.resolveCategoryCode(result.suggestedCategory());

        return IdeaProcessResponse.builder()
                .suggestedTitle(result.suggestedTitle().trim())
                .suggestedSummary(trimToEmpty(result.suggestedSummary()))
                .suggestedTags(result.suggestedTags() != null ? result.suggestedTags() : List.of())
                .suggestedCategory(resolvedCategory)
                .suggestedContent(trimToEmpty(result.suggestedContent()))
                .build();
    }

    private IdeaProcessResponse toPartialResponse(AiSuggestionResult result) {
        if (result == null) {
            return IdeaProcessResponse.builder().build();
        }

        return IdeaProcessResponse.builder()
                .suggestedTitle(trimToNull(result.suggestedTitle()))
                .suggestedSummary(trimToNull(result.suggestedSummary()))
                .suggestedTags(result.suggestedTags() != null ? result.suggestedTags() : List.of())
                .suggestedCategory(trimToNull(result.suggestedCategory()))
                .suggestedContent(trimToNull(result.suggestedContent()))
                .build();
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object data) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(data, org.springframework.http.MediaType.APPLICATION_JSON));
        } catch (IOException ex) {
            emitter.completeWithError(ex);
        }
    }

    private void completeWithError(SseEmitter emitter, String message) {
        try {
            sendEvent(emitter, "error", Map.of("message", message));
        } catch (Exception ignored) {
            // emitter may already be closed
        }
        emitter.complete();
    }

    private String buildUserPrompt(IdeaProcessRequest request) {
        StringBuilder prompt = new StringBuilder("请整理以下想法：\n");
        if (request.getOriginalTitle() != null && !request.getOriginalTitle().isBlank()) {
            prompt.append("标题：").append(request.getOriginalTitle().trim()).append("\n");
        }
        prompt.append("正文：").append(request.getOriginalContent().trim());
        return prompt.toString();
    }

    private String resolveAiErrorMessage(Throwable ex) {
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

    private static String trimToEmpty(String value) {
        return value != null ? value.trim() : "";
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record ProcessContext(String systemPrompt, String userPrompt) {
    }

    private record StreamContext(String systemPrompt, String userPrompt) {
    }
}
