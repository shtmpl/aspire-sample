package me.sample;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.mapper.TypeRef;
import me.sample.repository.CompanyAuthorityRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.NotificationRepository;
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
import me.sample.dto.NotificationDTO;
import me.sample.dto.NotificationSearchDTO;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Notification;
import me.sample.domain.Permission;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalPlatform;
import me.sample.repository.ApplicationRepository;
import me.sample.service.SecurityService;
import me.sample.web.rest.request.NotificationSendRequest;
import me.sample.web.rest.response.NotificationSendResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@Import(NoSecurityConfiguration.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class NotificationRestIntegrationTest {

    private static final String API_PATH = "/api/notifications";

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
    private NotificationRepository notificationRepository;

    @Before
    public void setUp() throws Exception {
        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);
    }

    @Test
    public void shouldIndexNotifications() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Notification notification = notificationRepository.save(Notification.builder()
                .terminal(terminal)
                .build());

        List<NotificationDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH)
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<NotificationDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(NotificationDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, hasItem(notification.getId()));
    }

    @Test
    public void shouldSearchNotifications() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Notification notification = notificationRepository.save(Notification.builder()
                .terminal(terminal)
                .build());

        NotificationSearchDTO request = NotificationSearchDTO.builder()
                .terminalIds(Stream.of(terminal.getId()).collect(Collectors.toList()))
                .build();

        List<NotificationDTO> response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/search")
                .then().log().all()
                .statusCode(200)
                .extract().as(new TypeRef<List<NotificationDTO>>() {
                });

        Set<UUID> responseIds = response.stream()
                .map(NotificationDTO::getId)
                .collect(Collectors.toSet());

        assertThat(responseIds.size(), is(response.size()));
        assertThat(responseIds, containsInAnyOrder(notification.getId()));
    }

    @Test
    public void shouldShowNotification() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .build());

        companyAuthorityRepository.save(CompanyAuthority.builder()
                .userId(USER_ID)
                .company(company)
                .permission(Permission.READ)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());

        Notification notification = notificationRepository.save(Notification.builder()
                .terminal(terminal)
                .build());

        NotificationDTO result = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .when().log().all()
                .get(API_PATH + "/{id}", notification.getId())
                .then().log().all()
                .statusCode(200)
                .extract().as(NotificationDTO.class);

        assertThat(result, is(notNullValue()));
        assertThat(result.getId(), is(notification.getId()));
    }

    @Test
    public void shouldSendNotification() throws Exception {
        NotificationSendRequest request = NotificationSendRequest.builder()
                .terminalPlatform(TerminalPlatform.IOS)
                .terminalPushId("42")
                .body("Text")
                .build();

        NotificationSendResponse response = RestAssured.given()
                .baseUri("http://localhost:" + port)
                .contentType(ContentType.JSON)
                .body(request)
                .when().log().all()
                .post(API_PATH + "/send")
                .then().log().all()
                .statusCode(200)
                .extract().as(NotificationSendResponse.class);
    }
}
