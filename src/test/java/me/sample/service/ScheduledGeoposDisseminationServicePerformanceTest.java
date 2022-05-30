package me.sample.service;

import me.sample.repository.CampaignRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.NotificationTemplateRepository;
import me.sample.repository.PartnerRepository;
import me.sample.repository.ScheduledGeoposDisseminationRepository;
import me.sample.repository.StoreRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Application;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.Company;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Partner;
import me.sample.domain.ScheduledGeoposDissemination;
import me.sample.domain.Store;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
import me.sample.repository.GeoPositionInfoRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ScheduledGeoposDisseminationServicePerformanceTest {

    @Autowired
    private ScheduledGeoposDisseminationService scheduledGeoposDisseminationService;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private ScheduledGeoposDisseminationRepository scheduledGeoposDisseminationRepository;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    private AtomicLong notificationCount;

    @Before
    public void setUp() throws Exception {
        notificationCount = new AtomicLong(0);

        NotificationLimitValidationResult successfulNotificationLimitValidation = NotificationLimitValidationResult.builder()
                .failed(false)
                .limit(null)
                .count(0L)
                .build();
        Mockito.doReturn(successfulNotificationLimitValidation)
                .when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class));
        Mockito.doReturn(successfulNotificationLimitValidation)
                .when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class), Mockito.any(Terminal.class));
        Mockito.doReturn(successfulNotificationLimitValidation)
                .when(notificationService).validateNotificationLimit(Mockito.any(Terminal.class));

        Mockito.doAnswer((InvocationOnMock invocation) -> {
            notificationCount.incrementAndGet();

            return null;
        }).when(notificationService).sendNotification(Mockito.any(Terminal.class), Mockito.any(Campaign.class));
    }

    @Test
    public void shouldFindDisseminationForTerminal() throws Exception {
        System.out.println("Saving company...");
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        System.out.println("Saving partners...");
        List<Partner> partners = partnerRepository.saveAll(IntStream.range(0, 10)
                .mapToObj((int x) -> Partner.builder()
                        .company(company)
                        .name(String.valueOf(UUID.randomUUID()))
                        .build())
                .collect(Collectors.toList()));

        List<Store> stores = partners.stream()
                .map((Partner partner) -> {
                    System.out.printf("Saving stores for partner %s%n", partner.getId());
                    return storeRepository.saveAll(IntStream.range(0, 10)
                            .mapToObj((int x) -> Store.builder()
                                    .partner(partner)
                                    .name(String.valueOf(UUID.randomUUID()))
                                    .lat(42.0)
                                    .lon(42.0)
                                    .build())
                            .collect(Collectors.toList()));
                }).flatMap(Collection::stream)
                .collect(Collectors.toList());

        System.out.println("Saving dissemination...");
        LocalDateTime now = LocalDateTime.now();
        ScheduledGeoposDissemination dissemination = scheduledGeoposDisseminationRepository.save(ScheduledGeoposDissemination.builder()
                .start(now.minusDays(1))
                .end(now.plusDays(1))
                .partners(new HashSet<>(partners))
                .stores(new HashSet<>(stores))
                .build());

        System.out.println("Saving campaign...");
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(1000L)
                .scheduledGeoposDissemination(dissemination)
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .apiKey(String.valueOf(UUID.randomUUID()))
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .build());


        System.out.println("Querying...");
        long time = System.nanoTime();
        ScheduledGeoposDissemination result = scheduledGeoposDisseminationService.findPrioritisedDisseminationForTerminal(terminal, 42.0, 42.0)
                .orElseThrow(AssertionError::new);


        System.out.printf("Found: %s%n", result.getId());
        System.out.printf("Elapsed time: %s%n", Duration.of(System.nanoTime() - time, ChronoUnit.NANOS));
    }

    @Test
    public void shouldExecuteDissemination() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Company company = companyRepository.findById(UUID.fromString("28d95f8c-317c-4a7b-8ea8-c4d94353270a"))
                .orElseThrow(AssertionError::new);

        Partner partner = partnerRepository.findById(UUID.fromString("0704e482-8456-49de-8675-802406051c2c"))
                .orElseThrow(AssertionError::new);

        NotificationTemplate notificationTemplate = notificationTemplateRepository.findById(UUID.fromString("1f6c2c79-af74-4a23-9246-5a261a2495e7"))
                .orElseThrow(AssertionError::new);

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .notificationTemplate(notificationTemplate)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(2000L)
                .scheduledGeoposDissemination(scheduledGeoposDisseminationRepository.save(ScheduledGeoposDissemination.builder()
                        .start(now)
                        .end(now.plusDays(1))
                        .partners(Stream.of(partner).collect(Collectors.toSet()))
                        .build()))
                .build());

        UUID disseminationId = campaign.getScheduledGeoposDissemination().getId();


        System.out.println("Executing dissemination...");
        long time = System.nanoTime();
        scheduledGeoposDisseminationService.executeDissemination(disseminationId);


        System.out.printf("Executed dissemination. Elapsed time: %s%n", Duration.of(System.nanoTime() - time, ChronoUnit.NANOS));
        System.out.printf("Notification count: %s%n", notificationCount);
    }

    @Test
    public void shouldDoStuff() throws Exception {
//        System.out.println("Creating terminals...");
//        List<Terminal> terminals = IntStream.range(0, 1000000)
//                .mapToObj((int x) -> Terminal.builder()
//                        .build())
//                .collect(Collectors.toList());
//
//        System.out.println("Saving terminals...");
//        terminalRepository.saveAll(terminals);

        System.out.println("Creating events...");
        List<GeoPositionInfo> events = terminalRepository.findAll().stream()
                .map((Terminal terminal) -> GeoPositionInfo.builder()
                                .terminal(terminal)
                                .lat(55.8748)
                                .lon(37.7085)
                                .build())
                .collect(Collectors.toList());

        System.out.println("Saving events...");
        geoPositionInfoRepository.saveAll(events);

        System.out.println("Done!");
    }
}
