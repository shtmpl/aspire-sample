package me.sample.service;

import me.sample.repository.CategoryRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.BadResourceException;
import me.sample.domain.Category;
import me.sample.domain.Source;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryServiceIntegrationTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Before
    public void setUp() throws Exception {
        categoryRepository.deleteAll();
    }

    @Test
    public void shouldSaveCategory() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Category result = categoryService.saveCategory(Category.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        assertThat(result.getId(), is(notNullValue()));
        assertThat(result.getCreatedDate(), is(greaterThanOrEqualTo(now)));
        assertThat(result.getUpdatedDate(), is(greaterThanOrEqualTo(now)));
    }

    @Test
    public void shouldNotSaveCategoryWithExistingId() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        try {
            categoryService.saveCategory(Category.builder()
                    .id(category.getId())
                    .build());

            fail();
        } catch (BadResourceException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldUpdateLocalCategory() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .source(Source.LOCAL)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Category result = categoryService.updateLocalCategory(category.getId(), Category.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build())
                .orElseThrow(AssertionError::new);

        assertThat(result.getId(), is(category.getId()));
        assertThat(result.getCreatedDate(), is(category.getCreatedDate()));
        assertThat(result.getUpdatedDate(), is(greaterThan(category.getUpdatedDate())));
    }
}
