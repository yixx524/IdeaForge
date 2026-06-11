package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.repository.IdeaCategoryRepository;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTInd;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 知识条目导出服务：生成 Word (.docx) 文件供下载。
 */
@Service
public class IdeaExportService {

    private static final Pattern INVALID_FILENAME = Pattern.compile("[\\\\/:*?\"<>|]");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final IdeaService ideaService;
    private final IdeaCategoryRepository categoryRepository;

    public IdeaExportService(IdeaService ideaService, IdeaCategoryRepository categoryRepository) {
        this.ideaService = ideaService;
        this.categoryRepository = categoryRepository;
    }

    public ExportResult exportDocx(UUID id) {
        IdeaResponse idea = ideaService.findById(id);
        String categoryLabel = resolveCategoryLabel(idea.getFinalCategory());
        byte[] content = buildDocx(idea, categoryLabel);
        String filename = buildFilename(idea.getFinalTitle(), id);
        return new ExportResult(filename, content);
    }

    private byte[] buildDocx(IdeaResponse idea, String categoryLabel) {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            addTitle(document, idea.getFinalTitle());

            String createdAt = idea.getCreatedAt() != null
                    ? idea.getCreatedAt().format(DATE_FORMAT)
                    : "";
            addMetaLine(document, "类别：" + categoryLabel + "    创建时间：" + createdAt);

            if (idea.getFinalTags() != null && !idea.getFinalTags().isEmpty()) {
                addMetaLine(document, "标签：" + String.join("、", idea.getFinalTags()));
            }

            addSectionHeading(document, "摘要");
            addFormattedText(document, blankToDash(idea.getFinalSummary()));

            addSectionHeading(document, "正文");
            String body = idea.getFinalContent() != null && !idea.getFinalContent().isBlank()
                    ? idea.getFinalContent()
                    : idea.getOriginalContent();
            addFormattedText(document, body);

            document.write(out);
            return out.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("Word 导出失败", ex);
        }
    }

    private void addFormattedText(XWPFDocument document, String text) {
        List<TextLayoutFormatter.TextBlock> blocks = resolveBlocks(text);
        if (blocks.isEmpty()) {
            addBodyParagraph(document, "（无）", false, 0);
            return;
        }

        for (TextLayoutFormatter.TextBlock block : blocks) {
            switch (block.type()) {
                case HEADING2 -> addHeading2(document, block.text());
                case LIST_ITEM -> addBodyParagraph(document, block.text(), false, 720);
                case PARAGRAPH -> addBodyParagraph(document, block.text(), false, 0);
            }
        }
    }

    private List<TextLayoutFormatter.TextBlock> resolveBlocks(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        if (HtmlLayoutFormatter.isHtmlContent(text)) {
            List<TextLayoutFormatter.TextBlock> htmlBlocks = HtmlLayoutFormatter.parse(text);
            if (!htmlBlocks.isEmpty()) {
                return htmlBlocks;
            }
        }
        return TextLayoutFormatter.parse(text);
    }

    private void addTitle(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(18);
        run.setText(text != null ? text : "未命名");
    }

    private void addMetaLine(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        XWPFRun run = paragraph.createRun();
        run.setFontSize(10);
        run.setColor("64748B");
        run.setText(text);
    }

    private void addSectionHeading(XWPFDocument document, String heading) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(200);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(12);
        run.setText(heading);
    }

    private void addHeading2(XWPFDocument document, String text) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingBefore(160);
        paragraph.setSpacingAfter(80);
        XWPFRun run = paragraph.createRun();
        run.setBold(true);
        run.setFontSize(14);
        run.setText(text);
    }

    private void addBodyParagraph(XWPFDocument document, String text, boolean bold, int indentTwips) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setSpacingAfter(120);
        paragraph.setSpacingBetween(1.5);

        if (indentTwips > 0) {
            CTPPr pPr = paragraph.getCTP().isSetPPr()
                    ? paragraph.getCTP().getPPr()
                    : paragraph.getCTP().addNewPPr();
            CTInd ind = pPr.isSetInd() ? pPr.getInd() : pPr.addNewInd();
            ind.setLeft(BigInteger.valueOf(indentTwips));
        }

        XWPFRun run = paragraph.createRun();
        run.setBold(bold);
        run.setFontSize(11);
        run.setText(text != null ? text : "");
    }

    private String resolveCategoryLabel(String code) {
        if (code == null || code.isBlank()) {
            return "-";
        }
        return categoryRepository.findByCode(code.trim())
                .map(entity -> entity.getLabel())
                .orElse(code);
    }

    private String buildFilename(String title, UUID id) {
        String base = title != null ? title.trim() : "知识条目";
        base = INVALID_FILENAME.matcher(base).replaceAll("_");
        if (base.isBlank()) {
            base = "知识条目";
        }
        if (base.length() > 60) {
            base = base.substring(0, 60);
        }
        return base + "_" + id.toString().substring(0, 8) + ".docx";
    }

    private String blankToDash(String value) {
        return value == null || value.isBlank() ? "（无）" : value;
    }

    public record ExportResult(String filename, byte[] content) {}
}
