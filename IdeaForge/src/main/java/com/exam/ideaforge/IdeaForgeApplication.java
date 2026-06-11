package com.exam.ideaforge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class IdeaForgeApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdeaForgeApplication.class, args);
    }

}
