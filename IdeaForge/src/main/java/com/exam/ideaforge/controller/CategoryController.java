package com.exam.ideaforge.controller;

import com.exam.ideaforge.dto.CategoryRequest;
import com.exam.ideaforge.dto.CategoryResponse;
import com.exam.ideaforge.dto.CategoryUpdateRequest;
import com.exam.ideaforge.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 想法类别字典 REST 接口 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /** 默认只返回未删除类别；all=true 含已软删除项（供搜索页历史 label 映射） */
    @GetMapping
    public List<CategoryResponse> list(@RequestParam(value = "all", defaultValue = "false") boolean all) {
        return categoryService.list(all);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
        return categoryService.create(request);
    }

    @PutMapping("/{id}")
    public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryUpdateRequest request) {
        return categoryService.update(id, request);
    }

    /** 软删除：enabled=false，DB 行保留，后期可手动硬删 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        categoryService.softDelete(id);
    }
}
