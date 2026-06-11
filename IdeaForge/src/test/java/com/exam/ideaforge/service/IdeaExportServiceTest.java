package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.repository.IdeaCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdeaExportServiceTest {

    @Mock
    private IdeaService ideaService;

    @Mock
    private IdeaCategoryRepository categoryRepository;

    @InjectMocks
    private IdeaExportService exportService;

    @Test
    void exportDocx_withHtmlContent_returnsDocxBytes() {
        UUID id = UUID.randomUUID();
        IdeaResponse idea = IdeaResponse.builder()
                .id(id)
                .finalTitle("HTML导出")
                .finalSummary("摘要")
                .finalTags(List.of("标签"))
                .finalCategory("WORK")
                .finalContent("<h3>小节</h3><ul><li>列表项</li></ul><p>段落文字</p>")
                .originalContent("正文")
                .createdAt(OffsetDateTime.parse("2026-06-11T10:00:00+08:00"))
                .build();

        when(ideaService.findById(id)).thenReturn(idea);
        when(categoryRepository.findByCode("WORK")).thenReturn(Optional.empty());

        IdeaExportService.ExportResult result = exportService.exportDocx(id);

        assertThat(result.content()).isNotEmpty();
        assertThat(result.content()[0]).isEqualTo((byte) 'P');
    }

    @Test
    void exportDocx_returnsDocxBytesAndFilename() {
        UUID id = UUID.randomUUID();
        IdeaResponse idea = IdeaResponse.builder()
                .id(id)
                .finalTitle("导出测试")
                .finalSummary("一句话摘要")
                .finalTags(List.of("标签A", "标签B"))
                .finalCategory("WORK")
                .finalContent("## 小节\n\n- 列表项\n\n段落文字")
                .originalContent("正文内容")
                .createdAt(OffsetDateTime.parse("2026-06-11T10:00:00+08:00"))
                .build();

        when(ideaService.findById(id)).thenReturn(idea);
        when(categoryRepository.findByCode("WORK")).thenReturn(Optional.empty());

        IdeaExportService.ExportResult result = exportService.exportDocx(id);

        assertThat(result.filename()).startsWith("导出测试_").endsWith(".docx");
        assertThat(result.content()).isNotEmpty();
        assertThat(result.content()[0]).isEqualTo((byte) 'P');
        assertThat(result.content()[1]).isEqualTo((byte) 'K');
    }
}
