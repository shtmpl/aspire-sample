package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.Campaign;
import me.sample.domain.CampaignClient;
import me.sample.domain.Terminal;
import me.sample.domain.TerminalPlatform;
import me.sample.domain.TerminalSpecifications;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TerminalRepositoryIntegrationTest {

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private CampaignClientRepository campaignClientRepository;

    @Before
    public void setUp() throws Exception {
        terminalRepository.deleteAll();
    }

    @Test
    public void shouldCountTestTerminals() throws Exception {
        Terminal terminal = terminalRepository.save(Terminal.builder()
                .test(true)
                .build());

        long result = terminalRepository.countByTest(true);

        assertThat(result, is(1L));
    }

    @Test
    public void shouldFindTerminalsByPushIdSpecification() throws Exception {
        Terminal terminal1 = terminalRepository.save(Terminal.builder()
                .pushId(null)
                .build());

        Terminal terminal2 = terminalRepository.save(Terminal.builder()
                .pushId(String.valueOf(UUID.randomUUID()))
                .build());


        assertThat(terminalRepository.findAll(TerminalSpecifications.pushIdIsNull()).stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal1.getId()));

        assertThat(terminalRepository.findAll(Specification.not(TerminalSpecifications.pushIdIsNull())).stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal2.getId()));
    }

    @Test
    public void shouldFindTerminalsByPlatformSpecification() throws Exception {
        Terminal terminal1 = terminalRepository.save(Terminal.builder()
                .platform(TerminalPlatform.ANDROID)
                .build());

        Terminal terminal2 = terminalRepository.save(Terminal.builder()
                .platform(TerminalPlatform.IOS)
                .build());


        assertThat(terminalRepository.findAll(TerminalSpecifications.platformEquals(TerminalPlatform.ANDROID))
                        .stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal1.getId()));

        assertThat(terminalRepository.findAll(TerminalSpecifications.platformEquals(TerminalPlatform.IOS))
                        .stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toSet()),
                containsInAnyOrder(terminal2.getId()));
    }

    @Test
    public void shouldFindTerminalsByClientIds() throws Exception {
        Campaign campaign = campaignRepository.save(Campaign.builder()
                .name(String.valueOf(UUID.randomUUID()))
                .build());

        CampaignClient client = campaignClientRepository.save(CampaignClient.builder()
                .campaign(campaign)
                .build()
                .setClientId("1"));

        Terminal terminal1 = terminalRepository.save(Terminal.builder()
                .build()
                .setProp(Terminal.PROP_KEY_CLIENT_ID, "1"));

        Terminal terminal2 = terminalRepository.save(Terminal.builder()
                .build()
                .setProp(Terminal.PROP_KEY_CLIENT_ID, "2"));

        assertThat(terminalRepository.findAll(
                TerminalSpecifications.existsCampaignClientForCampaignId(campaign.getId()))
                        .stream()
                        .map(Terminal::getId)
                        .collect(Collectors.toList()),
                containsInAnyOrder(terminal1.getId()));
    }
}
