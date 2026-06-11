package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaResponse;
import com.exam.ideaforge.dto.IdeaSaveRequest;
import com.exam.ideaforge.dto.IdeaUpdateRequest;
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
    private final CategoryService categoryService;

    public IdeaService(KnowledgeItemRepository repository, CategoryService categoryService) {
        this.repository = repository;
        this.categoryService = categoryService;
    }

    /** 保存用户确认后的条目，状态设为 CONFIRMED */
    @Transactional
    public IdeaResponse save(IdeaSaveRequest request) {
        categoryService.validateEnabledCategory(request.getFinalCategory());

        KnowledgeItem item = new KnowledgeItem();
        item.setOriginalTitle(request.getOriginalTitle());
        item.setOriginalContent(request.getOriginalContent().trim());
        item.setSuggestedTitle(request.getSuggestedTitle());
        item.setSuggestedSummary(request.getSuggestedSummary());
        item.setSuggestedTags(IdeaMapper.toTagArray(request.getSuggestedTags()));
        item.setSuggestedCategory(request.getSuggestedCategory());
        item.setSuggestedContent(request.getSuggestedContent());
        item.setFinalTitle(request.getFinalTitle().trim());
        item.setFinalSummary(request.getFinalSummary());
        item.setFinalTags(IdeaMapper.toTagArray(request.getFinalTags()));
        item.setFinalCategory(request.getFinalCategory().trim().toUpperCase());
        item.setFinalContent(resolveFinalContentForSave(request));
        item.setStatus(ItemStatus.CONFIRMED);

        KnowledgeItem saved = repository.save(item);
        return IdeaMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public IdeaResponse findById(UUID id) {
        return IdeaMapper.toResponse(findActiveOrThrow(id));
    }

    /** 更新已确认条目的最终字段；若请求携带 suggested 字段则同步更新 AI 建议 */
    @Transactional
    public IdeaResponse update(UUID id, IdeaUpdateRequest request) {
        categoryService.validateEnabledCategory(request.getFinalCategory());

        KnowledgeItem item = findActiveOrThrow(id);

        item.setFinalTitle(request.getFinalTitle().trim());
        item.setFinalSummary(request.getFinalSummary());
        item.setFinalTags(IdeaMapper.toTagArray(request.getFinalTags()));
        item.setFinalCategory(request.getFinalCategory().trim().toUpperCase());
        item.setFinalContent(resolveFinalContentForUpdate(request));
        applySuggestedFieldsIfPresent(item, request);

        KnowledgeItem saved = repository.save(item);
        return IdeaMapper.toResponse(saved);
    }

    /** 软删除：status=deleted，记录保留，浏览/搜索不可见 */
    @Transactional
    public void softDelete(UUID id) {
        KnowledgeItem item = findActiveOrThrow(id);
        item.setStatus(ItemStatus.DELETED);
        repository.save(item);
    }

    private KnowledgeItem findActiveOrThrow(UUID id) {
        KnowledgeItem item = repository.findById(id)
                .orElseThrow(() -> new IdeaNotFoundException("知识条目不存在：" + id));
        if (item.getStatus() == ItemStatus.DELETED) {
            throw new IdeaNotFoundException("知识条目不存在：" + id);
        }
        return item;
    }

    private static void applySuggestedFieldsIfPresent(KnowledgeItem item, IdeaUpdateRequest request) {
        if (request.getSuggestedTitle() != null && !request.getSuggestedTitle().isBlank()) {
            item.setSuggestedTitle(request.getSuggestedTitle().trim());
        }
        if (request.getSuggestedSummary() != null) {
            item.setSuggestedSummary(request.getSuggestedSummary().trim());
        }
        if (request.getSuggestedTags() != null) {
            item.setSuggestedTags(IdeaMapper.toTagArray(request.getSuggestedTags()));
        }
        if (request.getSuggestedCategory() != null && !request.getSuggestedCategory().isBlank()) {
            item.setSuggestedCategory(request.getSuggestedCategory().trim().toUpperCase());
        }
        if (request.getSuggestedContent() != null) {
            item.setSuggestedContent(request.getSuggestedContent().trim());
        }
    }

    private static String resolveFinalContentForSave(IdeaSaveRequest request) {
        if (request.getFinalContent() != null && !request.getFinalContent().isBlank()) {
            return request.getFinalContent().trim();
        }
        return request.getOriginalContent().trim();
    }

    private static String resolveFinalContentForUpdate(IdeaUpdateRequest request) {
        if (request.getFinalContent() != null && !request.getFinalContent().isBlank()) {
            return request.getFinalContent().trim();
        }
        return null;
    }
}
