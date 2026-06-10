package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.dto.IdeaSaveRequest;
import com.exam.ideaforge.entity.ItemStatus;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.exception.IdeaNotFoundException;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 知识条目持久化服务。
 * 用户在前端确认后，将原始字段、AI 建议与最终字段一并保存。
 */
@Service
public class IdeaService {

    private final KnowledgeItemRepository repository;

    public IdeaService(KnowledgeItemRepository repository) {
        this.repository = repository;
    }

    /** 保存用户确认后的条目，状态设为 CONFIRMED */
    @Transactional
    public IdeaResponse save(IdeaSaveRequest request) {
        KnowledgeItem item = new KnowledgeItem();
        item.setOriginalTitle(request.getOriginalTitle());
        item.setOriginalContent(request.getOriginalContent().trim());
        item.setSuggestedTitle(request.getSuggestedTitle());
        item.setSuggestedSummary(request.getSuggestedSummary());
        item.setSuggestedTags(IdeaMapper.toTagArray(request.getSuggestedTags()));
        item.setSuggestedCategory(request.getSuggestedCategory());
        item.setFinalTitle(request.getFinalTitle().trim());
        item.setFinalSummary(request.getFinalSummary());
        item.setFinalTags(IdeaMapper.toTagArray(request.getFinalTags()));
        item.setFinalCategory(request.getFinalCategory());
        item.setStatus(ItemStatus.CONFIRMED);

        KnowledgeItem saved = repository.save(item);
        return IdeaMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IdeaResponse findById(UUID id) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new IdeaNotFoundException("知识条目不存在：" + id));
        return IdeaMapper.toResponse(item);
    }
}
