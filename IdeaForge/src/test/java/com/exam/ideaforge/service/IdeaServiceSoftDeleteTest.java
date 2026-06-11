package com.exam.ideaforge.service;

import com.exam.ideaforge.dto.IdeaUpdateRequest;
import com.exam.ideaforge.entity.ItemStatus;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.exception.IdeaNotFoundException;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
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
class IdeaServiceSoftDeleteTest {

    @Mock
    private KnowledgeItemRepository repository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private IdeaService ideaService;

    @Test
    void softDelete_setsStatusDeleted() {
        UUID id = UUID.randomUUID();
        KnowledgeItem item = confirmedItem(id);

        when(repository.findById(id)).thenReturn(Optional.of(item));
        when(repository.save(any(KnowledgeItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ideaService.softDelete(id);

        ArgumentCaptor<KnowledgeItem> captor = ArgumentCaptor.forClass(KnowledgeItem.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(ItemStatus.DELETED);
    }

    @Test
    void findById_throwsWhenDeleted() {
        UUID id = UUID.randomUUID();
        KnowledgeItem item = confirmedItem(id);
        item.setStatus(ItemStatus.DELETED);

        when(repository.findById(id)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> ideaService.findById(id))
                .isInstanceOf(IdeaNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void update_appliesSuggestedFieldsWhenPresent() {
        UUID id = UUID.randomUUID();
        KnowledgeItem item = confirmedItem(id);

        when(repository.findById(id)).thenReturn(Optional.of(item));
        when(repository.save(any(KnowledgeItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IdeaUpdateRequest request = new IdeaUpdateRequest();
        request.setFinalTitle("新标题");
        request.setFinalCategory("STUDY");
        request.setSuggestedTitle("AI 标题");
        request.setSuggestedSummary("AI 摘要");
        request.setSuggestedTags(List.of("标签1", "标签2"));
        request.setSuggestedCategory("STUDY");
        request.setSuggestedContent("AI 正文");

        var response = ideaService.update(id, request);

        assertThat(response.getFinalTitle()).isEqualTo("新标题");
        assertThat(item.getSuggestedTitle()).isEqualTo("AI 标题");
        assertThat(item.getSuggestedSummary()).isEqualTo("AI 摘要");
        assertThat(item.getSuggestedTags()).containsExactly("标签1", "标签2");
        assertThat(item.getSuggestedCategory()).isEqualTo("STUDY");
        assertThat(item.getSuggestedContent()).isEqualTo("AI 正文");
    }

    private static KnowledgeItem confirmedItem(UUID id) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setOriginalContent("原始正文");
        item.setFinalTitle("标题");
        item.setFinalCategory("WORK");
        item.setStatus(ItemStatus.CONFIRMED);
        return item;
    }
}
