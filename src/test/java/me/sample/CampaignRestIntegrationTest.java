package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CampaignRepository;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.DistributionRepository;
import me.sample.repository.NotificationTemplateRepository;
import me.sample.repository.PartnerRepository;
import me.sample.repository.ScheduledGeoposDisseminationRepository;
import me.sample.repository.StoreRepository;
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
import me.sample.dto.CampaignDTO;
import me.sample.dto.CampaignSearchDTO;
import me.sample.dto.CompanyDTO;
import me.sample.dto.DistributionDTO;
import me.sample.dto.NotificationTemplateDTO;
import me.sample.dto.PartnerDTO;
import me.sample.dto.ScheduledGeoposDisseminationDTO;
import me.sample.dto.StoreDTO;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.DisseminationSchedule;
import me.sample.domain.Distribution;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Partner;
import me.sample.domain.Permission;
import me.sample.domain.ScheduledGeoposDissemination;
import me.sample.domain.Store;
import me.sample.repository.CampaignClientRepository;
import me.sample.service.SecurityService;
import me.sample.web.rest.ContentTypes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CampaignRestIntegrationTest {

    private static final String PATH_API_CAMPAIGN = "/api/campaigns";

    private static final Long USER_ID = 42L;

    @LocalServerPort
    private int port;

    @MockBean
    private SecurityService securityService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CompanyAuthorityRepository companyAuthorityRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignClientRepository campaignClientRepository;

    @Autowired
    private DistributionRepository distributionRepository;

    @Autowired
    private ScheduledGeoposDisseminationRepository scheduledGeoposDisseminationRepository;

    private Company company;
    private NotificationTemplate notificationTemplate;

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

        notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build());
    }

    @Test
    public void shouldIndexCampaigns() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build());

        List<CampaignDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<CampaignDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(CampaignDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(campaign.getId()));
    }

    @Test
    public void shouldSearchCampaignsByStates() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        List<CampaignDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(CampaignSearchDTO.builder()
                        .states(Stream.of(CampaignState.RUNNING).collect(Collectors.toList()))
                        .build())
                .when().log().all()
                .post(PATH_API_CAMPAIGN + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<CampaignDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(CampaignDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds, containsInAnyOrder(campaign.getId()));
    }

    @Test
    public void shouldShowCampaign() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build());

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CAMPAIGN + "/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void shouldShowScheduledGeoposDissemination() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .scheduledGeoposDissemination(scheduledGeoposDisseminationRepository.save(ScheduledGeoposDissemination.builder()
                        .partners(Stream.of(partner).collect(Collectors.toSet()))
                        .stores(Stream.of(store).collect(Collectors.toSet()))
                        .build()))
                .build());

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CAMPAIGN + "/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void shouldSaveDistribution() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(DistributionDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now.toLocalDate())
                        .end(now.toLocalDate())
                        .build())
                .build();

        CampaignDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(201)
                .extract().as(CampaignDTO.class);

        assertThat(response.getId(), is(notNullValue()));
        assertThat(response.getDistribution(), is(notNullValue()));
        assertThat(response.getDistribution().getId(), is(notNullValue()));
        assertThat(response.getDistribution().getSchedule(), is(request.getDistribution().getSchedule()));

        Campaign found = campaignRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getNotificationTemplate(), is(notNullValue()));
        assertThat(found.getDistribution(), is(notNullValue()));
        assertThat(found.getDistribution().getCron(), is(request.getDistribution().getSchedule().getCronExpression()));
    }

    @Test
    public void shouldSaveScheduledGeoposDissemination() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .scheduledGeoposDissemination(ScheduledGeoposDisseminationDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now)
                        .end(now)
                        .partners(Stream.of(partner)
                                .map((Partner it) -> PartnerDTO.builder()
                                        .id(it.getId())
                                        .build())
                                .collect(Collectors.toList()))
                        .stores(Stream.of(store)
                                .map((Store it) -> StoreDTO.builder()
                                        .id(it.getId())
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .build();

        CampaignDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(201)
                .extract().as(CampaignDTO.class);

        assertThat(response.getId(), is(notNullValue()));
        assertThat(response.getScheduledGeoposDissemination(), is(notNullValue()));
        assertThat(response.getScheduledGeoposDissemination().getId(), is(notNullValue()));
        assertThat(response.getScheduledGeoposDissemination().getSchedule(), is(request.getScheduledGeoposDissemination().getSchedule()));

        Campaign found = campaignRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getNotificationTemplate(), is(notNullValue()));
        assertThat(found.getScheduledGeoposDissemination(), is(notNullValue()));
        assertThat(found.getScheduledGeoposDissemination().getCron(), is(request.getScheduledGeoposDissemination().getSchedule().getCronExpression()));
    }

    @Test
    public void shouldNotSaveCampaignWithNoDistributionAndScheduledGeoposDissemination() throws Exception {
        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    public void shouldNotSaveCampaignWithBothDistributionAndScheduledGeoposDissemination() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(DistributionDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now.toLocalDate())
                        .end(now.toLocalDate())
                        .build())
                .scheduledGeoposDissemination(ScheduledGeoposDisseminationDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now)
                        .end(now)
                        .build())
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    public void shouldSaveCampaignWithCsvClientData() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(DistributionDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now.toLocalDate())
                        .end(now.toLocalDate())
                        .build())
                .clientIdDataContentType(ContentTypes.CSV)
                .clientIdDataBase64(Base64.getEncoder().encodeToString(Resources.slurpBytes("client/client-ids.csv")))
                .build();

        CampaignDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(201)
                .extract().as(CampaignDTO.class);

        assertThat(response.getId(), is(notNullValue()));
        assertThat(response.getDistribution(), is(notNullValue()));
        assertThat(response.getDistribution().getId(), is(notNullValue()));
        assertThat(response.getDistribution().getSchedule(), is(request.getDistribution().getSchedule()));

        Campaign found = campaignRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getNotificationTemplate(), is(notNullValue()));
        assertThat(found.getDistribution(), is(notNullValue()));
        assertThat(found.getDistribution().getCron(), is(request.getDistribution().getSchedule().getCronExpression()));

        assertThat(campaignClientRepository.existsByCampaignId(response.getId()), is(true));
    }

    @Test
    public void shouldSaveCampaignWithXlsClientData() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(DistributionDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now.toLocalDate())
                        .end(now.toLocalDate())
                        .build())
                .clientIdDataContentType(ContentTypes.XLS)
                .clientIdDataBase64(Base64.getEncoder().encodeToString(Resources.slurpBytes("client/client-ids.xls")))
                .build();

        CampaignDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(201)
                .extract().as(CampaignDTO.class);

        assertThat(response.getId(), is(notNullValue()));
        assertThat(response.getDistribution(), is(notNullValue()));
        assertThat(response.getDistribution().getId(), is(notNullValue()));
        assertThat(response.getDistribution().getSchedule(), is(request.getDistribution().getSchedule()));

        Campaign found = campaignRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getNotificationTemplate(), is(notNullValue()));
        assertThat(found.getDistribution(), is(notNullValue()));
        assertThat(found.getDistribution().getCron(), is(request.getDistribution().getSchedule().getCronExpression()));

        assertThat(campaignClientRepository.existsByCampaignId(response.getId()), is(true));
    }

    @Test
    public void shouldNotSaveCampaignWithInvalidClientData() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        CampaignDTO request = CampaignDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .notificationTemplate(NotificationTemplateDTO.builder()
                        .id(notificationTemplate.getId())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(DistributionDTO.builder()
                        .schedule(DisseminationSchedule.EVERY_MINUTE)
                        .start(now.toLocalDate())
                        .end(now.toLocalDate())
                        .build())
                .clientIdDataContentType(ContentTypes.XLS)
                .clientIdDataBase64(Base64.getEncoder().encodeToString("Invalid".getBytes(StandardCharsets.UTF_8)))
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_CAMPAIGN)
                .then().log().all()
                .statusCode(500);
    }

    @Test
    public void shouldStartCampaign() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build());

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CAMPAIGN + "/start/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200);

        Campaign result = campaignRepository.findById(campaign.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getState(), is(CampaignState.RUNNING));
    }

    @Test
    public void shouldPauseCampaign() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build());

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CAMPAIGN + "/pause/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200);

        Campaign result = campaignRepository.findById(campaign.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getState(), is(CampaignState.PAUSE));
    }

    @Test
    public void shouldCompleteCampaign() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .company(company)
                .build());

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_CAMPAIGN + "/complete/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200);

        Campaign result = campaignRepository.findById(campaign.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getState(), is(CampaignState.COMPLETED));
    }

    @Test
    public void shouldDeleteDistribution() throws Exception {
        Distribution dissemination = distributionRepository.save(Distribution.builder()
                .build());
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(dissemination)
                .build());


        UUID responseId = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_CAMPAIGN + "/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);


        assertThat(responseId, is(campaign.getId()));

        assertThat(campaignRepository.findById(campaign.getId()),
                is(Optional.empty()));
        assertThat(distributionRepository.findById(dissemination.getId()),
                is(Optional.empty()));
    }

    @Test
    public void shouldDeleteScheduledGeoposDissemination() throws Exception {
        ScheduledGeoposDissemination dissemination = scheduledGeoposDisseminationRepository.save(ScheduledGeoposDissemination.builder()
                .build());
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .scheduledGeoposDissemination(dissemination)
                .build());


        UUID responseId = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_CAMPAIGN + "/{id}", campaign.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);


        assertThat(responseId, is(campaign.getId()));

        assertThat(campaignRepository.findById(responseId),
                is(Optional.empty()));
        assertThat(scheduledGeoposDisseminationRepository.findById(dissemination.getId()),
                is(Optional.empty()));
    }

}
