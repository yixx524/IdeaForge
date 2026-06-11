package com.exam.ideaforge.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/** HTML 正文解析，供 Word 导出使用；与 TextLayoutFormatter 共用 TextBlock 结构 */
public final class HtmlLayoutFormatter {

    private static final Pattern HTML_TAG_PATTERN =
            Pattern.compile("<[a-z][\\s\\S]*?>", Pattern.CASE_INSENSITIVE);

    private HtmlLayoutFormatter() {
    }

    public static boolean isHtmlContent(String raw) {
        return raw != null && !raw.isBlank() && HTML_TAG_PATTERN.matcher(raw.trim()).find();
    }

    public static List<TextLayoutFormatter.TextBlock> parse(String raw) {
        if (!isHtmlContent(raw)) {
            return List.of();
        }

        Element body = Jsoup.parseBodyFragment(raw).body();
        List<TextLayoutFormatter.TextBlock> blocks = new ArrayList<>();
        walkChildren(body, blocks);
        return blocks;
    }

    private static void walkChildren(Element parent, List<TextLayoutFormatter.TextBlock> blocks) {
        for (Element child : parent.children()) {
            String tag = child.tagName().toLowerCase();
            switch (tag) {
                case "h1", "h2", "h3", "h4", "h5", "h6" -> addHeading(child.text(), blocks);
                case "p" -> addParagraph(child.text(), blocks);
                case "ul", "ol" -> {
                    for (Element li : child.children()) {
                        if ("li".equalsIgnoreCase(li.tagName())) {
                            blocks.add(new TextLayoutFormatter.TextBlock(
                                    TextLayoutFormatter.BlockType.LIST_ITEM,
                                    li.text().trim()
                            ));
                        }
                    }
                }
                case "blockquote" -> addParagraph(child.text(), blocks);
                case "div" -> walkChildren(child, blocks);
                default -> {
                    if (child.children().isEmpty()) {
                        addParagraph(child.text(), blocks);
                    } else {
                        walkChildren(child, blocks);
                    }
                }
            }
        }
    }

    private static void addHeading(String text, List<TextLayoutFormatter.TextBlock> blocks) {
        if (text != null && !text.isBlank()) {
            blocks.add(new TextLayoutFormatter.TextBlock(
                    TextLayoutFormatter.BlockType.HEADING2,
                    text.trim()
            ));
        }
    }

    private static void addParagraph(String text, List<TextLayoutFormatter.TextBlock> blocks) {
        if (text != null && !text.isBlank()) {
            blocks.add(new TextLayoutFormatter.TextBlock(
                    TextLayoutFormatter.BlockType.PARAGRAPH,
                    text.trim()
            ));
        }
    }
}
