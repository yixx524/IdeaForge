package com.exam.ideaforge.service;

import com.exam.ideaforge.entity.ItemStatus;
import com.exam.ideaforge.entity.KnowledgeItem;
import com.exam.ideaforge.repository.KnowledgeItemRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        when(repository.searchByKeyword(eq("工作"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        searchService.search("工作", null, 0, 20);

        verify(repository).searchByKeyword(eq("工作"), any(Pageable.class));
    }

    @Test
    void search_categoryOnly_returnsCategoryItems() {
        KnowledgeItem item = buildItem(UUID.randomUUID(), "工作笔记", "WORK", OffsetDateTime.now());
        when(repository.findConfirmedByCategory(eq("WORK"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        var results = searchService.search(null, "WORK", 0, 20);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getFinalCategory()).isEqualTo("WORK");
    }

    @Test
    void search_keywordAndCategory_usesCombinedRepositoryQuery() {
        KnowledgeItem workItem = buildItem(UUID.randomUUID(), "工作计划", "WORK", OffsetDateTime.now());
        when(repository.searchByKeywordAndCategory(eq("工作"), eq("WORK"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(workItem)));

        var results = searchService.search("工作", "WORK", 0, 20);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getFinalTitle()).isEqualTo("工作计划");
        verify(repository).searchByKeywordAndCategory(eq("工作"), eq("WORK"), any(Pageable.class));
    }

    @Test
    void search_returnsMappedResponses() {
        KnowledgeItem item = buildItem(UUID.randomUUID(), "标题", "STUDY", OffsetDateTime.now());
        item.setFinalTags(new String[]{"AI", "机器学习"});

        when(repository.searchByKeyword(eq("AI"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        var results = searchService.search("AI", null, 0, 20);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getFinalTitle()).isEqualTo("标题");
        assertThat(results.getContent().getFirst().getFinalTags()).containsExactly("AI", "机器学习");
    }

    @Test
    void search_noFilters_returnsPaginatedConfirmedItems() {
        KnowledgeItem item = buildItem(UUID.randomUUID(), "最近条目", "LIFE", OffsetDateTime.now());
        when(repository.findAllConfirmedOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item), Pageable.ofSize(20), 1));

        var results = searchService.search(null, null, 0, 20);

        assertThat(results.getContent()).hasSize(1);
        assertThat(results.getContent().getFirst().getFinalTitle()).isEqualTo("最近条目");
        assertThat(results.getTotalElements()).isEqualTo(1);
        verify(repository).findAllConfirmedOrderByCreatedAtDesc(any(Pageable.class));
    }

    @Test
    void search_clampsPageSizeToMax() {
        when(repository.findAllConfirmedOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(Page.empty());

        searchService.search(null, null, 0, 500);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAllConfirmedOrderByCreatedAtDesc(captor.capture());
        assertThat(captor.getValue().getPageSize()).isEqualTo(IdeaSearchService.MAX_PAGE_SIZE);
    }

    private static KnowledgeItem buildItem(UUID id, String title, String category, OffsetDateTime createdAt) {
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
