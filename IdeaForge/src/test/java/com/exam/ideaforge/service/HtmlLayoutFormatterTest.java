package com.exam.ideaforge.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HtmlLayoutFormatterTest {

    @Test
    void isHtmlContent_detectsTags() {
        assertThat(HtmlLayoutFormatter.isHtmlContent("<p>段落</p>")).isTrue();
        assertThat(HtmlLayoutFormatter.isHtmlContent("## 标题")).isFalse();
        assertThat(HtmlLayoutFormatter.isHtmlContent(null)).isFalse();
    }

    @Test
    void parse_headingListAndParagraph() {
        String html = """
                <h3>区块链的特点是什么？</h3>
                <ul><li>去中心化</li><li>不可篡改</li></ul>
                <p>补充说明段落。</p>
                """;

        List<TextLayoutFormatter.TextBlock> blocks = HtmlLayoutFormatter.parse(html);

        assertThat(blocks).extracting(TextLayoutFormatter.TextBlock::type)
                .containsExactly(
                        TextLayoutFormatter.BlockType.HEADING2,
                        TextLayoutFormatter.BlockType.LIST_ITEM,
                        TextLayoutFormatter.BlockType.LIST_ITEM,
                        TextLayoutFormatter.BlockType.PARAGRAPH
                );
        assertThat(blocks.get(0).text()).isEqualTo("区块链的特点是什么？");
        assertThat(blocks.get(1).text()).isEqualTo("去中心化");
    }

    @Test
    void parse_nonHtmlReturnsEmpty() {
        assertThat(HtmlLayoutFormatter.parse("纯文本")).isEmpty();
        assertThat(HtmlLayoutFormatter.parse("## Markdown")).isEmpty();
    }
}
