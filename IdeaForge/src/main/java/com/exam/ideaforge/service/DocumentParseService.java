package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.DocumentParseResponse;
import com.exam.ideaforge.exception.DocumentParseException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;

/**
 * 文档解析服务：从 .docx / .pdf 提取纯文本，供录入页填充后走 AI 整理流程。
 */
@Service
public class DocumentParseService {

    static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("docx", "pdf");

    public DocumentParseResponse parse(MultipartFile file) {
        validateFile(file);

        String extension = resolveExtension(file.getOriginalFilename());
        String content = switch (extension) {
            case "docx" -> parseDocx(file);
            case "pdf" -> parsePdf(file);
            default -> throw new DocumentParseException("仅支持 .docx 和 .pdf 文件");
        };

        String normalized = normalizeContent(content);
        if (normalized.isBlank()) {
            throw new DocumentParseException("未能从文档中提取文本，请确认文件含可识别文字（扫描版 PDF 不支持）");
        }

        return DocumentParseResponse.builder()
                .fileName(file.getOriginalFilename())
                .extractedTitle(extractTitleFromFileName(file.getOriginalFilename()))
                .extractedContent(normalized)
                .charCount(normalized.length())
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new DocumentParseException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new DocumentParseException("文件大小不能超过 10MB");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || filename.isBlank()) {
            throw new DocumentParseException("文件名无效");
        }
        String extension = resolveExtension(filename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new DocumentParseException("仅支持 .docx 和 .pdf 文件");
        }
    }

    private String resolveExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            throw new DocumentParseException("仅支持 .docx 和 .pdf 文件");
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String parseDocx(MultipartFile file) {
        try (InputStream input = file.getInputStream();
             XWPFDocument document = new XWPFDocument(input)) {
            StringBuilder builder = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    if (!builder.isEmpty()) {
                        builder.append('\n');
                    }
                    builder.append(text.trim());
                }
            }
            return builder.toString();
        } catch (IOException ex) {
            throw new DocumentParseException("Word 文档解析失败，请确认文件未损坏", ex);
        }
    }

    private String parsePdf(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            try (PDDocument document = Loader.loadPDF(bytes)) {
                PDFTextStripper stripper = new PDFTextStripper();
                return stripper.getText(document);
            }
        } catch (IOException ex) {
            throw new DocumentParseException("PDF 文档解析失败，请确认文件未损坏", ex);
        }
    }

    private String normalizeContent(String content) {
        if (content == null) {
            return "";
        }
        return content
                .replace("\r\n", "\n")
                .replace('\r', '\n')
                .trim();
    }

    private String extractTitleFromFileName(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }
        String name = filename;
        int slash = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        int dot = name.lastIndexOf('.');
        if (dot > 0) {
            name = name.substring(0, dot);
        }
        name = name.trim();
        return name.isBlank() ? null : name;
    }
}
