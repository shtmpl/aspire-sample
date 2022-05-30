package me.sample.utils.geo;

public class Converter {
    private Converter() {
    }

    public static double toDeg(double radians) {
        return radians * 57.29577951308232D;
    }

    public static double toRad(double degrees) {
        return degrees * 0.017453292519943295D;
    }

    public static double fromDegToKm(double deg) {
        return deg * 111.31949079327356D;
    }

    public static double fromRadToKm(double rad) {
        return rad * 6378.137D;
    }
}
