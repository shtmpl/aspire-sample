package me.sample.service;

import me.sample.repository.ClusterRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.repository.GeoPositionInfoRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {
        "logging.level.me.sample=INFO",
        "job.cluster.geoposition.association.enabled=false"
})
public class GeoAnalysisServicePerformanceTest {

    @Autowired
    private GeoAnalysisService geoAnalysisService;

    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private TerminalRepository terminalRepository;

    @Autowired
    private GeoPositionInfoRepository geoPositionInfoRepository;

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldAssociateGeopositions() throws Exception {
        LocalDate now = LocalDate.now();

        UUID terminalId = UUID.fromString("7d07c132-fd5c-424c-a179-4936e5783d27");
//        Terminal terminal = terminalRepository.save(Terminal.builder()
//                .id(terminalId)
//                .build());
//
//        for (int batch = 0; batch < 10; batch += 1) {
//            List<GeoPositionInfo> geopositions = IntStream.range(0, 100000)
//                    .mapToObj((int x) ->
//                            GeoPositionInfo.builder()
//                                    .terminal(terminal)
//                                    .createdDate(now.atTime(LocalTime.MIN).plusSeconds(x))
//                                    .lat(42.0)
//                                    .lon(42.0)
//                                    .build())
//                    .collect(Collectors.toList());
//
//            System.out.println("Saving geopositions...");
//            geoPositionInfoRepository.saveAll(geopositions);
//        }


        System.out.println("Associating geopositions...");
        long time = System.nanoTime();
        geoAnalysisService.associateGeopositionsForTerminal(terminalId, 1000);

        System.out.printf("Elapsed time: %s%n", Duration.ofNanos(System.nanoTime() - time));
    }
}
