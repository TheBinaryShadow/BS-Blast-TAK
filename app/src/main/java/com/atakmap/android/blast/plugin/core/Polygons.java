package com.atakmap.android.blast.plugin.core;

/** Small planar polygon helpers (local metres). */
public final class Polygons {

    private Polygons() {
    }

    /** Even-odd point-in-polygon; the polygon is implicitly closed. */
    public static boolean contains(double[][] poly, double x, double y) {
        if (poly == null || poly.length < 3)
            return false;
        boolean in = false;
        for (int i = 0, j = poly.length - 1; i < poly.length; j = i++) {
            double xi = poly[i][0], yi = poly[i][1];
            double xj = poly[j][0], yj = poly[j][1];
            if ((yi > y) != (yj > y)
                    && x < (xj - xi) * (y - yi) / (yj - yi) + xi)
                in = !in;
        }
        return in;
    }
}
