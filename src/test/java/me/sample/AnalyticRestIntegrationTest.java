package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.sample.enumeration.TimePeriod;
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
import me.sample.domain.TerminalPlatform;
import me.sample.domain.AnalyticReport;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Permission;
import me.sample.domain.PropFilter;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
import me.sample.repository.AnalyticReportRepository;
import me.sample.service.AnalyticService;
import me.sample.service.SecurityService;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AnalyticRestIntegrationTest {

    private static final String API_PATH = "/api/analytic";

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
    private AnalyticReportRepository analyticReportRepository;

    @Before
    public void setUp() throws Exception {
        terminalRepository.deleteAll();

        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);
    }

    @Test
    public void shouldShowTerminalCount() throws Exception {
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
                .pushId(String.valueOf(UUID.randomUUID()))
                .build());


        List<PropFilter> request = Collections.emptyList();

        Long response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/auditory")
                .then().log().all()
                .statusCode(200)
                .extract().as(Long.class);


        assertThat(response, is(1L));
    }

    @Test
    public void shouldShowTerminalCountForFilters() throws Exception {
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
                .platform(TerminalPlatform.IOS)
                .pushId(String.valueOf(UUID.randomUUID()))
                .city("г. Санкт-Петербург")
                .build());


        List<PropFilter> request = Stream.of(
                PropFilter.builder()
                        .sign(PropFilter.Sign.EQUAL)
                        .name("platform")
                        .value("IOS")
                        .build(),
                PropFilter.builder()
                        .sign(PropFilter.Sign.EQUAL)
                        .name("city")
                        .value("петер")
                        .build())
                .collect(Collectors.toList());

        Long response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/auditory")
                .then().log().all()
                .statusCode(200)
                .extract().as(Long.class);


        assertThat(response, is(1L));
    }

    @Test
    public void shouldShowAgeAnalyticReport() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        AnalyticReport analyticReport = analyticReportRepository.save(AnalyticReport.builder()
                .companyId(company.getId())
                .name(AnalyticService.REPORT_NAME_AGE)
                .timePeriod(TimePeriod.ALL_TIME)
                .build());


        AnalyticReport response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/age?timePeriod=ALL_TIME")
                .then().log().all()
                .statusCode(200)
                .extract().as(AnalyticReport.class);


        assertThat(response.getId(), is(analyticReport.getId()));
    }

    @Test
    public void shouldShowCityAnalyticReport() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        AnalyticReport analyticReport = analyticReportRepository.save(AnalyticReport.builder()
                .companyId(company.getId())
                .name(AnalyticService.REPORT_NAME_CITY)
                .timePeriod(TimePeriod.ALL_TIME)
                .build());


        AnalyticReport response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/city?timePeriod=ALL_TIME")
                .then().log().all()
                .statusCode(200)
                .extract().as(AnalyticReport.class);


        assertThat(response.getId(), is(analyticReport.getId()));
    }

    @Test
    public void shouldShowVendorAnalyticReport() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ_WRITE)
                .build());

        AnalyticReport analyticReport = analyticReportRepository.save(AnalyticReport.builder()
                .companyId(company.getId())
                .name(AnalyticService.REPORT_NAME_VENDOR)
                .timePeriod(TimePeriod.ALL_TIME)
                .build());


        AnalyticReport response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/vendor?timePeriod=ALL_TIME")
                .then().log().all()
                .statusCode(200)
                .extract().as(AnalyticReport.class);


        assertThat(response.getId(), is(analyticReport.getId()));
    }
}
