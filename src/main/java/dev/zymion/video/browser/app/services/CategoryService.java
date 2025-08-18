package dev.zymion.video.browser.app.services;

import dev.zymion.video.browser.app.models.entities.CategoryEntity;
import dev.zymion.video.browser.app.repositories.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryEntity> findAll() {
        return categoryRepository.findAll();
    }
}
