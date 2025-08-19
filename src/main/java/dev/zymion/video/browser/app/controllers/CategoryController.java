package dev.zymion.video.browser.app.controllers;

import dev.zymion.video.browser.app.models.dto.ShowDto;
import dev.zymion.video.browser.app.models.entities.CategoryEntity;
import dev.zymion.video.browser.app.services.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    @GetMapping("find/all")
    public ResponseEntity<List<CategoryEntity>> findAllCategories() {
        List<CategoryEntity> result = categoryService.findAll();
        return ResponseEntity.ok(result);
    }

}
