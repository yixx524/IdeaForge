package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** V1 关键词搜索服务，仅检索 status=confirmed 的条目 */
@Service
public class IdeaSearchService {

    private final KnowledgeItemRepository repository;

    public IdeaSearchService(KnowledgeItemRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<IdeaResponse> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        List<KnowledgeItem> items = repository.searchByKeyword(keyword.trim());
        return items.stream()
                .map(IdeaMapper::toResponse)
                .toList();
    }
}
