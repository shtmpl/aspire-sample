package me.sample.service.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.service.CompanyAuthorityService;
import me.sample.service.SecuredCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.Category;
import me.sample.domain.CategorySpecifications;
import me.sample.service.CategoryService;
import me.sample.service.SecurityService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
@Service
public class SecuredCategoryServiceImpl implements SecuredCategoryService {

    SecurityService securityService;
    CompanyAuthorityService companyAuthorityService;

    CategoryService categoryService;

    @Transactional(readOnly = true)
    @Override
    public List<Category> findCategories() {
        Long userId = securityService.getUserId();
        log.debug(".findCategories(), User.id: {}", userId);

        return categoryService.findCategories(CategorySpecifications.companyAuthorityUserIdEqualTo(userId));
    }

    @Transactional(readOnly = true)
    @Override
    public Optional<Category> findCategory(UUID id) {
        log.debug(".findCategory(id: {})", id);

        Category found = categoryService.findCategory(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkRead(found.getCompany().getId());

        return Optional.of(found);
    }

    @Override
    public Category saveCategory(Category data) {
        log.debug(".saveCategory()");

        companyAuthorityService.checkWrite(data.getCompany().getId());

        return categoryService.saveCategory(data);
    }

    @Override
    public Optional<Category> updateCategory(UUID id, Category data) {
        log.debug(".updateCategory(id: {})", id);

        Category found = categoryService.findCategory(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(data.getCompany().getId());
        companyAuthorityService.checkWrite(found.getCompany().getId());

        return categoryService.updateLocalCategory(id, data);
    }

    @Override
    public Optional<UUID> deleteCategory(UUID id) {
        log.debug(".deleteCategory(id: {})", id);

        Category found = categoryService.findCategory(id)
                .orElse(null);
        if (found == null) {
            return Optional.empty();
        }

        companyAuthorityService.checkWrite(found.getCompany().getId());

        return categoryService.deleteCategory(id);
    }
}
