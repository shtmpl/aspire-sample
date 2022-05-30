package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.PartnerRepository;
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
import me.sample.dto.CompanyDTO;
import me.sample.dto.PartnerDTO;
import me.sample.dto.StoreDTO;
import me.sample.dto.StoreSearchDTO;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Partner;
import me.sample.domain.Permission;
import me.sample.domain.Source;
import me.sample.domain.Store;
import me.sample.domain.StoreState;
import me.sample.service.SecurityService;

import java.util.Collection;
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
public class StoreRestIntegrationTest {

    private static final String API_PATH = "/api/stores";

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

    private Company company;
    private Partner partner;

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

        partner = partnerRepository.save(Partner.builder().company(company).build());

        storeRepository.deleteAll();
    }

    @Test
    public void shouldIndexStores() throws Exception {
        Store store = storeRepository.save(arbitraryStoreForPartner(partner));

        List<StoreDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<StoreDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(StoreDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(store.getId()));
    }

    @Test
    public void shouldSearchStores() throws Exception {
        IntStream.range(0, 100)
                .mapToObj((int x) -> arbitraryStoreForPartner(partner))
                .forEach(storeRepository::save);

        Set<UUID> storeIds = IntStream.range(0, 5)
                .mapToObj((int page) ->
                        RestAssured.given()
                                .baseUri("http://localhost:" + port)
                                .contentType(ContentType.JSON)
                                .body("{}")
                                .when().log().all()
                                .post(API_PATH + "/search?page={page}&size={size}", page, 20)
                                .then().log().all()
                                .statusCode(200)
                                .extract().as(new TypeRef<List<StoreDTO>>() {
                        }))
                .flatMap(Collection::stream)
                .map(StoreDTO::getId)
                .collect(Collectors.toSet());

        assertThat(storeIds.size(), is(100));
    }

    @Test
    public void shouldSearchStoresBySources() throws Exception {
        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .source(Source.LOCAL)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        List<StoreDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(StoreSearchDTO.builder()
                        .sources(Stream.of(Source.LOCAL).collect(Collectors.toList()))
                        .build())
                .when().log().all()
                .post(API_PATH + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<StoreDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(StoreDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds, containsInAnyOrder(store.getId()));
    }

    @Test
    public void shouldSearchStoresByStates() throws Exception {
        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .state(StoreState.INACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        List<StoreDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(StoreSearchDTO.builder()
                        .states(Stream.of(StoreState.INACTIVE).collect(Collectors.toList()))
                        .build())
                .when().log().all()
                .post(API_PATH + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<StoreDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(StoreDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds, containsInAnyOrder(store.getId()));
    }

    @Test
    public void shouldCountSearchedStores() throws Exception {
        storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Long response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body("{}")
                .when().log().all()
                .post(API_PATH + "/search/count")
                .then().log().all()
                .statusCode(200)
                .extract().as(Long.class);

        assertThat(response, is(1L));
    }

    @Test
    public void apiShow() throws Exception {
        Store store = storeRepository.save(arbitraryStoreForPartner(partner));

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/{id}", store.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Test
    public void shouldSaveLocalStore() throws Exception {
        StoreDTO request = StoreDTO.builder()
                .partner(PartnerDTO.builder()
                        .id(partner.getId())
                        .company(CompanyDTO.builder()
                                .id(company.getId())
                                .build())
                        .build())
                .name(String.valueOf(UUID.randomUUID()))
                .state(StoreState.ACTIVE)
                .lat(42.0)
                .lon(42.0)
                .radius(42L)
                .city(String.valueOf(UUID.randomUUID()))
                .address(String.valueOf(UUID.randomUUID()))
                .build();

        StoreDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH)
                .then().log().all()
                .statusCode(201)
                .extract().as(StoreDTO.class);

        assertThat(response.getSource(), is(Source.LOCAL));
        assertThat(response.getState(), is(request.getState()));
        assertThat(response.getName(), is(request.getName()));
        assertThat(response.getLat(), is(request.getLat()));
        assertThat(response.getLon(), is(request.getLon()));
        assertThat(response.getCity(), is(request.getCity()));
        assertThat(response.getAddress(), is(request.getAddress()));

        Store found = storeRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getSource(), is(Source.LOCAL));
        assertThat(found.getState(), is(request.getState()));
        assertThat(found.getName(), is(request.getName()));
        assertThat(found.getLat(), is(request.getLat()));
        assertThat(found.getLon(), is(request.getLon()));
        assertThat(found.getCity(), is(request.getCity()));
        assertThat(found.getAddress(), is(request.getAddress()));
    }

    @Test
    public void shouldUpdateLocalStore() throws Exception {
        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .state(StoreState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(10.0)
                .lon(10.0)
                .city(String.valueOf(UUID.randomUUID()))
                .address(String.valueOf(UUID.randomUUID()))
                .build());

        StoreDTO request = StoreDTO.builder()
                .partner(PartnerDTO.builder()
                        .id(partner.getId())
                        .company(CompanyDTO.builder()
                                .id(company.getId())
                                .build())
                        .build())
                .id(store.getId())
                .state(StoreState.INACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .city(String.valueOf(UUID.randomUUID()))
                .address(String.valueOf(UUID.randomUUID()))
                .build();

        StoreDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .put(API_PATH)
                .then().log().all()
                .statusCode(200)
                .extract().as(StoreDTO.class);

        assertThat(response.getSource(), is(Source.LOCAL));
        assertThat(response.getState(), is(request.getState()));
        assertThat(response.getName(), is(request.getName()));
        assertThat(response.getLat(), is(request.getLat()));
        assertThat(response.getLon(), is(request.getLon()));
        assertThat(response.getCity(), is(request.getCity()));
        assertThat(response.getAddress(), is(request.getAddress()));

        Store found = storeRepository.findById(response.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getSource(), is(Source.LOCAL));
        assertThat(found.getState(), is(request.getState()));
        assertThat(found.getName(), is(request.getName()));
        assertThat(found.getLat(), is(request.getLat()));
        assertThat(found.getLon(), is(request.getLon()));
        assertThat(found.getCity(), is(request.getCity()));
        assertThat(found.getAddress(), is(request.getAddress()));
    }

    @Test
    public void apiDelete() throws Exception {
        Store store = storeRepository.save(arbitraryStoreForPartner(partner));

        UUID id = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .delete(API_PATH + "/{id}", store.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(UUID.class);

        Store found = storeRepository.findById(id)
                .orElse(null);
        assertThat(found, is(nullValue()));
    }

    private static Store arbitraryStoreForPartner(Partner partner) {
        return Store.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .partner(partner)
                .build();
    }

    private static StoreDTO arbitraryRequestStoreForPartner(UUID companyId, UUID partnerId, UUID id) {
        CompanyDTO requestCompany = new CompanyDTO();
        requestCompany.setId(companyId);

        PartnerDTO requestPartner = new PartnerDTO();
        requestPartner.setId(partnerId);
        requestPartner.setCompany(requestCompany);

        StoreDTO result = new StoreDTO();
        result.setId(id);
        result.setState(StoreState.ACTIVE);
        result.setName(String.valueOf(UUID.randomUUID()));
        result.setLat(42.0);
        result.setLon(42.0);
        result.setRadius(42L);
        result.setPartner(requestPartner);

        return result;
    }
}
