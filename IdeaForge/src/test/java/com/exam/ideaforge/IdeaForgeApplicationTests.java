package com.exam.ideaforge;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.jpa.hibernate.ddl-auto=update")
class IdeaForgeApplicationTests {

    @Test
    void contextLoads() {
    }

}
