package me.sample.utils.geo;

public class GeoMath {
    private GeoMath() {
    }


    public static double getDistance(double[] p1, double[] p2) {
        if (p1[0] == p2[0] && p1[1] == p2[1]) {
            return 0.0D;
        } else {
            double hsinX = Math.sin((p1[0] - p2[0]) * 0.5D);
            double hsinY = Math.sin((p1[1] - p2[1]) * 0.5D);
            double a = hsinY * hsinY + Math.cos(p1[1]) * Math.cos(p2[1]) * hsinX * hsinX;
            return 2.0D * Math.asin(Math.min(1.0D, Math.sqrt(a)));
        }
    }


}

