package me.sample.utils.geo;

public class GeoUtils {
    private GeoUtils() {
    }

    /**
     * calculate distance between two points in kilometers
     * @param pointXArrayRad
     * @param pointYArrayRad
     * @return distance in kilometers
     */
    public static double getDistance(double[] pointXArrayRad, double[] pointYArrayRad) {
        return Converter.fromRadToKm(GeoMath.getDistance(pointXArrayRad, pointYArrayRad));
    }
}
