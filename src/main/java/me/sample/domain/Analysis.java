package me.sample.domain;

import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import me.sample.domain.geo.Event;
import me.sample.domain.geo.Point;

import java.time.Duration;
import java.util.Collection;

/**
 * Утилитный класс. Содержит функции, помогающие проведение анализа геолокации
 */
public final class Analysis {

    public static final double EARTH_RADIUS_IN_KILOMETERS = 6371;

    /**
     * Computes the distance between two points.
     * Uses haversine formula
     *
     * @param lat1 first point latitude
     * @param lon1 first point longitude
     * @param lat2 second point latitude
     * @param lon2 second point longitude
     * @return distance between the specified geopositions in meters
     */
    public static double spatialDistanceInKilometers(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_IN_KILOMETERS * c;
    }

    public static Distance spatialDistance(Point point1, Point point2) {
        return new Distance(
                spatialDistanceInKilometers(point1.getLat(), point1.getLon(), point2.getLat(), point2.getLon()),
                Metrics.KILOMETERS);
    }

    public static Duration temporalDistance(Event event1, Event event2) {
        return Duration.between(event1.getAt(), event2.getAt()).abs();
    }

    /**
     * Computes an average for the specified collection of geopositions
     *
     * @param points collection of geopositions
     * @return average for the specified collection of geopositions
     */
    public static Point avg(Collection<Point> points) {
        double latAvg = points.stream()
                .mapToDouble(Point::getLat)
                .average()
                .orElse(0);
        double lonAvg = points.stream()
                .mapToDouble(Point::getLon)
                .average()
                .orElse(0);

        return Point.builder()
                .lat(latAvg)
                .lon(lonAvg)
                .build();
    }
}
