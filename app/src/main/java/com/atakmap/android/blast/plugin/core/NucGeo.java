package com.atakmap.android.blast.plugin.core;

/** Places a fallout outline on the earth. */
public final class NucGeo {

    private NucGeo() {
    }

    /**
     * Outline of the H+1 contour {@code rate} as {lat, lon} pairs around
     * ground zero; null if the record draws no fallout.
     */
    public static double[][] fallout(NucRecord r, int rate, double gzLat,
            double gzLon) {
        if (!r.drawsFallout())
            return null;
        NucModel.Contour c = NucModel.contour(r.yieldKt, rate, r.windMph);
        if (c == null)
            return null;
        double[][] local = NucModel.outline(c);
        double th = Math.toRadians(r.downwindDeg());
        double ux = Math.sin(th), uy = Math.cos(th);
        double[][] out = new double[local.length][];
        for (int k = 0; k < local.length; k++) {
            double along = local[k][0], left = local[k][1];
            double east = along * ux - left * uy;
            double north = along * uy + left * ux;
            double dist = Math.hypot(east, north);
            double brg = Math.toDegrees(Math.atan2(east, north));
            out[k] = Geo.destination(gzLat, gzLon, (brg + 360) % 360, dist);
        }
        return out;
    }
}
