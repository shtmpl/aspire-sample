package me.sample.service;

import me.sample.repository.ClusterRepository;
import me.sample.repository.TerminalRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.Terminal;
import me.sample.domain.geo.Event;
import me.sample.domain.geo.Point;
import me.sample.repository.GeoPositionInfoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeoAnalysisServiceManualTest {

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
    public void shouldOperate() throws Exception {
        LocalDate today = LocalDate.now();

        Terminal terminal = terminalRepository.save(Terminal.builder()
                .build());


        Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("00:00")), new Point(42.00, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("00:10")), new Point(42.00, 42.0)),

                new Event(LocalDateTime.of(today, LocalTime.parse("10:00")), new Point(42.01, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("10:10")), new Point(42.01, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("11:00")), new Point(42.01, 42.0)),

                new Event(LocalDateTime.of(today, LocalTime.parse("20:00")), new Point(42.02, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("20:10")), new Point(42.02, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("21:00")), new Point(42.02, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("22:00")), new Point(42.02, 42.0)))
                .forEach((Event event) -> {
                    geoPositionInfoRepository.save(GeoPositionInfo.builder()
                            .terminal(terminal)
                            .createdDate(event.getAt())
                            .lat(event.getPoint().getLat())
                            .lon(event.getPoint().getLon())
                            .build());

                    geoAnalysisService.associateGeopositions();
                });


        System.out.println("Done!");
    }
}
