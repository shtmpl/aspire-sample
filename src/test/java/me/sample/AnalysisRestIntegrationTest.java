package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.ClusterRepository;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.PartnerRepository;
import me.sample.repository.StoreRepository;
import me.sample.repository.TerminalRepository;
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
import me.sample.dto.ClusterDTO;
import me.sample.dto.AnalysisDTO;
import me.sample.dto.StoreAnalysisCountDTO;
import me.sample.dto.StoreAnalysisDTO;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Partner;
import me.sample.domain.Permission;
import me.sample.domain.Store;
import me.sample.domain.Terminal;
import me.sample.domain.geo.Cluster;
import me.sample.repository.ApplicationRepository;
import me.sample.service.SecurityService;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnalysisRestIntegrationTest {

    private static final String API_PATH = "/api/analysis";

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
    private ClusterRepository clusterRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Before
    public void setUp() throws Exception {
        clusterRepository.deleteAll();

        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);
    }

    @Test
    public void shouldCountIndexedGeopositionClusters() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .terminal(terminal)
                .visitCount(4)
                .build());


        Long response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/terminal/geoposition-cluster/count")
                .then().log().all()
                .statusCode(200)
                .extract().as(Long.class);


        assertThat(response, is(1L));
    }

    @Test
    public void shouldIndexGeopositionClustersForTerminal() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .terminal(terminal)
                .visitCount(4)
                .build());


        List<ClusterDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/terminal/{terminalId}/geoposition-cluster", terminal.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<ClusterDTO>>() {
                });


        List<UUID> responseClusterIds = response.stream()
                .map(ClusterDTO::getId)
                .collect(Collectors.toList());

        assertThat(responseClusterIds, contains(cluster.getId()));
    }

    @Test
    public void shouldAnalyzeClusters() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());


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


        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .visitCount(4)
                .build());


        AnalysisDTO request = AnalysisDTO.builder()
                .radius(0L)
                .storePartnerId(partner.getId())
                .build();

        List<ClusterDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/cluster")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<ClusterDTO>>() {
                });

        assertThat(response.stream()
                        .map(ClusterDTO::getId)
                        .collect(Collectors.toList()),
                contains(cluster.getId()));
    }

    @Test
    public void shouldCountAnalyzedStores() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());


        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .city("45 км Автодороги Санкт-Петербург-Псков")
                .build());


        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .visitCount(4)
                .build());


        AnalysisDTO request = AnalysisDTO.builder()
                .radius(0L)
                .storePartnerId(partner.getId())
                .storeCity("Санкт-Петербург")
                .build();

        StoreAnalysisCountDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/store/count")
                .then().log().all()
                .statusCode(200)
                .extract().as(StoreAnalysisCountDTO.class);


        assertThat(response.getStoreCount(), is(1L));

        assertThat(response.getUniqueClusterCount(), is(1L));

        assertThat(response.getUniqueClusterAcceptedGeopositionCount(), is(0L));

        assertThat(response.getUniqueTerminalCount(), is(1L));
    }

    @Test
    public void shouldAnalyzeStores() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());


        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .city("45 км Автодороги Санкт-Петербург-Псков")
                .build());


        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .visitCount(4)
                .build());


        AnalysisDTO request = AnalysisDTO.builder()
                .radius(0L)
                .storePartnerId(partner.getId())
                .storeCity("Санкт-Петербург")
                .build();

        List<StoreAnalysisDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/store")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<StoreAnalysisDTO>>() {
                });


        assertThat(response.size(), is(1));

        assertThat(response.get(0).getStore().getId(),
                is(store.getId()));

        assertThat(response.get(0).getClusters(),
                is(nullValue()));

        assertThat(response.get(0).getClusterCount(), is(1L));
        assertThat(response.get(0).getClusterAcceptedGeopositionCount(), is(0L));
        assertThat(response.get(0).getTerminalCount(), is(1L));
    }

    @Test
    public void shouldAnalyzeStore() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());


        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .city("X")
                .build());


        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .visitCount(4)
                .build());


        AnalysisDTO request = AnalysisDTO.builder()
                .radius(0L)
                .storePartnerId(partner.getId())
                .storeCity("X")
                .build();

        StoreAnalysisDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/store/{id}", store.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(StoreAnalysisDTO.class);


        assertThat(response.getStore().getId(),
                is(store.getId()));

        assertThat(response.getClusters().stream()
                        .map(ClusterDTO::getId)
                        .collect(Collectors.toList()),
                contains(cluster.getId()));
    }
}
