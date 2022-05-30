package me.sample.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Category;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    public void shouldSaveWithAssignedId() throws Exception {
        UUID id = UUID.randomUUID();
        Category category = categoryRepository.save(Category.builder()
                .id(id)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Assigned id: %s. Saved w/ id: %s%n", id, category.getId());

        assertThat(category.getId(), is(id));
    }

    @Test
    public void shouldSaveWithGeneratedId() throws Exception {
        Category category = categoryRepository.save(Category.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.printf("Saved w/ id: %s%n", category.getId());

        assertThat(category.getId(), is(notNullValue()));
    }
}
