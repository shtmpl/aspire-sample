package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.BadResourceException;
import me.sample.domain.Category;
import me.sample.domain.Source;
import me.sample.repository.CategoryRepository;
import me.sample.service.CategoryService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Slf4j
@CacheConfig(cacheNames = CategoryServiceImpl.CATEGORY_CACHE)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Service
public class CategoryServiceImpl implements CategoryService {

    public static final String CATEGORY_CACHE = "category";

    CategoryRepository categoryRepository;

    @Cacheable
    @Transactional(readOnly = true)
    @Override
    public List<Category> findCategories(Specification<Category> specification) {
        return categoryRepository.findAll(specification);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Category> findCategory(UUID id) {
        return categoryRepository.findById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Category> findCategory(Specification<Category> specification) {
        return categoryRepository.findOne(specification);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Category saveCategory(Category data) {
        UUID id = data.getId();
        if (id != null && categoryRepository.existsById(id)) {
            throw new BadResourceException(String.format("Category already exists for id: %s", id));
        }

        return categoryRepository.save(data);
    }

    @Override
    public Optional<Category> updateLocalCategory(UUID id, Category data) {
        Category found = categoryRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source foundSource = found.getSource();
        if (foundSource != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо редактирование созданной по интеграции категории: %s. Источник: %s", id, foundSource));
        }

        return Optional.of(updateCategory(found, data));
    }

    @Override
    public Optional<Category> updateImportedCategory(UUID id, Category data) {
        Category found = categoryRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        return Optional.of(updateCategory(found, data));
    }

    private Category updateCategory(Category found, Category category) {
        String name = category.getName();
        if (name != null && !name.equals(found.getName())) {
            found.setName(name);
        }

        String description = category.getDescription();
        if (description != null && !description.equals(found.getDescription())) {
            found.setDescription(description);
        }

        return categoryRepository.save(found);
    }

    @CacheEvict(allEntries = true)
    @Override
    public Optional<UUID> deleteCategory(UUID id) {
        Category found = categoryRepository.findById(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        Source foundSource = found.getSource();
        if (foundSource != Source.LOCAL) {
            throw new UnsupportedOperationException(String.format(
                    "Недопустимо удаление созданной по интеграции категории: %s. Источник: %s", id, foundSource));
        }

        categoryRepository.delete(found);

        return Optional.of(id);
    }
}
