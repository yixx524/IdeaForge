package com.exam.ideaforge.dto;

import lombok.Builder;
import lombok.Getter;

/** POST /api/ideas/parse-document 响应：从 Word/PDF 提取的文本 */
@Getter
@Builder
public class DocumentParseResponse {

    private final String fileName;
    private final String extractedTitle;
    private final String extractedContent;
    private final int charCount;
}
