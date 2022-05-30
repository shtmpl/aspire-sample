package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.PartnerRepository;
import me.sample.repository.PromoRepository;
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
import me.sample.dto.PartnerDTO;
import me.sample.dto.PromoDTO;
import me.sample.dto.PromoSearchDTO;
import me.sample.dto.StoreDTO;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Partner;
import me.sample.domain.Permission;
import me.sample.domain.Promo;
import me.sample.domain.PromoState;
import me.sample.domain.Source;
import me.sample.domain.Store;
import me.sample.service.SecurityService;

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
public class PromoRestIntegrationTest {

    private static final String PATH_API_PROMO = "/api/promos";

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

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PromoRepository promoRepository;

    private Company company;

    @Before
    public void setUp() throws Exception {
        promoRepository.deleteAll();

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
    public void shouldIndexPromos() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .build());

        List<Partner> partners = IntStream.range(0, 4)
                .mapToObj((int x) -> partnerRepository.save(Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .promos(Stream.of(promo).collect(Collectors.toSet()))
                        .build()))
                .collect(Collectors.toList());

        List<Store> stores = partners.stream()
                .flatMap((Partner partner) -> IntStream.range(0, 4)
                        .mapToObj((int x) -> storeRepository.save(Store.builder()
                                .partner(partner)
                                .name(String.valueOf(UUID.randomUUID()))
                                .lat(42.0)
                                .lon(42.0)
                                .promos(Stream.of(promo).collect(Collectors.toSet()))
                                .build())))
                .collect(Collectors.toList());

        List<PromoDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PROMO + "?page={page}&size={size}", 0, 100)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PromoDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(PromoDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(promo.getId()));
    }

    @Test
    public void shouldSearchPromos() throws Exception {
        String arbitraryString = String.valueOf(UUID.randomUUID());
        Promo promo = promoRepository.save(Promo.builder()
                .source(Source.LOCAL)
                .state(PromoState.ACTIVE)
                .name(arbitraryString)
                .description(arbitraryString)
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .promos(Stream.of(promo).collect(Collectors.toSet()))
                        .build());

        PromoSearchDTO request = PromoSearchDTO.builder()
                .query(arbitraryString)
                .source(Source.LOCAL)
                .state(PromoState.ACTIVE)
                .build();


        List<PromoDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<PromoDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(PromoDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds, containsInAnyOrder(promo.getId()));
    }

    @Test
    public void shouldShowPromoWithPartners() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Promo promo = promoRepository.save(Promo.builder()
                .build());

        partner.getPromos().add(promo);
        partnerRepository.save(partner);

        PromoDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PROMO + "/{id}", promo.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(PromoDTO.class);

        assertThat(response.getId(), is(promo.getId()));
    }

    @Test
    public void shouldShowPromoWithStores() throws Exception {
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

        Promo promo = promoRepository.save(Promo.builder()
                .build());

        store.getPromos().add(promo);
        storeRepository.save(store);

        PromoDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_PROMO + "/{id}", promo.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(PromoDTO.class);

        assertThat(response.getId(), is(promo.getId()));
    }

    @Test
    public void shouldNotSaveLocalPromoWithNoPartnersAndStores() throws Exception {
        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    public void shouldSavePromoForExistingAllowedPartner() throws Exception {
        Set<Partner> partners = IntStream.range(0, 1)
                .mapToObj((int x) -> Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .build())
                .map(partnerRepository::save)
                .collect(Collectors.toSet());

        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .partners(partners.stream()
                        .map(Partner::getId)
                        .map((UUID partnerId) ->
                                PartnerDTO.builder()
                                        .id(partnerId)
                                        .build())
                        .collect(Collectors.toList()))
                .build();

        PromoDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(201)
                .extract().as(PromoDTO.class);

        assertThat(response.getPartners().stream()
                        .map(PartnerDTO::getId)
                        .sorted()
                        .collect(Collectors.toList()),
                is(partners.stream()
                        .map(Partner::getId)
                        .sorted()
                        .collect(Collectors.toList())));
    }

    @Test
    public void shouldNotSavePromoForExistingForbiddenPartner() throws Exception {
        Set<Partner> partners = IntStream.range(0, 1)
                .mapToObj((int x) -> Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .build())
                .map(partnerRepository::save)
                .collect(Collectors.toSet());

        Partner forbidden = partnerRepository.save(Partner.builder()
                .company(companyRepository.save(Company.builder()
                        .build()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .partners(Stream.concat(Stream.of(forbidden.getId()), partners.stream().map(Partner::getId))
                        .map((UUID partnerId) ->
                                PartnerDTO.builder()
                                        .id(partnerId)
                                        .build())
                        .collect(Collectors.toList()))
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    public void shouldNotSavePromoForNonexistentPartner() throws Exception {
        Set<Partner> partners = IntStream.range(0, 1)
                .mapToObj((int x) -> Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .build())
                .map(partnerRepository::save)
                .collect(Collectors.toSet());

        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .partners(Stream.concat(Stream.of(UUID.randomUUID()), partners.stream().map(Partner::getId))
                        .map((UUID partnerId) ->
                                PartnerDTO.builder()
                                        .id(partnerId)
                                        .build())
                        .collect(Collectors.toList()))
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    public void shouldSavePromoForExistingAllowedStore() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Set<Store> stores = IntStream.range(0, 1)
                .mapToObj((int x) -> Store.builder()
                        .partner(partner)
                        .name(String.valueOf(UUID.randomUUID()))
                        .lat(42.0)
                        .lon(42.0)
                        .build())
                .map(storeRepository::save)
                .collect(Collectors.toSet());

        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .stores(stores.stream()
                        .map(Store::getId)
                        .map((UUID storeId) -> StoreDTO.builder()
                                .id(storeId)
                                .build())
                        .collect(Collectors.toList()))
                .build();

        PromoDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(201)
                .extract().as(PromoDTO.class);

        assertThat(response.getStores().stream()
                        .map(StoreDTO::getId)
                        .sorted()
                        .collect(Collectors.toList()),
                is(stores.stream()
                        .map(Store::getId)
                        .sorted()
                        .collect(Collectors.toList())));
    }

    @Test
    public void shouldNotSavePromoForExistingForbiddenStore() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Set<Store> stores = IntStream.range(0, 1)
                .mapToObj((int x) -> Store.builder()
                        .partner(partner)
                        .name(String.valueOf(UUID.randomUUID()))
                        .lat(42.0)
                        .lon(42.0)
                        .build())
                .map(storeRepository::save)
                .collect(Collectors.toSet());

        Store forbidden = storeRepository.save(Store.builder()
                .partner(partnerRepository.save(Partner.builder()
                        .company(companyRepository.save(Company.builder()
                                .build()))
                        .name(String.valueOf(UUID.randomUUID()))
                        .build()))
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .stores(Stream.concat(Stream.of(forbidden.getId()), stores.stream().map(Store::getId))
                        .map((UUID storeId) ->
                                StoreDTO.builder()
                                        .id(storeId)
                                        .build())
                        .collect(Collectors.toList()))
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(403);
    }

    @Test
    public void shouldNotSavePromoForNonexistentStore() throws Exception {
        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Set<Store> stores = IntStream.range(0, 1)
                .mapToObj((int x) -> Store.builder()
                        .partner(partner)
                        .name(String.valueOf(UUID.randomUUID()))
                        .lat(42.0)
                        .lon(42.0)
                        .build())
                .map(storeRepository::save)
                .collect(Collectors.toSet());

        PromoDTO request = PromoDTO.builder()
                .state(PromoState.ACTIVE)
                .stores(Stream.concat(Stream.of(UUID.randomUUID()), stores.stream().map(Store::getId))
                        .map((UUID storeId) ->
                                StoreDTO.builder()
                                        .id(storeId)
                                        .build())
                        .collect(Collectors.toList()))
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_PROMO)
                .then().log().all()
                .statusCode(404);
    }

    @Test
    public void shouldUpdateLocalPromo() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .state(PromoState.ACTIVE)
                .source(Source.LOCAL)
                .build());

        PromoDTO request = PromoDTO.builder()
                .id(promo.getId())
                .state(PromoState.INACTIVE)
                .build();

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .put(PATH_API_PROMO)
                .then().log().all()
                .statusCode(400);
    }

    @Test
    public void shouldDeleteLocalPromo() throws Exception {
        Promo promo = promoRepository.save(Promo.builder()
                .source(Source.LOCAL)
                .build());

        UUID id = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(PATH_API_PROMO + "/{id}", promo.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        Promo found = promoRepository.findById(id)
                .orElse(null);
        assertThat(found, is(nullValue()));
    }
}
