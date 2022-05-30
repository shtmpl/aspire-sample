package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CampaignRepository;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.NotificationTemplateRepository;
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
import me.sample.dto.CompanyDTO;
import me.sample.dto.NotificationTemplateDTO;
import me.sample.domain.Campaign;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Permission;
import me.sample.service.SecurityService;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationTemplateRestIntegrationTest {

    private static final String PATH_API_NOTIFICATION_TEMPLATE = "/api/notification-templates";

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
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private CampaignRepository campaignRepository;

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

        campaignRepository.deleteAll();
        notificationTemplateRepository.deleteAll();
    }

    @Test
    public void shouldIndexNotificationTemplates() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(arbitraryNotificationTemplateForCompany(company));

        List<NotificationTemplateDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_NOTIFICATION_TEMPLATE)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<NotificationTemplateDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(NotificationTemplateDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(notificationTemplate.getId()));
    }

    @Test
    public void shouldShowNotificationTemplate() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(arbitraryNotificationTemplateForCompany(company));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_NOTIFICATION_TEMPLATE + "/{id}", notificationTemplate.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void shouldShowNotificationTemplateWithDisseminationAssociations() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .notificationTemplate(notificationTemplate)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        NotificationTemplateDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_NOTIFICATION_TEMPLATE + "/{id}", notificationTemplate.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(NotificationTemplateDTO.class);

        assertThat(response.getDisseminationIds(),
                containsInAnyOrder(campaign.getId()));
    }

    @Test
    public void apiSave() throws Exception {
        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(arbitraryRequestNotificationTemplateForCompany(company.getId()))
                .when().log().all()
                .post(PATH_API_NOTIFICATION_TEMPLATE)
                .then().log().all()
                .statusCode(201);
    }

    @Test
    public void apiUpdate() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(arbitraryNotificationTemplateForCompany(company));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(arbitraryRequestNotificationTemplateForCompany(company.getId(), notificationTemplate.getId()))
                .when().log().all()
                .put(PATH_API_NOTIFICATION_TEMPLATE)
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void apiDelete() throws Exception {
        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(arbitraryNotificationTemplateForCompany(company));

        UUID id = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_NOTIFICATION_TEMPLATE + "/{id}", notificationTemplate.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        NotificationTemplate found = notificationTemplateRepository.findById(id)
                .orElse(null);
        assertThat(found, is(nullValue()));
    }

    private static NotificationTemplate arbitraryNotificationTemplateForCompany(Company company) {
        return NotificationTemplate.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build();
    }

    private static NotificationTemplateDTO arbitraryRequestNotificationTemplateForCompany(UUID companyId) {
        CompanyDTO requestCompany = new CompanyDTO();
        requestCompany.setId(companyId);

        NotificationTemplateDTO result = new NotificationTemplateDTO();
        result.setName(String.valueOf(UUID.randomUUID()));
        result.setCompany(requestCompany);

        return result;
    }

    private static NotificationTemplateDTO arbitraryRequestNotificationTemplateForCompany(UUID companyId, UUID id) {
        CompanyDTO requestCompany = new CompanyDTO();
        requestCompany.setId(companyId);

        NotificationTemplateDTO result = new NotificationTemplateDTO();
        result.setId(id);
        result.setName(String.valueOf(UUID.randomUUID()));
        result.setCompany(requestCompany);

        return result;
    }
}
