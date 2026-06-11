package com.exam.ideaforge.controller;

import com.exam.ideaforge.dto.DocumentParseResponse;
import com.exam.ideaforge.dto.IdeaProcessRequest;
import com.exam.ideaforge.dto.IdeaProcessResponse;
import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.dto.IdeaSaveRequest;
import com.exam.ideaforge.dto.IdeaUpdateRequest;
import com.exam.ideaforge.service.DocumentParseService;
import com.exam.ideaforge.service.IdeaExportService;
import com.exam.ideaforge.service.IdeaProcessService;
import com.exam.ideaforge.service.IdeaSearchService;
import com.exam.ideaforge.service.IdeaService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 想法相关 REST 接口。
 * 流程：process（AI 整理）→ 前端编辑 → save（确认入库）→ search（检索）
 */
@RestController
@RequestMapping("/api/ideas")
public class IdeaController {

    private final IdeaProcessService processService;
    private final IdeaService ideaService;
    private final IdeaSearchService searchService;
    private final DocumentParseService documentParseService;
    private final IdeaExportService exportService;

    public IdeaController(
            IdeaProcessService processService,
            IdeaService ideaService,
            IdeaSearchService searchService,
            DocumentParseService documentParseService,
            IdeaExportService exportService) {
        this.processService = processService;
        this.ideaService = ideaService;
        this.searchService = searchService;
        this.documentParseService = documentParseService;
        this.exportService = exportService;
    }

    /** 阶段一：AI 整理，不落库 */
    @PostMapping("/process")
    public IdeaProcessResponse process(@Valid @RequestBody IdeaProcessRequest request) {
        return processService.process(request);
    }

    /** 阶段二：用户确认后持久化 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IdeaResponse save(@Valid @RequestBody IdeaSaveRequest request) {
        return ideaService.save(request);
    }

    /**
     * 搜索已确认条目。
     * q：关键词（标题/摘要/正文/标签）；category：类别筛选（WORK 等），可与 q 组合。
     */
    @GetMapping("/search")
    public List<IdeaResponse> search(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category) {
        return searchService.search(keyword, category);
    }

    /** 上传 Word/PDF，提取文本供录入页填充（不落库、不调用 AI） */
    @PostMapping("/parse-document")
    public DocumentParseResponse parseDocument(@RequestPart("file") MultipartFile file) {
        return documentParseService.parse(file);
    }

    @GetMapping("/{id}/export/docx")
    public ResponseEntity<byte[]> exportDocx(@PathVariable UUID id) {
        IdeaExportService.ExportResult result = exportService.exportDocx(id);
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(result.filename(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .body(result.content());
    }

    /** 软删除：status=deleted，浏览页不再显示 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        ideaService.softDelete(id);
    }

    /** 更新已确认条目的最终字段 */
    @PutMapping("/{id}")
    public IdeaResponse update(@PathVariable UUID id, @Valid @RequestBody IdeaUpdateRequest request) {
        return ideaService.update(id, request);
    }

    @GetMapping("/{id}")
    public IdeaResponse findById(@PathVariable UUID id) {
        return ideaService.findById(id);
    }
}
