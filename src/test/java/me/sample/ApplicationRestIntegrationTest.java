package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
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
import me.sample.dto.ApplicationDTO;
import me.sample.dto.CompanyDTO;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Permission;
import me.sample.repository.ApplicationRepository;
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
public class ApplicationRestIntegrationTest {

    private static final String PATH_API_APPLICATION = "/api/applications";

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
    private ApplicationRepository applicationRepository;

    private Company company;

    @Before
    public void setUp() throws Exception {
        applicationRepository.deleteAll();

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
    public void shouldIndexApplications() throws Exception {
        Application application = applicationRepository.save(arbitraryApplicationForCompany(company));

        List<ApplicationDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_APPLICATION)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<ApplicationDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(ApplicationDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(application.getId()));
    }

    @Test
    public void apiShow() throws Exception {
        Application application = applicationRepository.save(arbitraryApplicationForCompany(company));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_APPLICATION + "/{id}", application.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void apiSave() throws Exception {
        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(arbitraryRequestApplicationForCompany(company.getId()))
                .when().log().all()
                .post(PATH_API_APPLICATION)
                .then().log().all()
                .statusCode(201);
    }

    @Test
    public void apiUpdate() throws Exception {
        Application application = applicationRepository.save(arbitraryApplicationForCompany(company));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(arbitraryRequestApplicationForCompany(company.getId(), application.getId()))
                .when().log().all()
                .put(PATH_API_APPLICATION)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void apiDelete() throws Exception {
        Application application = applicationRepository.save(arbitraryApplicationForCompany(company));

        UUID id = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_APPLICATION + "/{id}", application.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        Application found = applicationRepository.findById(id)
                .orElse(null);
        assertThat(found, is(nullValue()));
    }

    private static Application arbitraryApplicationForCompany(Company company) {
        return Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build();
    }

    private static ApplicationDTO arbitraryRequestApplicationForCompany(UUID companyId) {
        CompanyDTO requestCompany = new CompanyDTO();
        requestCompany.setId(companyId);

        ApplicationDTO result = new ApplicationDTO();
        result.setName(String.valueOf(UUID.randomUUID()));
        result.setApiKey(String.valueOf(UUID.randomUUID()));
        result.setCompany(requestCompany);

        return result;
    }

    private static ApplicationDTO arbitraryRequestApplicationForCompany(UUID companyId, UUID id) {
        CompanyDTO requestCompany = new CompanyDTO();
        requestCompany.setId(companyId);

        ApplicationDTO result = new ApplicationDTO();
        result.setId(id);
        result.setName(String.valueOf(UUID.randomUUID()));
        result.setApiKey(String.valueOf(UUID.randomUUID()));
        result.setCompany(requestCompany);

        return result;
    }
}
