package com.exam.ideaforge.service;

import com.exam.ideaforge.entity.IdeaCategory;
import com.exam.ideaforge.entity.ItemStatus;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdeaSearchServiceTest {

    @Mock
    private KnowledgeItemRepository repository;

    @InjectMocks
    private IdeaSearchService searchService;

    @Test
    void search_keywordOnly_doesNotResolveCategory() {
        when(repository.searchByKeyword("工作")).thenReturn(List.of());

        searchService.search("工作", null);

        verify(repository).searchByKeyword("工作");
    }

    @Test
    void search_categoryOnly_returnsCategoryItems() {
        KnowledgeItem item = buildItem(UUID.randomUUID(), "工作笔记", IdeaCategory.WORK, OffsetDateTime.now());
        when(repository.findConfirmedByCategory(IdeaCategory.WORK.name()))
                .thenReturn(List.of(item));

        var results = searchService.search(null, IdeaCategory.WORK);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getFinalCategory()).isEqualTo(IdeaCategory.WORK);
    }

    @Test
    void search_keywordAndCategory_intersectsResults() {
        KnowledgeItem workItem = buildItem(UUID.randomUUID(), "工作计划", IdeaCategory.WORK, OffsetDateTime.now());
        KnowledgeItem todoItem = buildItem(UUID.randomUUID(), "正常工作", IdeaCategory.TODO, OffsetDateTime.now());

        when(repository.searchByKeyword("工作")).thenReturn(List.of(workItem, todoItem));

        var results = searchService.search("工作", IdeaCategory.WORK);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getFinalTitle()).isEqualTo("工作计划");
    }

    @Test
    void search_returnsMappedResponses() {
        KnowledgeItem item = buildItem(UUID.randomUUID(), "标题", IdeaCategory.STUDY, OffsetDateTime.now());
        item.setFinalTags(new String[]{"AI", "机器学习"});

        when(repository.searchByKeyword("AI")).thenReturn(List.of(item));

        var results = searchService.search("AI", null);

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getFinalTitle()).isEqualTo("标题");
        assertThat(results.getFirst().getFinalTags()).containsExactly("AI", "机器学习");
    }

    @Test
    void search_returnsEmptyWhenNoKeywordAndNoCategory() {
        assertThat(searchService.search(null, null)).isEmpty();
        assertThat(searchService.search("", null)).isEmpty();
        assertThat(searchService.search("  ", null)).isEmpty();
    }

    private static KnowledgeItem buildItem(UUID id, String title, IdeaCategory category, OffsetDateTime createdAt) {
        KnowledgeItem item = new KnowledgeItem();
        item.setId(id);
        item.setOriginalContent("正文");
        item.setFinalTitle(title);
        item.setFinalCategory(category);
        item.setStatus(ItemStatus.CONFIRMED);
        item.setCreatedAt(createdAt);
        return item;
    }
}
