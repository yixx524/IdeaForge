package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.CategoryRequest;
import com.exam.ideaforge.dto.CategoryResponse;
import com.exam.ideaforge.dto.CategoryUpdateRequest;
import com.exam.ideaforge.entity.IdeaCategoryEntity;
import com.exam.ideaforge.exception.CategoryNotFoundException;
import com.exam.ideaforge.exception.CategoryValidationException;
import com.exam.ideaforge.repository.IdeaCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** 想法类别字典：列表、CRUD、软删除及保存校验 */
@Service
public class CategoryService {

    private final IdeaCategoryRepository repository;

    public CategoryService(IdeaCategoryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(boolean includeAll) {
        List<IdeaCategoryEntity> items = includeAll
                ? repository.findAllByOrderBySortOrderAscCodeAsc()
                : repository.findByEnabledTrueOrderBySortOrderAscCodeAsc();
        return items.stream().map(CategoryService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<String> getEnabledCodes() {
        return repository.findByEnabledTrueOrderBySortOrderAscCodeAsc().stream()
                .map(IdeaCategoryEntity::getCode)
                .toList();
    }

    /** 校验 finalCategory 必须是 enabled 的 code */
    @Transactional(readOnly = true)
    public void validateEnabledCategory(String code) {
        if (code == null || code.isBlank()) {
            throw new CategoryValidationException("最终类别不能为空");
        }
        repository.findByCodeAndEnabledTrue(code.trim())
                .orElseThrow(() -> new CategoryValidationException("类别不存在或已删除：" + code.trim()));
    }

    /** 将 AI 返回的 code 解析为有效的 enabled code，无效时 fallback */
    @Transactional(readOnly = true)
    public String resolveCategoryCode(String rawCode) {
        List<IdeaCategoryEntity> enabled = repository.findByEnabledTrueOrderBySortOrderAscCodeAsc();
        if (enabled.isEmpty()) {
            throw new CategoryValidationException("没有可用的类别，请先在类别管理中新增");
        }

        if (rawCode != null && !rawCode.isBlank()) {
            String normalized = rawCode.trim().toUpperCase();
            for (IdeaCategoryEntity entity : enabled) {
                if (entity.getCode().equals(normalized)) {
                    return normalized;
                }
            }
        }

        return enabled.stream()
                .filter(e -> "INSPIRATION".equals(e.getCode()))
                .findFirst()
                .map(IdeaCategoryEntity::getCode)
                .orElseGet(() -> enabled.getFirst().getCode());
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        String code = request.getCode().trim().toUpperCase();
        var existing = repository.findByCode(code);
        if (existing.isPresent()) {
            IdeaCategoryEntity entity = existing.get();
            if (entity.isEnabled()) {
                throw new CategoryValidationException("类别 code 已存在：" + code);
            }
            entity.setLabel(request.getLabel().trim());
            entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
            entity.setEnabled(true);
            return toResponse(repository.save(entity));
        }

        IdeaCategoryEntity entity = new IdeaCategoryEntity();
        entity.setCode(code);
        entity.setLabel(request.getLabel().trim());
        entity.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0);
        entity.setEnabled(true);

        return toResponse(repository.save(entity));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryUpdateRequest request) {
        IdeaCategoryEntity entity = findEntity(id);

        if (request.getLabel() != null && !request.getLabel().isBlank()) {
            entity.setLabel(request.getLabel().trim());
        }
        if (request.getSortOrder() != null) {
            entity.setSortOrder(request.getSortOrder());
        }
        if (request.getEnabled() != null) {
            entity.setEnabled(request.getEnabled());
        }

        return toResponse(repository.save(entity));
    }

    /** 软删除：enabled=false，记录保留供历史条目引用，管理页不再展示 */
    @Transactional
    public void softDelete(UUID id) {
        IdeaCategoryEntity entity = findEntity(id);
        if (!entity.isEnabled()) {
            throw new CategoryValidationException("类别已删除：" + entity.getCode());
        }
        entity.setEnabled(false);
        repository.save(entity);
    }

    private IdeaCategoryEntity findEntity(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("类别不存在：" + id));
    }

    private static CategoryResponse toResponse(IdeaCategoryEntity entity) {
        return CategoryResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .label(entity.getLabel())
                .sortOrder(entity.getSortOrder())
                .enabled(entity.isEnabled())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
