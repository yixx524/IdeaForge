package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.dto.IdeaSearchPageResponse;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * V1 搜索服务，仅检索 status=confirmed 的条目。
 * 关键词与类别筛选分离：关键词匹配标题/摘要/正文/标签，类别由独立参数筛选。
 */
@Service
public class IdeaSearchService {

    static final int DEFAULT_PAGE_SIZE = 20;
    static final int MAX_PAGE_SIZE = 100;

    private final KnowledgeItemRepository repository;

    public IdeaSearchService(KnowledgeItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public IdeaSearchPageResponse search(String keyword, String category, int page, int size) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();
        int safeSize = clampSize(size);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize);

        Page<KnowledgeItem> resultPage;
        if (!hasKeyword && !hasCategory) {
            resultPage = repository.findAllConfirmedOrderByCreatedAtDesc(pageable);
        } else if (hasKeyword && hasCategory) {
            resultPage = repository.searchByKeywordAndCategory(
                    keyword.trim(),
                    category.trim().toUpperCase(),
                    pageable);
        } else if (hasCategory) {
            resultPage = repository.findConfirmedByCategory(category.trim().toUpperCase(), pageable);
        } else {
            resultPage = repository.searchByKeyword(keyword.trim(), pageable);
        }

        Page<IdeaResponse> mapped = resultPage.map(IdeaMapper::toResponse);
        return IdeaSearchPageResponse.from(mapped);
    }

    private static int clampSize(int size) {
        if (size < 1) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }
}
