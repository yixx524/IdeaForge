package com.exam.ideaforge.controller;

import com.exam.ideaforge.dto.IdeaProcessRequest;
import com.exam.ideaforge.dto.IdeaProcessResponse;
import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.dto.IdeaSaveRequest;
import com.exam.ideaforge.service.IdeaProcessService;
import com.exam.ideaforge.service.IdeaSearchService;
import com.exam.ideaforge.service.IdeaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    public IdeaController(
            IdeaProcessService processService,
            IdeaService ideaService,
            IdeaSearchService searchService) {
        this.processService = processService;
        this.ideaService = ideaService;
        this.searchService = searchService;
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

    /** 关键词搜索已确认条目 */
    @GetMapping("/search")
    public List<IdeaResponse> search(@RequestParam("q") String keyword) {
        return searchService.search(keyword);
    }

    @GetMapping("/{id}")
    public IdeaResponse findById(@PathVariable UUID id) {
        return ideaService.findById(id);
    }
}
