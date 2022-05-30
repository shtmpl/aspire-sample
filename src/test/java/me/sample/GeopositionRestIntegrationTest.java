package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
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
import me.sample.dto.GeoPositionInfoDTO;
import me.sample.dto.GeopositionSearchDTO;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.Permission;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
import me.sample.repository.GeoPositionInfoRepository;
import me.sample.service.SecurityService;

import java.util.List;
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
public class GeopositionRestIntegrationTest {

    private static final String API_PATH = "/api/geopositions";

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

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Before
    public void setUp() throws Exception {
        geoPositionInfoRepository.deleteAll();

        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);
    }

    @Test
    public void shouldCountIndexedGeopositions() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        GeoPositionInfo geoposition = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .build());


        Long response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/count")
                .then().log().all()
                .statusCode(200)
                .extract().as(Long.class);


        assertThat(response, is(1L));
    }

    @Test
    public void shouldIndexGeopositions() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        GeoPositionInfo geoPositionInfo = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .build());

        List<GeoPositionInfoDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<GeoPositionInfoDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(GeoPositionInfoDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(geoPositionInfo.getId()));
    }

    @Test
    public void shouldSearchGeopositions() throws Exception {
        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);

        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        GeoPositionInfo geoPositionInfo = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .build());

        List<GeoPositionInfoDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(GeopositionSearchDTO.builder()
                        .terminalIds(Stream.of(terminal.getId()).collect(Collectors.toList()))
                        .build())
                .when().log().all()
                .post(API_PATH + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<GeoPositionInfoDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(GeoPositionInfoDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, containsInAnyOrder(geoPositionInfo.getId()));
    }

    @Test
    public void shouldShowGeoposition() throws Exception {
        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);

        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        GeoPositionInfo geoPositionInfo = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .build());

        GeoPositionInfoDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/{id}", geoPositionInfo.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(GeoPositionInfoDTO.class);

        assertThat(response, is(notNullValue()));
        assertThat(response.getId(), is(geoPositionInfo.getId()));
    }
}
