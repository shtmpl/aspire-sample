package me.sample.service;

import me.sample.domain.Category;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryService {

    List<Category> findCategories(Specification<Category> specification);

    Optional<Category> findCategory(UUID id);

    Optional<Category> findCategory(Specification<Category> specification);

    Category saveCategory(Category data);

    Optional<Category> updateLocalCategory(UUID id, Category data);

    Optional<Category> updateImportedCategory(UUID id, Category data);

    Optional<UUID> deleteCategory(UUID id);
}
