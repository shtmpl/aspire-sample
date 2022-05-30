package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.Terminal;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeoPositionInfoRepositoryIntegrationTest {

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Before
    public void setUp() throws Exception {
        geoPositionInfoRepository.deleteAll();
    }

    @Transactional
    @Test
    public void shouldFindEventsByGeopositionWithinRadius() throws Exception {
        List<GeoPositionInfo> events = IntStream.range(0, 4)
                .mapToObj((int x) ->
                        geoPositionInfoRepository.save(GeoPositionInfo.builder()
                                .lat(42.0 + x / 100.0)
                                .lon(42.0 + x / 100.0)
                                .build()))
                .collect(Collectors.toList());

        Set<GeoPositionInfo> results;
        try (Stream<GeoPositionInfo> found = geoPositionInfoRepository.findAsStreamAllByGeopositionWithinRadius(42.0, 42.0, 2000L)) {
            results = found.collect(Collectors.toSet());
        }

        assertThat(results, hasItems(events.get(0), events.get(1)));
    }

    @Test
    public void shouldFindNonClusteredGeopositionsForTerminal() throws Exception {
        Terminal terminal = terminalRepository.save(Terminal.builder()
                .build());

        GeoPositionInfo geoposition = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .build());

        List<GeoPositionInfo> results = geoPositionInfoRepository.findAllByTerminalIdAndClusteredFalse(terminal.getId(), 100);

        assertThat(results, containsInAnyOrder(geoposition));
    }

    @Test
    public void shouldFindTerminalIdsForNonClusteredGeopositions() throws Exception {
        Terminal terminal = terminalRepository.save(Terminal.builder()
                .build());

        GeoPositionInfo geoposition = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .terminal(terminal)
                .lat(42.0)
                .lon(42.0)
                .build());

        Set<UUID> results = geoPositionInfoRepository.findDistinctTerminalIdsByClusteredFalse();

        assertThat(results, containsInAnyOrder(terminal.getId()));
    }

    @Test
    public void shouldSaveGeopositionsWithAssignedTimestamp() throws Exception {
        LocalDateTime epoch = LocalDateTime.ofInstant(Instant.EPOCH, ZoneId.systemDefault());

        GeoPositionInfo geoposition = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .createdDate(epoch)
                .build());

        System.out.printf("Assigned time: %s. Saved w/ createdDate: %s%n", epoch, geoposition.getCreatedDate());

        assertThat(geoposition.getCreatedDate(), is(epoch));
    }

    @Test
    public void shouldSaveGeopositionsWithGeneratedTimestamp() throws Exception {
        LocalDateTime now = LocalDateTime.now();

        GeoPositionInfo geoposition = geoPositionInfoRepository.save(GeoPositionInfo.builder()
                .build());

        System.out.printf("Saved w/ createdDate: %s%n", geoposition.getCreatedDate());

        assertThat(geoposition.getCreatedDate(), is(greaterThanOrEqualTo(now)));
    }
}
