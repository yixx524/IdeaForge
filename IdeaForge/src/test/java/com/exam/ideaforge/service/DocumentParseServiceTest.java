package com.exam.ideaforge.service;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DocumentParseServiceTest {

    private final DocumentParseService service = new DocumentParseService();

    @Test
    void parse_docx_extractsParagraphText() throws Exception {
        byte[] docxBytes;
        try (XWPFDocument document = new XWPFDocument()) {
            XWPFParagraph paragraph = document.createParagraph();
            XWPFRun run = paragraph.createRun();
            run.setText("这是 Word 文档正文");
            docxBytes = writeDocx(document);
        }

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "我的笔记.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                docxBytes
        );

        var result = service.parse(file);

        assertThat(result.getFileName()).isEqualTo("我的笔记.docx");
        assertThat(result.getExtractedTitle()).isEqualTo("我的笔记");
        assertThat(result.getExtractedContent()).isEqualTo("这是 Word 文档正文");
        assertThat(result.getCharCount()).isEqualTo("这是 Word 文档正文".length());
    }

    @Test
    void parse_rejectsUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThatThrownBy(() -> service.parse(file))
                .isInstanceOf(com.exam.ideaforge.exception.DocumentParseException.class)
                .hasMessageContaining(".docx");
    }

    @Test
    void parse_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[0]
        );

        assertThatThrownBy(() -> service.parse(file))
                .isInstanceOf(com.exam.ideaforge.exception.DocumentParseException.class)
                .hasMessageContaining("请选择");
    }

    private static byte[] writeDocx(XWPFDocument document) throws Exception {
        try (var out = new java.io.ByteArrayOutputStream()) {
            document.write(out);
            return out.toByteArray();
        }
    }
}
