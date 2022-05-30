package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CategoryRepository;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.config.NoSecurityConfiguration;
import me.sample.dto.CategoryDTO;
import me.sample.dto.CompanyDTO;
import me.sample.domain.Category;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Permission;
import me.sample.domain.Source;
import me.sample.service.SecurityService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CategoryRestIntegrationTest {

    private static final String PATH_API_CATEGORY = "/api/categories";

    private static final Long USER_ID = 42L;

    @LocalServerPort
    private int port;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private CompanyAuthorityRepository companyAuthorityRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Company company;

    @Before
    public void setUp() throws Exception {
        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);

        company = companyRepository.save(Company.builder().build());
        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());
    }

    @Test
    public void shouldIndexCategories() throws Exception {
        Category category = categoryRepository.save(arbitraryCategoryForCompany(company));

        List<CategoryDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CATEGORY)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<CategoryDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(CategoryDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(category.getId()));
    }

    @Test
    public void apiShow() throws Exception {
        Category category = categoryRepository.save(arbitraryCategoryForCompany(company));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CATEGORY + "/{id}", category.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void shouldSaveLocalCategory() throws Exception {
        CategoryDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(CategoryDTO.builder()
                        .company(CompanyDTO.builder()
                                .id(company.getId())
                                .build())
                        .name(String.valueOf(UUID.randomUUID()))
                        .build())
                .when().log().all()
                .post(PATH_API_CATEGORY)
                .then().log().all()
                .statusCode(201)
                .extract().as(CategoryDTO.class);

        assertThat(response.getSource(), is(Source.LOCAL));

        Category found = categoryRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getSource(), is(Source.LOCAL));
    }

    @Test
    public void apiUpdate() throws Exception {
        Category category = categoryRepository.save(arbitraryCategoryForCompany(company));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(arbitraryRequestCategoryForCompany(company.getId(), category.getId()))
                .when().log().all()
                .put(PATH_API_CATEGORY)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void apiDelete() throws Exception {
        Category category = categoryRepository.save(arbitraryCategoryForCompany(company));

        UUID id = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_CATEGORY + "/{id}", category.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        Category found = categoryRepository.findById(id)
                .orElse(null);
        assertThat(found, is(nullValue()));
    }

    private static Category arbitraryCategoryForCompany(Company company) {
        return Category.builder()
                .source(Source.LOCAL)
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build();
    }

    private static CategoryDTO arbitraryRequestCategoryForCompany(UUID companyId, UUID id) {
        CompanyDTO requestCompany = new CompanyDTO();
        requestCompany.setId(companyId);

        CategoryDTO result = new CategoryDTO();
        result.setId(id);
        result.setName(String.valueOf(UUID.randomUUID()));
        result.setCompany(requestCompany);

        return result;
    }
}
