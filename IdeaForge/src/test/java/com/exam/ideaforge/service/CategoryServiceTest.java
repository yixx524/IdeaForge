package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.CategoryRequest;
import com.exam.ideaforge.dto.CategoryUpdateRequest;
import com.exam.ideaforge.entity.IdeaCategoryEntity;
import com.exam.ideaforge.exception.CategoryNotFoundException;
import com.exam.ideaforge.exception.CategoryValidationException;
import com.exam.ideaforge.repository.IdeaCategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private IdeaCategoryRepository repository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_persistsNewCategory() {
        CategoryRequest request = new CategoryRequest();
        request.setCode("side_hustle");
        request.setLabel("副业");
        request.setSortOrder(10);

        when(repository.findByCode("SIDE_HUSTLE")).thenReturn(Optional.empty());
        when(repository.save(any(IdeaCategoryEntity.class))).thenAnswer(invocation -> {
            IdeaCategoryEntity entity = invocation.getArgument(0);
            entity.setId(UUID.randomUUID());
            return entity;
        });

        var response = categoryService.create(request);

        assertThat(response.getCode()).isEqualTo("SIDE_HUSTLE");
        assertThat(response.getLabel()).isEqualTo("副业");
        assertThat(response.isEnabled()).isTrue();

        ArgumentCaptor<IdeaCategoryEntity> captor = ArgumentCaptor.forClass(IdeaCategoryEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getCode()).isEqualTo("SIDE_HUSTLE");
    }

    @Test
    void create_rejectsDuplicateCode() {
        CategoryRequest request = new CategoryRequest();
        request.setCode("WORK");
        request.setLabel("工作");

        IdeaCategoryEntity existing = new IdeaCategoryEntity();
        existing.setCode("WORK");
        existing.setEnabled(true);
        when(repository.findByCode("WORK")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(CategoryValidationException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    void softDelete_setsEnabledFalse() {
        UUID id = UUID.randomUUID();
        IdeaCategoryEntity entity = new IdeaCategoryEntity();
        entity.setId(id);
        entity.setCode("TODO");
        entity.setLabel("待办");
        entity.setEnabled(true);

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.save(entity)).thenReturn(entity);

        categoryService.softDelete(id);

        assertThat(entity.isEnabled()).isFalse();
        verify(repository).save(entity);
    }

    @Test
    void validateEnabledCategory_rejectsDeletedCode() {
        when(repository.findByCodeAndEnabledTrue("TODO")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.validateEnabledCategory("TODO"))
                .isInstanceOf(CategoryValidationException.class)
                .hasMessageContaining("已删除");
    }

    @Test
    void resolveCategoryCode_fallsBackWhenUnknown() {
        IdeaCategoryEntity inspiration = new IdeaCategoryEntity();
        inspiration.setCode("INSPIRATION");
        inspiration.setLabel("灵感");
        inspiration.setEnabled(true);

        when(repository.findByEnabledTrueOrderBySortOrderAscCodeAsc())
                .thenReturn(List.of(inspiration));

        assertThat(categoryService.resolveCategoryCode("UNKNOWN")).isEqualTo("INSPIRATION");
    }

    @Test
    void update_notFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(id, new CategoryUpdateRequest()))
                .isInstanceOf(CategoryNotFoundException.class);
    }
}
