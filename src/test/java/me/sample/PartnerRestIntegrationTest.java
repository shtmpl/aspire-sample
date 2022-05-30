package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.PartnerRepository;
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
import me.sample.dto.PartnerDTO;
import me.sample.dto.PartnerSearchDTO;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Partner;
import me.sample.domain.PartnerState;
import me.sample.domain.Permission;
import me.sample.domain.Source;
import me.sample.service.SecurityService;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PartnerRestIntegrationTest {

    private static final String PATH_API_PARTNER = "/api/partners";

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
    private PartnerRepository partnerRepository;

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

        partnerRepository.deleteAll();
    }

    @Test
    public void shouldIndexPartners() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .source(Source.LOCAL)
                .build());

        List<PartnerDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PARTNER)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PartnerDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(PartnerDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(partner.getId()));
    }

    @Test
    public void shouldIndexPartnersWithDefaultSort() throws Exception {
        List<Partner> partners = IntStream.range(0, 10)
                .mapToObj((int x) -> partnerRepository.save(Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .source(Source.LOCAL)
                        .build()))
                .collect(Collectors.toList());

        List<PartnerDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PARTNER)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PartnerDTO>>() {
                });

        List<UUID> responseIds = response.stream()
                .map(PartnerDTO::getId)
                .collect(Collectors.toList());

        assertThat(responseIds,
                is(responseIds.stream().sorted(Comparator.comparing(String::valueOf)).collect(Collectors.toList())));
    }

    @Test
    public void shouldSearchPartners() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .source(Source.LOCAL)
                .build());

        List<PartnerDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(PartnerSearchDTO.builder()
                        .id(partner.getId())
                        .query(partner.getName())
                        .build())
                .when().log().all()
                .post(PATH_API_PARTNER + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PartnerDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(PartnerDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, containsInAnyOrder(partner.getId()));
    }

    @Test
    public void shouldSearchPartnersWithDefaultSort() throws Exception {
        List<Partner> partners = IntStream.range(0, 10)
                .mapToObj((int x) -> partnerRepository.save(Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .source(Source.LOCAL)
                        .build()))
                .collect(Collectors.toList());

        List<PartnerDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(PartnerSearchDTO.builder()
                        .build())
                .when().log().all()
                .post(PATH_API_PARTNER + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PartnerDTO>>() {
                });

        List<UUID> responseIds = response.stream()
                .map(PartnerDTO::getId)
                .collect(Collectors.toList());

        assertThat(responseIds,
                is(responseIds.stream().sorted(Comparator.comparing(String::valueOf)).collect(Collectors.toList())));
    }

    @Test
    public void shouldSearchPartnersBySources() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .source(Source.LOCAL)
                .build());

        List<PartnerDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(PartnerSearchDTO.builder()
                        .sources(Stream.of(Source.LOCAL).collect(Collectors.toList()))
                        .build())
                .when().log().all()
                .post(PATH_API_PARTNER + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PartnerDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(PartnerDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds, containsInAnyOrder(partner.getId()));
    }

    @Test
    public void shouldShowPartner() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .source(Source.LOCAL)
                .build());

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PARTNER + "/{id}", partner.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void shouldSaveLocalPartner() throws Exception {
        PartnerDTO request = PartnerDTO.builder()
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .state(PartnerState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .descriptionFull(String.valueOf(UUID.randomUUID()))
                .descriptionShort(String.valueOf(UUID.randomUUID()))
                .siteUrl(String.valueOf(UUID.randomUUID()))
                .sitePayment(false)
                .deliveryRussia(false)
                .hasOnlineStore(false)
                .hasOnlineStore(false)
                .build();

        PartnerDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PARTNER)
                .then().log().all()
                .statusCode(201)
                .extract().as(PartnerDTO.class);

        assertThat(response.getSource(), is(Source.LOCAL));
        assertThat(response.getState(), is(request.getState()));
        assertThat(response.getName(), is(request.getName()));
        assertThat(response.getDescriptionFull(), is(request.getDescriptionFull()));
        assertThat(response.getDescriptionShort(), is(request.getDescriptionShort()));
        assertThat(response.getSiteUrl(), is(request.getSiteUrl()));
        assertThat(response.getSitePayment(), is(request.getSitePayment()));
        assertThat(response.getDeliveryRussia(), is(request.getDeliveryRussia()));
        assertThat(response.getHasOnlineStore(), is(request.getHasOnlineStore()));
        assertThat(response.getHasOfflineStore(), is(request.getHasOfflineStore()));

        Partner found = partnerRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getSource(), is(Source.LOCAL));
        assertThat(found.getState(), is(request.getState()));
        assertThat(found.getName(), is(request.getName()));
        assertThat(found.getDescriptionFull(), is(request.getDescriptionFull()));
        assertThat(found.getDescriptionShort(), is(request.getDescriptionShort()));
        assertThat(found.getSiteUrl(), is(request.getSiteUrl()));
        assertThat(found.getSitePayment(), is(request.getSitePayment()));
        assertThat(found.getDeliveryRussia(), is(request.getDeliveryRussia()));
        assertThat(found.getHasOnlineStore(), is(request.getHasOnlineStore()));
        assertThat(found.getHasOfflineStore(), is(request.getHasOfflineStore()));
    }

    @Test
    public void shouldUpdateLocalPartner() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .source(Source.LOCAL)
                .state(PartnerState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .descriptionFull(String.valueOf(UUID.randomUUID()))
                .descriptionShort(String.valueOf(UUID.randomUUID()))
                .siteUrl(String.valueOf(UUID.randomUUID()))
                .sitePayment(false)
                .deliveryRussia(false)
                .hasOnlineStore(false)
                .hasOfflineStore(false)
                .build());

        PartnerDTO request = PartnerDTO.builder()
                .id(partner.getId())
                .company(CompanyDTO.builder()
                        .id(company.getId())
                        .build())
                .state(PartnerState.INACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .descriptionFull(String.valueOf(UUID.randomUUID()))
                .descriptionShort(String.valueOf(UUID.randomUUID()))
                .siteUrl(String.valueOf(UUID.randomUUID()))
                .sitePayment(true)
                .deliveryRussia(true)
                .hasOnlineStore(true)
                .hasOfflineStore(true)
                .build();

        PartnerDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .put(PATH_API_PARTNER)
                .then().log().all()
                .statusCode(200)
                .extract().as(PartnerDTO.class);

        assertThat(response.getState(), is(PartnerState.INACTIVE));
        assertThat(response.getName(), is(request.getName()));
        assertThat(response.getDescriptionFull(), is(request.getDescriptionFull()));
        assertThat(response.getDescriptionShort(), is(request.getDescriptionShort()));
        assertThat(response.getSiteUrl(), is(request.getSiteUrl()));
        assertThat(response.getSitePayment(), is(request.getSitePayment()));
        assertThat(response.getDeliveryRussia(), is(request.getDeliveryRussia()));
        assertThat(response.getHasOnlineStore(), is(request.getHasOnlineStore()));
        assertThat(response.getHasOfflineStore(), is(request.getHasOfflineStore()));

        Partner found = partnerRepository.findById(partner.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getState(), is(request.getState()));
        assertThat(found.getName(), is(request.getName()));
        assertThat(found.getDescriptionFull(), is(request.getDescriptionFull()));
        assertThat(found.getDescriptionShort(), is(request.getDescriptionShort()));
        assertThat(found.getSiteUrl(), is(request.getSiteUrl()));
        assertThat(found.getSitePayment(), is(request.getSitePayment()));
        assertThat(found.getDeliveryRussia(), is(request.getDeliveryRussia()));
        assertThat(found.getHasOnlineStore(), is(request.getHasOnlineStore()));
        assertThat(found.getHasOfflineStore(), is(request.getHasOfflineStore()));
    }

    @Test
    public void shouldDeleteLocalPartner() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .source(Source.LOCAL)
                .build());

        UUID id = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_PARTNER + "/{id}", partner.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        Partner found = partnerRepository.findById(id)
                .orElse(null);
        assertThat(found, is(nullValue()));
    }
}
