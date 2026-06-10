package com.exam.ideaforge.repository;

import com.exam.ideaforge.entity.ItemStatus;
import com.exam.ideaforge.entity.KnowledgeItem;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 标签/类别搜索集成测试，依赖 VM PostgreSQL（application.yml 数据源）。
 * 本地无数据库时保持 @Disabled；手动验收见类注释。
 *
 * 手动验收步骤：
 * 1. 录入想法并确认保存
 * 2. GET /api/ideas/search?category=INSPIRATION → 返回该类别所有条目
 * 3. GET /api/ideas/search?q=标签关键词 → 有结果
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Disabled("Requires PostgreSQL VM at 192.168.226.131 — run manually when DB is available")
class KnowledgeItemRepositorySearchTest {

    @Autowired
    private KnowledgeItemRepository repository;

    @Test
    void search_matchesTagsAndCategory() {
        KnowledgeItem item = new KnowledgeItem();
        item.setOriginalContent("原始正文内容");
        item.setFinalTitle("测试标题");
        item.setFinalSummary("测试摘要");
        item.setFinalTags(new String[]{"机器学习", "AI"});
        item.setFinalCategory("WORK");
        item.setStatus(ItemStatus.CONFIRMED);
        repository.saveAndFlush(item);

        assertThat(repository.searchByKeyword("机器"))
                .extracting(KnowledgeItem::getFinalTitle)
                .contains("测试标题");

        assertThat(repository.searchByKeyword("AI"))
                .extracting(KnowledgeItem::getFinalTitle)
                .contains("测试标题");

        assertThat(repository.findConfirmedByCategory("WORK"))
                .extracting(KnowledgeItem::getFinalTitle)
                .contains("测试标题");
    }
}
