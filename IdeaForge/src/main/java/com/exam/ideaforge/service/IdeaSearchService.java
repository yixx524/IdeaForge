package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * V1 搜索服务，仅检索 status=confirmed 的条目。
 * 关键词与类别筛选分离：关键词匹配标题/摘要/正文/标签，类别由独立参数筛选。
 */
@Service
public class IdeaSearchService {

    private final KnowledgeItemRepository repository;

    public IdeaSearchService(KnowledgeItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<IdeaResponse> search(String keyword, String category) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = category != null && !category.isBlank();

        if (!hasKeyword && !hasCategory) {
            return List.of();
        }

        List<KnowledgeItem> items;
        if (hasKeyword && hasCategory) {
            String trimmedKeyword = keyword.trim();
            String normalizedCategory = category.trim().toUpperCase();
            items = repository.searchByKeyword(trimmedKeyword).stream()
                    .filter(item -> normalizedCategory.equals(item.getFinalCategory()))
                    .toList();
        } else if (hasCategory) {
            items = repository.findConfirmedByCategory(category.trim().toUpperCase());
        } else {
            items = repository.searchByKeyword(keyword.trim());
        }

        return items.stream()
                .map(IdeaMapper::toResponse)
                .toList();
    }
}
