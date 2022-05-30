package me.sample.service;

import me.sample.repository.CampaignRepository;
import me.sample.repository.CompanyRepository;
import me.sample.repository.DistributionRepository;
import me.sample.repository.NotificationRepository;
import me.sample.repository.NotificationTemplateRepository;
import me.sample.repository.PartnerRepository;
import me.sample.repository.StoreRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignState;
import me.sample.domain.Company;
import me.sample.domain.Distribution;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.NotificationLimitValidationResult;
import me.sample.domain.NotificationTemplate;
import me.sample.domain.Partner;
import me.sample.domain.Store;
import me.sample.domain.StoreState;
import me.sample.domain.Terminal;
import me.sample.repository.GeoPositionInfoRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributionServiceIntegrationTest {

    @MockBean
    private Scheduler scheduler;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private DistributionService distributionService;

    @Autowired
    private NotificationTemplateRepository notificationTemplateRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private DistributionRepository distributionRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Before
    public void setUp() throws Exception {
        storeRepository.deleteAll();

        geoPositionInfoRepository.deleteAll();
        notificationRepository.deleteAll();
        terminalRepository.deleteAll();

        distributionRepository.deleteAll();
        notificationTemplateRepository.deleteAll();

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
    }

    @Test
    public void shouldSaveDissemination() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Distribution dissemination = distributionService.saveDissemination(Distribution.builder()
                .start(now.toLocalDate())
                .end(now.plusDays(1).toLocalDate())
                .cron("* * * * * ?")
                .build());

        Distribution found = distributionRepository.findById(dissemination.getId())
                .orElseThrow(AssertionError::new);
    }

    @Test
    public void shouldStartDissemination() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Distribution dissemination = distributionService.saveDissemination(Distribution.builder()
                .start(now.toLocalDate())
                .end(now.plusDays(1).toLocalDate())
                .cron("* * * * * ?")
                .build());
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .distribution(dissemination)
                .build());
        distributionRepository.save(dissemination.setCampaign(campaign));


        distributionService.startDissemination(dissemination);


        Mockito.verify(scheduler).scheduleJob(Mockito.any(), Mockito.anySet(), Mockito.anyBoolean());
    }

    @Test
    public void shouldNotStartDisseminationWhenNotificationLimitIsExceeded() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Distribution dissemination = distributionService.saveDissemination(Distribution.builder()
                .start(now.toLocalDate())
                .end(now.plusDays(1).toLocalDate())
                .cron("* * * * * ?")
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(2000L)
                .notificationLimit(0L)
                .distribution(dissemination)
                .build());

        distributionRepository.save(dissemination.setCampaign(campaign));

        Mockito.doReturn(NotificationLimitValidationResult.builder()
                .failed(true)
                .reason("Test")
                .limit(0L)
                .count(0L)
                .build()).when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class));


        try {
            distributionService.startDissemination(dissemination);

            fail();
        } catch (UnsupportedOperationException expected) {
            System.out.printf("Thrown: %s%n", expected);
        }
    }

    @Test
    public void shouldExecuteDissemination() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .state(StoreState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .pushId(String.valueOf(UUID.randomUUID()))
                .build());

        geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .lat(store.getLat() + 0.01)
                .lon(store.getLon() + 0.01)
                .build());

        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .subject(String.valueOf(UUID.randomUUID()))
                .text(String.valueOf(UUID.randomUUID()))
                .customPushPartName(String.valueOf(UUID.randomUUID()))
                .customPushPartValue(String.valueOf(UUID.randomUUID()))
                .build());

        Distribution dissemination = distributionRepository.save(Distribution.builder()
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .notificationTemplate(notificationTemplate)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(2000L)
                .distribution(dissemination)
                .build());

        distributionRepository.save(dissemination.setCampaign(campaign));


        distributionService.executeDissemination(dissemination.getId());


        Mockito.verify(notificationService, Mockito.times(1))
                .sendNotification(Mockito.any(Terminal.class), Mockito.any(Campaign.class));
    }

    @Test
    public void shouldNotExecuteDisseminationWhenNotificationLimitIsExceeded() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .state(StoreState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .pushId(String.valueOf(UUID.randomUUID()))
                .build());

        geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .lat(store.getLat() + 0.01)
                .lon(store.getLon() + 0.01)
                .build());

        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .subject(String.valueOf(UUID.randomUUID()))
                .text(String.valueOf(UUID.randomUUID()))
                .customPushPartName(String.valueOf(UUID.randomUUID()))
                .customPushPartValue(String.valueOf(UUID.randomUUID()))
                .build());

        Distribution dissemination = distributionRepository.save(Distribution.builder()
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .notificationTemplate(notificationTemplate)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(2000L)
                .notificationLimit(1L)
                .distribution(dissemination)
                .build());

        distributionRepository.save(dissemination.setCampaign(campaign));


        Mockito.doReturn(NotificationLimitValidationResult.builder()
                .failed(true)
                .reason("Test")
                .limit(0L)
                .count(0L)
                .build()).when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class));


        distributionService.executeDissemination(dissemination.getId());


        Campaign found = campaignRepository.findById(campaign.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getState(), is(CampaignState.PAUSE));

        Mockito.verify(notificationService, Mockito.times(0))
                .sendNotification(Mockito.any(Terminal.class), Mockito.any(Campaign.class));
    }

    @Test
    public void shouldNotExecuteDisseminationWhenNotificationLimitPerTerminalIsExceeded() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Partner partner = partnerRepository.save(Partner.builder()
                .company(company)
                .build());

        Store store = storeRepository.save(Store.builder()
                .partner(partner)
                .state(StoreState.ACTIVE)
                .name(String.valueOf(UUID.randomUUID()))
                .lat(42.0)
                .lon(42.0)
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .pushId(String.valueOf(UUID.randomUUID()))
                .build());

        geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .lat(store.getLat() + 0.01)
                .lon(store.getLon() + 0.01)
                .build());

        NotificationTemplate notificationTemplate = notificationTemplateRepository.save(NotificationTemplate.builder()
                .subject(String.valueOf(UUID.randomUUID()))
                .text(String.valueOf(UUID.randomUUID()))
                .customPushPartName(String.valueOf(UUID.randomUUID()))
                .customPushPartValue(String.valueOf(UUID.randomUUID()))
                .build());

        Distribution dissemination = distributionRepository.save(Distribution.builder()
                .build());

        Campaign campaign = campaignRepository.save(Campaign.builder()
                .company(company)
                .notificationTemplate(notificationTemplate)
                .state(CampaignState.RUNNING)
                .name(String.valueOf(UUID.randomUUID()))
                .radius(2000L)
                .notificationLimitPerTerminal(1L)
                .distribution(dissemination)
                .build());

        distributionRepository.save(dissemination.setCampaign(campaign));


        Mockito.doReturn(NotificationLimitValidationResult.builder()
                .failed(true)
                .reason("Test")
                .limit(0L)
                .count(0L)
                .build()).when(notificationService).validateNotificationLimit(Mockito.any(Campaign.class), Mockito.any(Terminal.class));


        distributionService.executeDissemination(dissemination.getId());


        Mockito.verify(notificationService, Mockito.times(0))
                .sendNotification(Mockito.any(Terminal.class), Mockito.any(Campaign.class));
    }

    @Test
    public void shouldDeleteDissemination() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        Distribution dissemination = distributionService.saveDissemination(Distribution.builder()
                .start(now.toLocalDate())
                .end(now.plusDays(1).toLocalDate())
                .cron("* * * * * ?")
                .build());


        distributionService.deleteDissemination(dissemination.getId());


        Distribution found = distributionRepository.findById(dissemination.getId())
                .orElse(null);

        assertThat(found, is(nullValue()));


        Mockito.verify(scheduler).deleteJob(Mockito.any());
    }
}
