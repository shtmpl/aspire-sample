package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.config.NoSecurityConfiguration;
import me.sample.dto.GeoPositionInfoDTO;
import me.sample.dto.PushTokenDTO;
import me.sample.domain.TerminalPlatform;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
import me.sample.service.LocationConsumerService;

import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Интеграционные тесты эндпоинтов, доступных для клиентского приложения
 */
@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TerminalApiIntegrationTest {

    private static final String PATH_API_TERMINAL = "/api/terminal";

    @LocalServerPort
    private int port;

    @MockBean
    private LocationConsumerService locationConsumerService;

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
    }

    @Test
    public void shouldUpdateTerminalByHardwareIdAndApplicationApiKey() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        String applicationApiKey = String.valueOf(UUID.randomUUID());
        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(applicationApiKey)
                .build());

        String terminalHardwareId = String.valueOf(UUID.randomUUID());
        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .hardwareId(terminalHardwareId)
                .build());


        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .headers("hwid", terminalHardwareId, "appBundle", applicationApiKey, "platform", TerminalPlatform.ANDROID)
                .body(GeoPositionInfoDTO.builder()
                        .lat(42.0)
                        .lon(42.0)
                        .build())
                .when().log().all()
                .post(PATH_API_TERMINAL + "/location")
                .then().log().all()
                .statusCode(200);

        assertThat(terminalRepository.findAll().stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal.getId()));


        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .headers("hwid", terminalHardwareId, "appBundle", applicationApiKey, "platform", TerminalPlatform.ANDROID)
                .body(PushTokenDTO.builder()
                        .token(String.valueOf(UUID.randomUUID()))
                        .build())
                .when().log().all()
                .post(PATH_API_TERMINAL + "/pushToken")
                .then().log().all()
                .statusCode(200);

        assertThat(terminalRepository.findAll().stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal.getId()));


        RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .headers("hwid", terminalHardwareId, "appBundle", applicationApiKey, "platform", TerminalPlatform.ANDROID)
                .body(Collections.emptyMap())
                .when().log().all()
                .post(PATH_API_TERMINAL + "/setProps")
                .then().log().all()
                .statusCode(200);

        assertThat(terminalRepository.findAll().stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal.getId()));
    }
}
