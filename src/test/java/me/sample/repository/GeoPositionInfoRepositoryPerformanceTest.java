package me.sample.repository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.Terminal;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterGeoposition;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeoPositionInfoRepositoryPerformanceTest {

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private ClusterGeopositionRepository clusterGeopositionRepository;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldFindNonClusteredGeopositionsForTerminal() throws Exception {
        LocalDate now = LocalDate.now();

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .build());

        Cluster cluster = clusterRepository.save(Cluster.builder()
                .build());

        for (int batch = 0; batch < 10; batch += 1) {
            List<GeoPositionInfo> geopositions = IntStream.range(0, 100000)
                    .mapToObj((int x) ->
                            GeoPositionInfo.builder()
                                    .terminal(terminal)
                                    .createdDate(now.atTime(LocalTime.MIN).plusSeconds(x))
                                    .lat(42.0)
                                    .lon(42.0)
                                    .build())
                    .collect(Collectors.toList());

            System.out.println("Saving geopositions...");
            geoPositionInfoRepository.saveAll(geopositions);

            System.out.println("Saving cluster geopositions...");
            clusterGeopositionRepository.saveAll(geopositions.stream()
                    .map((GeoPositionInfo geoposition) ->
                            ClusterGeoposition.builder()
                                    .cluster(cluster)
                                    .geoposition(geoposition)
                                    .build())
                    .collect(Collectors.toList()));
        }


        System.out.println("Querying...");
        long time = System.nanoTime();
        geoPositionInfoRepository.findAllByTerminalIdAndClusteredFalse(terminal.getId(), 100);

        System.out.printf("Elapsed time: %s%n", Duration.ofNanos(System.nanoTime() - time));
    }
}
