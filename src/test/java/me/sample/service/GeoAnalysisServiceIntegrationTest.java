package me.sample.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import me.sample.domain.GeoPositionInfo;
import me.sample.domain.geo.Cluster;
import me.sample.domain.geo.ClusterGeoposition;
import me.sample.domain.geo.Event;
import me.sample.domain.geo.Point;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GeoAnalysisServiceIntegrationTest {

    @Autowired
    private GeoAnalysisService geoAnalysisService;

    @Before
    public void setUp() throws Exception {
    }

    // Note: 0.001 of latitude is approx. 111 meters

    /**
     * Тестовый сценарий "Дом-работа".
     * Пользователь находится днем на работе и заходит в приложение с интервалами более 30 минут до и после обеденного перерыва несколько раз.
     * Дома пользователь заходит в приложение более одного раза утром и более одного раза вечером.
     * Система интерпретирует это как два часто посещаемых места
     */
    @Test
    public void testCase1() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("07:00")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("08:00")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("11:00")), new Point(42.1, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.1, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:00")), new Point(42.1, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("14:00")), new Point(42.1, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("19:00")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("20:00")), new Point(42.0, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(2));

        assertThat(results.get(0).getLat(), is(42.1));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(4));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("14:00"))));

        assertThat(results.get(1).getLat(), is(42.0));
        assertThat(results.get(1).getLon(), is(42.0));
        assertThat(results.get(1).getVisitCount(), is(4));
        assertThat(results.get(1).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("20:00"))));
    }

    /**
     * Тестовый сценарий "Единократное посещение, перезагружал приложение несколько раз".
     * Пользователь зашел в магазин / тц / другое место единократно и в пределах 30 минут зашел в приложение более 2 раз (5-10 раз).
     * Система интерпретирует это как разовое посещение этого места (при условии что больше в этом месте пользователь никогда не бывал ранее)
     */
    @Test
    public void testCase2() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:01")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:02")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:03")), new Point(42.0, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:04")), new Point(42.0, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(1));

        assertThat(results.get(0).getLat(), is(42.0));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(1));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:00"))));
    }

    /**
     * Тестовый сценарий "Поездка на машине".
     * Пользователь движется с равномерной скоростью в фиксированном направлении, не пересекая свой маршрут,
     * при этом за любые 30 минут максимум две его геолокации оказываются на расстоянии менее 200 метров друг от друга.
     * Система интерпретирует это как единократные посещения точек по марштуру, расстояние между которыми более 200 метров
     */
    @Test
    public void testCase3() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:15")), new Point(42.001, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:30")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:45")), new Point(42.003, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:00")), new Point(42.004, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(3));

        assertThat(results.get(0).getLat(), is(42.000));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(1));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:00"))));

        assertThat(results.get(1).getLat(), is(42.002));
        assertThat(results.get(1).getLon(), is(42.0));
        assertThat(results.get(1).getVisitCount(), is(1));
        assertThat(results.get(1).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:30"))));

        assertThat(results.get(2).getLat(), is(42.004));
        assertThat(results.get(2).getLon(), is(42.0));
        assertThat(results.get(2).getVisitCount(), is(1));
        assertThat(results.get(2).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("13:00"))));
    }

    /**
     * Тестовый сценарий "Прогулка пешком".
     * Пользователь движется с равномерной скоростью в фиксированном направлении, не пересекая свой маршрут,
     * при этом за любые 30 минут более 2х его координат (много) оказываются на расстоянии меньшем 200 метров друг от друга,
     * но за время более 30 минут пользователь гарантированно смещается на расстояние более 200 метров.
     * Система интерпретирует это как единократные посещения точек по маршруту, точки отстоят друг от друга на 200 и более метров
     */
    @Test
    public void testCase4() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.0000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:10")), new Point(42.0007, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:20")), new Point(42.0014, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:30")), new Point(42.0021, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:40")), new Point(42.0028, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:50")), new Point(42.0035, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:00")), new Point(42.0042, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:10")), new Point(42.0049, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:20")), new Point(42.0056, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:30")), new Point(42.0063, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:40")), new Point(42.0070, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(4));

        assertThat(results.get(0).getLat(), is(42.0000));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(1));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:00"))));

        assertThat(results.get(1).getLat(), is(42.0021));
        assertThat(results.get(1).getLon(), is(42.0));
        assertThat(results.get(1).getVisitCount(), is(1));
        assertThat(results.get(1).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:30"))));

        assertThat(results.get(2).getLat(), is(42.0042));
        assertThat(results.get(2).getLon(), is(42.0));
        assertThat(results.get(2).getVisitCount(), is(1));
        assertThat(results.get(2).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("13:00"))));

        assertThat(results.get(3).getLat(), is(42.0063));
        assertThat(results.get(3).getLon(), is(42.0));
        assertThat(results.get(3).getVisitCount(), is(1));
        assertThat(results.get(3).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("13:30"))));
    }

    /**
     * Тестовый сценарий "Прогулка пешком, зашел в кафе".
     * Пользователь движется с равномерной скоростью в фиксированном направлении, не пересекая свой маршрут, при на некоторых участках за время более чем 30 минут 2 и более координаты оказываются на расстоянии меньшем 200 метров друг от друга.
     * Система интерпретирует это как посещение точек по маршруту, причем на тех участках маршрута где за время более 30 минут пользователь находился в точках отстоящих друг от друга менее чем на 200 метров - это считается как второе посещение.
     * Если спустя 1 час пользователь все еще находится менее чем в 200 метрах от этого места - это считается третьим (частым) посещением
     */
    @Test
    public void testCase5() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:15")), new Point(42.001, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:30")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:45")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:15")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("14:00")), new Point(42.002, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(2));

        assertThat(results.get(0).getLat(), is(42.000));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(1));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:00"))));

        assertThat(results.get(1).getLat(), is(42.002));
        assertThat(results.get(1).getLon(), is(42.0));
        assertThat(results.get(1).getVisitCount(), is(3));
        assertThat(results.get(1).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("14:00"))));
    }

    /**
     * Тестовый сценарий "Курьер 1".
     * В течение только 30 минут пользователь многократно перемещается между двумя точками, находящимися на расстоянии более 200 метров, и в каждой точке открывает приложение.
     * Система интерпретирует это как разовое посещение двух разных мест
     */
    @Test
    public void testCase6() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:05")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:10")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:15")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:20")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:25")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:30")), new Point(42.000, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(2));

        assertThat(results.get(0).getLat(), is(42.000));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(1));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:00"))));

        assertThat(results.get(1).getLat(), is(42.002));
        assertThat(results.get(1).getLon(), is(42.0));
        assertThat(results.get(1).getVisitCount(), is(1));
        assertThat(results.get(1).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:05"))));
    }

    /**
     * Тестовый сценарий "Курьер 2".
     * В течение более 30 минут пользователь многократно перемещается между двумя точками (более двух раз в каждой), находящимися на расстоянии более 200 метров, и в каждой точке открывает приложение.
     * Система интерпретирует это как многократное посещение двух разных мест
     */
    @Test
    public void testCase7() throws Exception {
        LocalDate today = LocalDate.now();

        Set<GeoPositionInfo> geopositions = Stream.of(
                new Event(LocalDateTime.of(today, LocalTime.parse("12:00")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:05")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:10")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:15")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:20")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:25")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:30")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:35")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:40")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:45")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:50")), new Point(42.000, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("12:55")), new Point(42.002, 42.0)),
                new Event(LocalDateTime.of(today, LocalTime.parse("13:00")), new Point(42.000, 42.0)))
                .map((Event event) ->
                        GeoPositionInfo.builder()
                                .createdDate(event.getAt())
                                .lat(event.getPoint().getLat())
                                .lon(event.getPoint().getLon())
                                .build())
                .collect(Collectors.toSet());


        List<Cluster> results = geoAnalysisService.clusterGeopositions(geopositions, Collections.emptySet())
                .stream()
                .map(ClusterGeoposition::getCluster)
                .distinct()
                .sorted(Comparator.comparing(Cluster::getLastVisitedAt))
                .collect(Collectors.toList());


        assertThat(results.size(), is(2));

        assertThat(results.get(0).getLat(), is(42.000));
        assertThat(results.get(0).getLon(), is(42.0));
        assertThat(results.get(0).getVisitCount(), is(2));
        assertThat(results.get(0).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:40"))));

        assertThat(results.get(1).getLat(), is(42.002));
        assertThat(results.get(1).getLon(), is(42.0));
        assertThat(results.get(1).getVisitCount(), is(2));
        assertThat(results.get(1).getLastVisitedAt(), is(LocalDateTime.of(today, LocalTime.parse("12:45"))));
    }
}
