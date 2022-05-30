package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Ignore;
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
import me.sample.dto.TerminalDTO;
import me.sample.dto.TerminalSearchDTO;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Permission;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
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
public class TerminalRestIntegrationTest {

    private static final String PATH_API_TERMINAL = "/api/terminals";

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

    @Before
    public void setUp() throws Exception {
        terminalRepository.deleteAll();

        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);
    }

    @Test
    public void shouldIndexTerminals() throws Exception {
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

        List<TerminalDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_TERMINAL)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<TerminalDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(TerminalDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(terminal.getId()));
    }

    @Test
    public void shouldSearchTerminals() throws Exception {
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
                .hardwareId(String.valueOf(UUID.randomUUID()))
                .vendor(String.valueOf(UUID.randomUUID()))
                .model(String.valueOf(UUID.randomUUID()))
                .osVersion(String.valueOf(UUID.randomUUID()))
                .build()
                .setProp(Terminal.PROP_KEY_CLIENT_ID, "x"));


        TerminalSearchDTO request = TerminalSearchDTO.builder()
                .id(terminal.getId())
                .hardwareId(terminal.getHardwareId())
                .vendor(terminal.getVendor())
                .model(terminal.getModel())
                .osVersion(terminal.getOsVersion())
                .clientId(String.valueOf(terminal.getProp(Terminal.PROP_KEY_CLIENT_ID)))
                .build();

        List<TerminalDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(PATH_API_TERMINAL + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<TerminalDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(TerminalDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds, containsInAnyOrder(terminal.getId()));
    }

    @Test
    public void shouldShowTerminal() throws Exception {
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

        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(PATH_API_TERMINAL + "/{id}", terminal.getId())
                .then().log().all()
                .statusCode(200);
    }

    @Ignore("N/A")
    @Test
    public void shouldAlterTerminalTest() throws Exception {
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
                .test(false)
                .build());

        TerminalDTO request = TerminalDTO.builder()
                .test(true)
                .build();

        TerminalDTO response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .patch(PATH_API_TERMINAL + "/{id}", terminal.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(TerminalDTO.class);

        assertThat(response.getTest(), is(request.getTest()));

        Terminal found = terminalRepository.findById(terminal.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getTest(), is(request.getTest()));
    }

}
