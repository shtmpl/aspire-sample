package me.sample.service;

import me.sample.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SecuredCategoryService {

    List<Category> findCategories();

    Optional<Category> findCategory(UUID id);

    Category saveCategory(Category data);

    Optional<Category> updateCategory(UUID id, Category data);

    Optional<UUID> deleteCategory(UUID id);
}
