package com.exam.ideaforge.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TextLayoutFormatterTest {

    @Test
    void parse_headingListAndParagraph() {
        String text = """
                ## 背景
                这是第一段说明。

                - 要点一
                - 要点二

                这是结尾段落。
                """;

        List<TextLayoutFormatter.TextBlock> blocks = TextLayoutFormatter.parse(text);

        assertThat(blocks).extracting(TextLayoutFormatter.TextBlock::type)
                .containsExactly(
                        TextLayoutFormatter.BlockType.HEADING2,
                        TextLayoutFormatter.BlockType.PARAGRAPH,
                        TextLayoutFormatter.BlockType.LIST_ITEM,
                        TextLayoutFormatter.BlockType.LIST_ITEM,
                        TextLayoutFormatter.BlockType.PARAGRAPH
                );
        assertThat(blocks.get(0).text()).isEqualTo("背景");
        assertThat(blocks.get(2).text()).isEqualTo("要点一");
    }

    @Test
    void parse_heading3() {
        String text = """
                ### 区块链的特点是什么？
                - 去中心化
                - 不可篡改

                ## 补充说明
                段落文字。
                """;

        List<TextLayoutFormatter.TextBlock> blocks = TextLayoutFormatter.parse(text);

        assertThat(blocks).extracting(TextLayoutFormatter.TextBlock::type)
                .containsExactly(
                        TextLayoutFormatter.BlockType.HEADING2,
                        TextLayoutFormatter.BlockType.LIST_ITEM,
                        TextLayoutFormatter.BlockType.LIST_ITEM,
                        TextLayoutFormatter.BlockType.HEADING2,
                        TextLayoutFormatter.BlockType.PARAGRAPH
                );
        assertThat(blocks.get(0).text()).isEqualTo("区块链的特点是什么？");
        assertThat(blocks.get(3).text()).isEqualTo("补充说明");
    }

    @Test
    void parse_blankReturnsEmpty() {
        assertThat(TextLayoutFormatter.parse(null)).isEmpty();
        assertThat(TextLayoutFormatter.parse("   ")).isEmpty();
    }
}
