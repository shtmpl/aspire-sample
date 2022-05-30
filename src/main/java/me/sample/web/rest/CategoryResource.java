package me.sample.web.rest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import me.sample.dto.CategoryDTO;
import me.sample.service.SecuredCategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import me.sample.mapper.CategoryMapper;
import me.sample.domain.Category;
import me.sample.domain.NotFoundResourceException;
import me.sample.domain.Source;
import me.sample.web.rest.errors.BadRequestAlertException;
import me.sample.web.rest.util.HeaderUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_MANAGER')")
@RequestMapping("/api/categories")
@RestController
public class CategoryResource {
    private static final String ENTITY_NAME = "samplebackendCategory";

    SecuredCategoryService securedCategoryService;

    CategoryMapper categoryMapper;

    @GetMapping
    public ResponseEntity<List<CategoryDTO>> index() {
        log.debug("REST request to get a page of Companies");

        List<Category> categories = securedCategoryService.findCategories();

        return ResponseEntity.ok()
                .body(categories.stream().map(categoryMapper::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDTO> show(@PathVariable UUID id) {
        log.debug("REST request to get Category : {}", id);

        Category category = securedCategoryService.findCategory(id)
                .orElseThrow(() -> new NotFoundResourceException("Category", id));

        return ResponseEntity.ok(categoryMapper.toDto(category));
    }

    @PostMapping
    public ResponseEntity<CategoryDTO> save(@RequestBody CategoryDTO request) throws URISyntaxException {
        log.debug("REST request to save Category : {}", request);

        if (request.getId() != null) {
            throw new BadRequestAlertException("A new category cannot already have an ID", ENTITY_NAME, "idexists");
        }

        if (request.getCompany() == null || request.getCompany().getId() == null) {
            throw new BadRequestAlertException("No company id provided", ENTITY_NAME, "idnull");
        }

        request.setSource(Source.LOCAL);
        Category category = securedCategoryService.saveCategory(categoryMapper.toEntity(request));
        CategoryDTO result = categoryMapper.toDto(category);

        return ResponseEntity.created(new URI("/api/categories/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, category.getId().toString()))
                .body(result);
    }

    @PutMapping
    public ResponseEntity<CategoryDTO> update(@RequestBody CategoryDTO request) throws URISyntaxException {
        log.debug("REST request to update Category : {}", request);

        UUID id = request.getId();
        if (id == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }

        Category category = securedCategoryService.updateCategory(id, categoryMapper.toEntity(request))
                .orElseThrow(() -> new NotFoundResourceException("Category", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, id.toString()))
                .body(categoryMapper.toDto(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UUID> delete(@PathVariable UUID id) {
        log.debug("REST request to delete Category : {}", id);
        UUID result = securedCategoryService.deleteCategory(id)
                .orElseThrow(() -> new NotFoundResourceException("Category", id));

        return ResponseEntity.ok()
                .headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString()))
                .body(result);
    }
}
