package me.sample.service;

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
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.AnalyticReport;
import me.sample.domain.Application;
import me.sample.domain.CityPercentage;
import me.sample.domain.Company;
import me.sample.domain.CompanyAuthority;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
import me.sample.repository.AnalyticReportRepository;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AnalyticServiceIntegrationTest {

    private static final Long USER_ID = 42L;


    @MockBean
    private SecurityService securityService;

    @MockBean
    private CompanyAuthorityRepository companyAuthorityRepository;

    @Autowired
    private AnalyticService analyticService;

    @Autowired
    private AnalyticReportRepository analyticReportRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Before
    public void setUp() throws Exception {
        analyticReportRepository.deleteAll();


    }

    @Test
    public void shouldFindAnalyticReport() throws Exception {
        Company company1 = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        analyticReportRepository.save(AnalyticReport.builder()
                .companyId(company1.getId())
                .name(AnalyticService.REPORT_NAME_CITY)
                .timePeriod(TimePeriod.ALL_TIME)
                .data(Arrays.asList(
                        CityPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .city("A")
                                .count(1L)
                                .percentage(0.10)
                                .build(),
                        CityPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .city("B")
                                .count(2L)
                                .percentage(0.20)
                                .build()))
                .build());


        Company company2 = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        analyticReportRepository.save(AnalyticReport.builder()
                .companyId(company2.getId())
                .name(AnalyticService.REPORT_NAME_CITY)
                .timePeriod(TimePeriod.ALL_TIME)
                .data(Arrays.asList(
                        CityPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .city("A")
                                .count(1L)
                                .percentage(0.10)
                                .build(),
                        CityPercentage.builder()
                                .id(String.valueOf(UUID.randomUUID()))
                                .city("B")
                                .count(2L)
                                .percentage(0.20)
                                .build()))
                .build());


        Mockito.when(securityService.getUserId())
                .thenReturn(USER_ID);

        Mockito.when(companyAuthorityRepository.findAllByUserId(USER_ID))
                .thenReturn(Arrays.asList(
                        CompanyAuthority.builder()
                                .company(company1)
                                .build(),
                        CompanyAuthority.builder()
                                .company(company2)
                                .build()));


        AnalyticReport result = analyticService.findAnalyticReport(AnalyticService.REPORT_NAME_CITY, TimePeriod.ALL_TIME)
                .orElseThrow(AssertionError::new);

        System.out.println(result.getData());
    }

    @Test
    public void shouldMergeAnalyticReports() throws Exception {
        Map<String, Object> analyticReportDataUnit1 = new LinkedHashMap<>();
        analyticReportDataUnit1.put("id", String.valueOf(UUID.randomUUID()));
        analyticReportDataUnit1.put("city", "A");
        analyticReportDataUnit1.put("count", 1);
        analyticReportDataUnit1.put("percentage", 0.10);

        Map<String, Object> analyticReportDataUnit2 = new LinkedHashMap<>();
        analyticReportDataUnit2.put("id", String.valueOf(UUID.randomUUID()));
        analyticReportDataUnit2.put("city", "B");
        analyticReportDataUnit2.put("count", 2);
        analyticReportDataUnit2.put("percentage", 0.20);

        List<Map<String, Object>> analyticReportData1 = Arrays.asList(
                analyticReportDataUnit1,
                analyticReportDataUnit2);

        List<Map<String, Object>> analyticReportData2 = Arrays.asList(
                analyticReportDataUnit1,
                analyticReportDataUnit2);


        List<Map<String, Object>> result = analyticService.mergeAnalyticReportData(
                AnalyticService.REPORT_NAME_CITY,
                Arrays.asList(
                        analyticReportData1,
                        analyticReportData2));


        System.out.println(result);
    }

    @Test
    public void shouldGenerateAndSaveAnalyticReports() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Stream.concat(
                Stream.of(null, "", "    "),
                Stream.of("A"))
                .map((String city) -> Terminal.builder()
                        .application(application)
                        .city(city)
                        .build())
                .forEach(terminalRepository::save);


        analyticService.generateAndSaveAnalyticReports();
    }
}
