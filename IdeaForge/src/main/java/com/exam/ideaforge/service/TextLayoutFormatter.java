package com.exam.ideaforge.service;

import java.util.ArrayList;
import java.util.List;

/** 轻量 Markdown 文本排版解析，供 Word 导出使用 */
public final class TextLayoutFormatter {

    public enum BlockType {
        HEADING2,
        LIST_ITEM,
        PARAGRAPH
    }

    public record TextBlock(BlockType type, String text) {}

    private TextLayoutFormatter() {
    }

    public static List<TextBlock> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        String normalized = raw.replace("\r\n", "\n").replace('\r', '\n').trim();
        String[] lines = normalized.split("\n", -1);
        List<TextBlock> blocks = new ArrayList<>();
        StringBuilder paragraph = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.stripTrailing();

            if (trimmed.isBlank()) {
                flushParagraph(blocks, paragraph);
                continue;
            }

            if (trimmed.startsWith("## ")) {
                flushParagraph(blocks, paragraph);
                blocks.add(new TextBlock(BlockType.HEADING2, trimmed.substring(3).trim()));
                continue;
            }

            if (trimmed.startsWith("- ") || trimmed.startsWith("* ")) {
                flushParagraph(blocks, paragraph);
                blocks.add(new TextBlock(BlockType.LIST_ITEM, trimmed.substring(2).trim()));
                continue;
            }

            if (!paragraph.isEmpty()) {
                paragraph.append(' ');
            }
            paragraph.append(trimmed);
        }

        flushParagraph(blocks, paragraph);
        return blocks;
    }

    private static void flushParagraph(List<TextBlock> blocks, StringBuilder paragraph) {
        if (!paragraph.isEmpty()) {
            blocks.add(new TextBlock(BlockType.PARAGRAPH, paragraph.toString().trim()));
            paragraph.setLength(0);
        }
    }
}
