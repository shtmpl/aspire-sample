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
import me.sample.domain.Terminal;
import me.sample.repository.ApplicationRepository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "logging.level.me.sample=INFO"
})
public class TerminalServicePerformanceTest {

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

    @Before
    public void setUp() throws Exception {
//        terminalRepository.deleteAll();

        Mockito.when(dadataGateway.findCityByIp(Mockito.anyString()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void shouldSaveOrUpdateTerminal() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        TerminalDTO request = TerminalDTO.builder()
                .appBundle(application.getApiKey())
                .hardwareId(String.valueOf(UUID.randomUUID()))
                .ip("127.0.0.1")
                .build();


        Terminal terminal = terminalService.saveOrUpdateTerminal(request);

        long time = System.nanoTime();
        IntStream.range(0, 10).forEach((int x) -> {
            terminalService.saveOrUpdateTerminal(request
                    .setIp("127.0.0.2")
            );
        });

        System.out.printf("Elapsed time: %s%n", Duration.ofNanos(System.nanoTime() - time));

        Terminal found = terminalRepository.findById(terminal.getId())
                .orElseThrow(AssertionError::new);

        assertThat(found.getIp(), is("127.0.0.2"));
    }

    @Test
    public void shouldSyncTerminalCities() throws Exception {
        Company company = companyRepository.save(Company.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        Application application = applicationRepository.save(Application.builder()
                .company(company)
                .name(String.valueOf(UUID.randomUUID()))
                .apiKey(String.valueOf(UUID.randomUUID()))
                .build());

        for (int batch = 0; batch < 10; batch += 1) {
            List<Terminal> terminals = IntStream.range(0, 100000)
                    .mapToObj((int x) -> Terminal.builder()
                            .application(application)
                            .hardwareId(String.valueOf(UUID.randomUUID()))
                            .ip("127.0.0.1")
                            .build())
                    .collect(Collectors.toList());

            System.out.println("Saving terminals...");
            terminalRepository.saveAll(terminals);
        }


        Mockito.when(dadataGateway.findCityByIp(Mockito.anyString()))
                .thenReturn(Optional.of("X"));

        System.out.println("Sync'ing terminal cities...");
        long time = System.nanoTime();
        terminalService.syncTerminalCities();

        System.out.printf("Elapsed time: %s%n", Duration.ofNanos(System.nanoTime() - time));
    }
}
