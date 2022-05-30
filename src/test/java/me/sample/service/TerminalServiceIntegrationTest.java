package me.sample.service;

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
import me.sample.dto.TerminalDTO;
import me.sample.gateway.dadata.DadataGateway;
import me.sample.domain.Application;
import me.sample.domain.Company;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.Specifications;
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;
import me.sample.repository.GeoPositionInfoRepository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TerminalServiceIntegrationTest {

    @Autowired
    private TerminalService terminalService;

    @MockBean
    private DadataGateway dadataGateway;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Before
    public void setUp() throws Exception {
        terminalRepository.deleteAll();

        Mockito.when(dadataGateway.findCityByIp(Mockito.anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void shouldCountTerminals() throws Exception {
        terminalRepository.save(Terminal.builder()
                .build());

        long result = terminalService.countTerminals(Specifications.any());

        assertThat(result, is(1L));
    }

    @Test
    public void shouldFindCachedTerminalByHardwareIdAndApplicationApiKey() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .hardwareId(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal found = terminalService.findTerminal(terminal.getHardwareId(), application.getApiKey())
                .orElseThrow(AssertionError::new);

        IntStream.range(0, 4).forEach((int x) ->
                terminalService.findTerminal(terminal.getHardwareId(), application.getApiKey())
                        .orElseThrow(AssertionError::new));

        assertThat(found.getId(), is(terminal.getId()));
    }

    @Test
    public void shouldSaveOrUpdateTerminal() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        String applicationApiKey = String.valueOf(UUID.randomUUID());
        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(applicationApiKey)
                .build());

        String terminalHardwareId = String.valueOf(UUID.randomUUID());
        Terminal terminal = terminalService.saveOrUpdateTerminal(TerminalDTO.builder()
                .appBundle(applicationApiKey)
                .hardwareId(terminalHardwareId)
                .ip("127.0.0.1")
                .prop(Terminal.PROP_KEY_BIRTH_DATE, 1492)
                .build());

        terminalService.saveOrUpdateTerminal(TerminalDTO.builder()
                .appBundle(applicationApiKey)
                .hardwareId(terminalHardwareId)
                .ip("127.0.0.2")
                .prop(Terminal.PROP_KEY_BIRTH_DATE, "Invalid")
                .prop(Terminal.PROP_KEY_CLIENT_ID, null)
                .build());


        Terminal result = terminalRepository.findById(terminal.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getIp(), is("127.0.0.2"));
        assertThat(result.getProp(Terminal.PROP_KEY_BIRTH_DATE), is(1492));
    }

    @Test
    public void shouldUpdateTerminalCityByIp() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .hardwareId(String.valueOf(UUID.randomUUID()))
                .build());


        Mockito.when(dadataGateway.findCityByIp(Mockito.anyString()))
                .thenReturn(Optional.of("X"));


        terminalService.updateTerminalCityByIp(terminal, "127.0.0.1");


        Terminal result = terminalRepository.findById(terminal.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getCity(), is("X"));
        assertThat(result.getLastLocationUpdate(), is(notNullValue()));


        terminalService.updateTerminalCityByIp(terminal, "127.0.0.2");
    }

    @Test
    public void shouldSyncTerminalCityByIp() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .hardwareId(String.valueOf(UUID.randomUUID()))
                .ip("127.0.0.1")
                .build());


        Mockito.when(dadataGateway.findCityByIp(Mockito.anyString()))
                .thenReturn(Optional.of("X"));


        terminalService.syncTerminalCity(terminal);


        Terminal result = terminalRepository.findById(terminal.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getCity(), is("X"));
    }

    @Test
    public void shouldSyncTerminalCityByIpThenByLastKnownGeoposition() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .application(application)
                .hardwareId(String.valueOf(UUID.randomUUID()))
                .ip("127.0.0.1")
                .build());

        geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .build());


        Mockito.when(dadataGateway.findCityByIp(Mockito.anyString()))
                .thenReturn(Optional.empty());

        Mockito.when(dadataGateway.findCityByCoordinates(Mockito.anyDouble(), Mockito.anyDouble()))
                .thenReturn(Optional.of("X"));


        terminalService.syncTerminalCity(terminal);


        Terminal result = terminalRepository.findById(terminal.getId())
                .orElseThrow(AssertionError::new);

        assertThat(result.getCity(), is("X"));
    }
}
