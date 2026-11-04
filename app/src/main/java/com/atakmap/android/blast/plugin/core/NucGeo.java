package com.atakmap.android.blast.plugin.core;

/** Places nuclear outlines (fallout, military zones) on the earth. */
public final class NucGeo {

    private NucGeo() {
    }

    /**
     * Local metres (x along {@code axisDeg}, y to its left) to {lat, lon}
     * pairs around ground zero.
     */
    public static double[][] place(double[][] localM, double axisDeg,
            double gzLat, double gzLon) {
        double th = Math.toRadians(Double.isNaN(axisDeg) ? 0 : axisDeg);
        double ux = Math.sin(th), uy = Math.cos(th);
        double[][] out = new double[localM.length][];
        for (int k = 0; k < localM.length; k++) {
            double along = localM[k][0], left = localM[k][1];
            double east = along * ux - left * uy;
            double north = along * uy + left * ux;
            double dist = Math.hypot(east, north);
            double brg = Math.toDegrees(Math.atan2(east, north));
            out[k] = Geo.destination(gzLat, gzLon, (brg + 360) % 360, dist);
        }
        return out;
    }

    static double[][] kmToM(double[][] km) {
        if (km == null)
            return null;
        double[][] m = new double[km.length][];
        for (int i = 0; i < km.length; i++)
            m[i] = new double[] { km[i][0] * 1000, km[i][1] * 1000 };
        return m;
    }

    /**
     * Outline of the H+1 contour {@code rate} as {lat, lon} pairs around
     * ground zero; null if the record draws no civil fallout.
     */
    public static double[][] fallout(NucRecord r, int rate, double gzLat,
            double gzLon) {
        if (!r.drawsFallout())
            return null;
        NucModel.Contour c = NucModel.contour(r.yieldKt, rate, r.windMph);
        if (c == null)
            return null;
        return place(NucModel.outline(c), r.downwindDeg(), gzLat, gzLon);
    }

    /** Military Zone I (1) or Zone II (2) outline; null if none. */
    public static double[][] zone(NucRecord r, int zone, double gzLat,
            double gzLon) {
        if (!r.drawsMilitary())
            return null;
        NucMil.Prediction p = r.militaryPrediction();
        double[][] km = zone == 1 ? p.zoneI() : p.zoneII();
        return km == null ? null : place(kmToM(km), p.eddDeg, gzLat, gzLon);
    }

    /** Time-of-arrival arc H+hours (open line); null if none. */
    public static double[][] arrivalArc(NucRecord r, int hours, double gzLat,
            double gzLon) {
        if (!r.drawsMilitary())
            return null;
        NucMil.Prediction p = r.militaryPrediction();
        if (!p.arrivalHours().contains(hours))
            return null;
        double[][] km = p.arc(hours * p.ewsKph);
        return km == null ? null : place(kmToM(km), p.eddDeg, gzLat, gzLon);
    }
}
