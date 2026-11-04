package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified fallout prediction of legacy public US doctrine: FM 3-3-1
 * Nuclear Contamination Avoidance (9 Sep 1994, approved for public
 * release) and FM 3-11.3 MTTP for CBRN Contamination Avoidance (2 Feb
 * 2006, Change 1 30 Apr 2009, distribution unlimited), the M5A2 method.
 * The current manuals (TM 3-11.32, ATP-45(F)) are restricted; this
 * follows the last public versions.
 * <ul>
 * <li>Zone I downwind distance: FM 3-3-1 Table E-4 (by yield and
 * effective wind speed, km). Where the published table decreases with a
 * larger yield or wind (typesetting errors in the 6-13 km band), the larger
 * neighbouring value is used, so no distance is below the table.</li>
 * <li>Zone II = 2 x Zone I. Radial lines 20 degrees either side of the
 * effective downwind direction (40 degree sector) unless the effective
 * downwind message (EDM) gives an expanded angle. Tangents from the
 * cloud-radius semicircle of the yield group to the Zone I arc.</li>
 * <li>Cloud radius per yield group (M5A2 predictor, FM 3-3-1 p. 3-23):
 * 1.2, 1.9, 4.2, 6.8, 11.2, 18.0, 28 km for groups ALFA-GOLF (2 kt, 5 kt,
 * 30 kt, 100 kt, 300 kt, 1 MT, 3 MT). Above 3 MT (no yield group): FM
 * 3-3-1 Table E-1 cloud radius.</li>
 * <li>Effective wind speed below 8 km/h: circular case, Zone I radius from
 * the table at 8 km/h, Zone II twice that (FM 3-11.3 G-31; FM 3-3-1
 * p. 3-16).</li>
 * <li>Time-of-arrival arcs every effective wind speed (km) within Zone II;
 * actual arrival may be as early as half the estimate; no fallout by twice
 * the estimate (or 12 h, whichever is earlier) means none expected (FM
 * 3-3-1 p. 3-23).</li>
 * <li>No prediction for air bursts over land (FM 3-11.3 G-23).</li>
 * </ul>
 */
public final class NucMil {

    public static final String SOURCE = "FM 3-3-1 (1994) / FM 3-11.3 (2009)";

    /** FM 3-11.3 G-23: Zone I / Zone II doses, cGy. */
    public static final int ZONE_I_CGY = 125, ZONE_II_CGY = 75;

    public static final double MIN_EWS_KPH = 8;
    public static final double STD_ANGLE_DEG = 40;
    public static final double MAX_ANGLE_DEG = 180;
    /** Speed of sound for the polar plot (FM 3-11.3 G-15), km/s. */
    public static final double SOUND_KM_S = 0.35;

    static final double[] E4_V = { 8, 10, 12, 14, 16, 18, 20, 25, 30, 35, 40,
            45, 50, 60, 70, 80, 90, 100, 120, 160 };
    static final double[] E4_W = { 0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07,
            0.08, 0.09, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1, 2, 3,
            4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 200,
            300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 2500, 3000,
            4000, 5000, 6000, 7000, 8000, 9000, 10000 };
    /** FM 3-3-1 Table E-4, Zone I downwind distance, km, as published. */
    static final double[][] E4_PUBLISHED = {
            { 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.3, 0.3, 0.3, 0.3, 0.4, 0.4, 0.5 }, // 0.01
            { 0.1, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.3, 0.3, 0.3, 0.4, 0.4, 0.4, 0.5, 0.5, 0.5, 0.6, 0.6, 0.7, 0.8 }, // 0.02
            { 0.2, 0.2, 0.2, 0.3, 0.3, 0.3, 0.3, 0.4, 0.4, 0.5, 0.5, 0.5, 0.6, 0.6, 0.7, 0.7, 0.8, 0.9, 0.9, 1.1 }, // 0.03
            { 0.2, 0.3, 0.3, 0.3, 0.4, 0.4, 0.4, 0.5, 0.5, 0.6, 0.6, 0.7, 0.7, 0.8, 0.9, 0.9, 1, 1.1, 1.2, 1.4 }, // 0.04
            { 0.3, 0.3, 0.3, 0.4, 0.4, 0.4, 0.5, 0.5, 0.6, 0.7, 0.7, 0.8, 0.8, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.7 }, // 0.05
            { 0.3, 0.4, 0.4, 0.4, 0.5, 0.5, 0.5, 0.6, 0.7, 0.8, 0.8, 0.9, 0.9, 1.1, 1.2, 1.3, 1.3, 1.4, 1.6, 1.9 }, // 0.06
            { 0.4, 0.4, 0.5, 0.5, 0.5, 0.6, 0.6, 0.7, 0.8, 0.9, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.5, 1.6, 1.8, 2.1 }, // 0.07
            { 0.4, 0.4, 0.5, 0.5, 0.6, 0.6, 0.7, 0.8, 0.9, 1, 1, 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 1.8, 2, 2.4 }, // 0.08
            { 0.4, 0.5, 0.5, 0.6, 0.6, 0.7, 0.7, 0.8, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 1.8, 2, 2.2, 2.6 }, // 0.09
            { 0.5, 0.5, 0.6, 0.6, 0.7, 0.8, 0.8, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 1.8, 2, 2.1, 2.4, 2.8 }, // 0.1
            { 0.8, 0.9, 1, 1.1, 1.2, 1.3, 1.4, 1.6, 1.7, 1.9, 2.1, 2.2, 2.4, 2.6, 2.9, 3.1, 3.4, 3.6, 4, 4.7 }, // 0.2
            { 1.1, 1.2, 1.4, 1.5, 1.6, 1.7, 1.8, 2.1, 2.4, 2.6, 2.8, 3, 3.2, 3.6, 3.9, 4.2, 4.6, 4.9, 5.4, 6.4 }, // 0.3
            { 1.3, 1.5, 1.7, 1.9, 2, 2.2, 2.3, 2.6, 2.9, 3.2, 3.5, 3.7, 4, 4.4, 4.9, 5.3, 5.7, 6, 6.7, 10 }, // 0.4
            { 1.6, 1.8, 2, 2.2, 2.4, 2.6, 2.7, 3.1, 3.5, 3.8, 4.1, 4.4, 4.7, 5.3, 5.8, 6.3, 6.7, 9, 9, 11 }, // 0.5
            { 1.8, 2.1, 2.3, 2.5, 2.7, 2.9, 3.1, 3.6, 4, 4.4, 4.7, 5.1, 5.4, 6, 6.6, 8, 9, 9, 10, 12 }, // 0.6
            { 2, 2.3, 2.6, 2.8, 3.1, 3.3, 3.5, 4, 4.5, 4.9, 5.3, 5.7, 6.1, 6.8, 8, 9, 10, 10, 11, 13 }, // 0.7
            { 2.2, 2.6, 2.9, 3.1, 3.4, 3.6, 3.9, 4.4, 5, 5.4, 5.9, 6.3, 6.7, 8, 9, 10, 10, 11, 12, 14 }, // 0.8
            { 2.4, 2.8, 3.1, 3.4, 3.7, 4, 4.2, 4.9, 5.4, 5.9, 6.4, 6.9, 8, 9, 10, 10, 11, 11, 12, 14 }, // 0.9
            { 2.6, 3, 3.4, 3.7, 4, 4.3, 4.6, 5.3, 5.9, 6.4, 7, 8, 8, 9, 10, 11, 11, 12, 13, 15 }, // 1
            { 4.5, 5.1, 5.7, 6.3, 6.8, 7, 7, 8, 9, 10, 10, 11, 12, 13, 14, 15, 16, 17, 18, 21 }, // 2
            { 6.1, 7, 7, 7, 8, 8, 9, 10, 11, 12, 13, 13, 14, 16, 17, 18, 19, 20, 22, 26 }, // 3
            { 6, 7, 8, 9, 9, 10, 10, 11, 13, 14, 15, 15, 16, 18, 19, 21, 22, 23, 25, 29 }, // 4
            { 7, 8, 9, 9, 10, 11, 11, 13, 14, 15, 16, 17, 18, 20, 21, 23, 24, 26, 28, 32 }, // 5
            { 8, 9, 10, 10, 11, 12, 12, 14, 15, 16, 18, 19, 20, 22, 23, 25, 26, 28, 31, 35 }, // 6
            { 8, 9, 10, 11, 12, 13, 13, 15, 16, 18, 19, 20, 21, 23, 25, 27, 28, 30, 33, 38 }, // 7
            { 9, 10, 11, 12, 13, 13, 14, 16, 17, 19, 20, 21, 23, 25, 27, 29, 30, 32, 35, 41 }, // 8
            { 9, 11, 12, 13, 13, 14, 15, 17, 18, 20, 21, 23, 24, 26, 28, 30, 32, 34, 37, 43 }, // 9
            { 10, 11, 12, 13, 14, 15, 16, 18, 19, 21, 22, 24, 25, 27, 30, 32, 34, 36, 39, 45 }, // 10
            { 14, 15, 17, 18, 20, 21, 22, 24, 27, 29, 31, 33, 35, 38, 41, 44, 47, 49, 54, 63 }, // 20
            { 17, 19, 20, 22, 24, 25, 26, 30, 33, 35, 38, 40, 42, 46, 50, 53, 57, 60, 66, 76 }, // 30
            { 19, 21, 23, 25, 27, 29, 30, 34, 37, 40, 43, 46, 48, 53, 57, 61, 65, 69, 75, 87 }, // 40
            { 21, 24, 26, 28, 30, 32, 34, 38, 41, 45, 48, 51, 54, 59, 64, 68, 72, 76, 83, 97 }, // 50
            { 23, 26, 28, 31, 33, 35, 37, 41, 45, 49, 52, 55, 58, 64, 69, 74, 79, 83, 91, 105 }, // 60
            { 25, 28, 31, 33, 35, 37, 40, 44, 49, 52, 56, 60, 63, 69, 75, 80, 85, 89, 98, 113 }, // 70
            { 26, 30, 33, 35, 38, 40, 42, 47, 52, 56, 60, 63, 67, 73, 79, 85, 90, 95, 104, 121 }, // 80
            { 28, 31, 34, 37, 40, 42, 45, 50, 55, 59, 63, 67, 71, 78, 84, 90, 95, 101, 110, 128 }, // 90
            { 29, 33, 36, 39, 42, 44, 47, 52, 57, 62, 66, 71, 74, 82, 88, 94, 100, 106, 116, 134 }, // 100
            { 41, 46, 50, 54, 58, 62, 65, 73, 80, 86, 92, 98, 103, 113, 122, 131, 139, 147, 161, 186 }, // 200
            { 49, 55, 61, 66, 70, 75, 79, 88, 97, 104, 112, 119, 125, 137, 148, 159, 168, 178, 195, 225 }, // 300
            { 57, 63, 70, 75, 81, 85, 90, 101, 111, 120, 128, 136, 143, 157, 170, 182, 193, 204, 223, 258 }, // 400
            { 63, 71, 77, 84, 90, 95, 100, 112, 123, 133, 142, 151, 159, 175, 189, 202, 214, 226, 248, 287 }, // 500
            { 69, 77, 84, 91, 98, 104, 109, 122, 134, 145, 155, 165, 174, 190, 206, 220, 234, 247, 270, 313 }, // 600
            { 74, 83, 91, 98, 105, 111, 117, 132, 144, 156, 167, 177, 187, 205, 221, 237, 251, 265, 291, 336 }, // 700
            { 79, 88, 97, 104, 112, 119, 125, 140, 154, 166, 178, 189, 199, 218, 236, 252, 268, 283, 310, 358 }, // 800
            { 83, 93, 102, 110, 118, 125, 132, 148, 162, 176, 188, 199, 210, 231, 249, 267, 283, 299, 328, 379 }, // 900
            { 87, 98, 107, 116, 124, 132, 139, 156, 171, 185, 198, 210, 221, 242, 262, 280, 298, 314, 344, 398 }, // 1000
            { 106, 119, 130, 141, 150, 160, 168, 189, 207, 224, 239, 254, 268, 294, 318, 340, 361, 380, 417, 483 }, // 1500
            { 121, 136, 149, 161, 172, 183, 193, 216, 237, 256, 274, 291, 307, 337, 364, 389, 413, 436, 478, 553 }, // 2000
            { 135, 151, 166, 179, 192, 203, 215, 240, 263, 285, 305, 323, 341, 374, 404, 433, 459, 484, 531, 614 }, // 2500
            { 147, 165, 181, 195, 209, 222, 234, 262, 287, 310, 332, 352, 372, 408, 441, 472, 501, 528, 579, 670 }, // 3000
            { 169, 189, 207, 224, 239, 254, 268, 300, 329, 356, 381, 404, 426, 467, 505, 540, 574, 605, 663, 767 }, // 4000
            { 187, 210, 230, 249, 266, 282, 298, 333, 366, 395, 423, 449, 473, 519, 561, 601, 637, 672, 737, 853 }, // 5000
            { 204, 229, 251, 271, 290, 308, 325, 363, 399, 431, 461, 489, 516, 566, 612, 655, 695, 733, 804, 930 }, // 6000
            { 220, 246, 270, 291, 312, 331, 349, 391, 429, 463, 496, 526, 555, 609, 658, 704, 747, 788, 864, 1000 }, // 7000
            { 234, 262, 287, 310, 332, 353, 372, 416, 457, 494, 528, 561, 591, 648, 701, 750, 796, 840, 921, 1065 }, // 8000
            { 247, 277, 304, 328, 351, 373, 393, 440, 483, 522, 558, 593, 625, 686, 741, 793, 842, 888, 974, 1126 }, // 9000
            { 260, 291, 319, 345, 369, 392, 413, 463, 507, 549, 587, 623, 657, 721, 779, 834, 885, 933, 1023, 1184 }, // 10000
    };
    /** E4 with every value raised to at least its smaller-yield/wind neighbours. */
    static final double[][] E4 = monotone(E4_PUBLISHED);

    static double[][] monotone(double[][] t) {
        double[][] m = new double[t.length][];
        for (int i = 0; i < t.length; i++)
            m[i] = t[i].clone();
        boolean changed = true;
        while (changed) {
            changed = false;
            for (int i = 0; i < m.length; i++)
                for (int j = 0; j < m[i].length; j++) {
                    double v = m[i][j];
                    if (i > 0)
                        v = Math.max(v, m[i - 1][j]);
                    if (j > 0)
                        v = Math.max(v, m[i][j - 1]);
                    if (v != m[i][j]) {
                        m[i][j] = v;
                        changed = true;
                    }
                }
        }
        return m;
    }

    /** Yield groups of the EDM: upper yield (kt), letter, M5A2 cloud radius km. */
    static final double[] GROUP_KT = { 2, 5, 30, 100, 300, 1000, 3000 };
    static final String[] GROUP = { "ALFA", "BRAVO", "CHARLIE", "DELTA",
            "ECHO", "FOXTROT", "GOLF" };
    static final double[] GROUP_RADIUS_KM = { 1.2, 1.9, 4.2, 6.8, 11.2, 18.0,
            28 };
    /** FM 3-3-1 Table E-1 cloud radius above 3 MT (kt, km). */
    static final double[] E1_KT = { 3000, 4000, 5000, 6000, 7000, 8000, 9000,
            10000, 20000 };
    static final double[] E1_KM = { 28, 32, 35, 37, 40, 42, 44, 46, 62 };

    private NucMil() {
    }

    /** EDM yield group letter, or null above 3 MT. */
    public static String group(double kt) {
        for (int i = 0; i < GROUP_KT.length; i++)
            if (kt <= GROUP_KT[i])
                return GROUP[i];
        return null;
    }

    /** Cloud radius for the yield's group (or Table E-1 above 3 MT), km. */
    public static double cloudRadiusKm(double kt) {
        for (int i = 0; i < GROUP_KT.length; i++)
            if (kt <= GROUP_KT[i])
                return GROUP_RADIUS_KM[i];
        return NucModel.logLog(E1_KT, E1_KM, kt);
    }

    /** Zone I downwind distance (km) from Table E-4; wind below 8 uses 8. */
    public static double zoneIKm(double kt, double ewsKph) {
        double v = Math.max(MIN_EWS_KPH, ewsKph);
        double[] col = new double[E4_W.length];
        for (int i = 0; i < E4_W.length; i++)
            col[i] = NucModel.logLog(E4_V, E4[i], v);
        return NucModel.logLog(E4_W, col, kt);
    }

    /** True if Table E-4 had to be extrapolated. */
    public static boolean extrapolated(double kt, double ewsKph) {
        return kt > 10000 || ewsKph > 160;
    }

    /** One simplified fallout prediction (distances in km, angles deg). */
    public static final class Prediction {
        public final double yieldKt, eddDeg, ewsKph, angleDeg;
        public final double zoneIKm, zoneIIKm, cloudRadiusKm;
        public final boolean circular;
        /** Zone I is no larger than the cloud radius (drawn as that circle). */
        public final boolean withinCloud;

        Prediction(double yieldKt, double eddDeg, double ewsKph,
                double angleDeg, double zoneIKm, double cloudRadiusKm,
                boolean circular) {
            this.yieldKt = yieldKt;
            this.eddDeg = eddDeg;
            this.ewsKph = ewsKph;
            this.angleDeg = angleDeg;
            this.zoneIKm = zoneIKm;
            this.zoneIIKm = 2 * zoneIKm;
            this.cloudRadiusKm = cloudRadiusKm;
            this.circular = circular;
            this.withinCloud = !circular && zoneIKm <= cloudRadiusKm;
        }

        /** Hours after burst for the nth arc; arcs stop inside Zone II. */
        public List<Integer> arrivalHours() {
            List<Integer> out = new ArrayList<>();
            if (circular || !(ewsKph > 0))
                return out;
            for (int n = 1; n <= 48 && n * ewsKph < zoneIIKm - 1e-9; n++)
                out.add(n);
            return out;
        }

        /** Estimated arrival time (h) at a downwind distance (km). */
        public double arrivalH(double km) {
            return circular ? Double.NaN : km / ewsKph;
        }

        /** Zone I outline, local km (x downwind, y left), closed implicitly. */
        public double[][] zoneI() {
            if (circular)
                return circle(zoneIKm);
            if (withinCloud)
                return circle(cloudRadiusKm);
            double h = Math.toRadians(angleDeg / 2);
            double r = cloudRadiusKm, z = zoneIKm;
            double t = Math.min(Math.PI, h + Math.acos(r / z));
            List<double[]> p = new ArrayList<>();
            int n = 24;
            for (int i = 0; i <= n; i++) {
                double a = -h + 2 * h * i / n;
                p.add(new double[] { z * Math.cos(a), z * Math.sin(a) });
            }
            for (int i = 0; i <= n; i++) {
                double a = t + (2 * Math.PI - 2 * t) * i / n;
                p.add(new double[] { r * Math.cos(a), r * Math.sin(a) });
            }
            return p.toArray(new double[0][]);
        }

        /** Zone II outline (annular sector or, circular case, full circle). */
        public double[][] zoneII() {
            if (circular)
                return circle(zoneIIKm);
            double inner = withinCloud ? cloudRadiusKm : zoneIKm;
            if (zoneIIKm <= inner)
                return null;
            double h = Math.toRadians(angleDeg / 2);
            List<double[]> p = new ArrayList<>();
            int n = 24;
            for (int i = 0; i <= n; i++) {
                double a = -h + 2 * h * i / n;
                p.add(new double[] { zoneIIKm * Math.cos(a),
                        zoneIIKm * Math.sin(a) });
            }
            for (int i = n; i >= 0; i--) {
                double a = -h + 2 * h * i / n;
                p.add(new double[] { inner * Math.cos(a), inner * Math.sin(a) });
            }
            return p.toArray(new double[0][]);
        }

        /** Arc at d km clipped to the predicted area, local km; null if none. */
        public double[][] arc(double km) {
            double[][] z1 = zoneI(), z2 = zoneII();
            double max = -1;
            for (double deg = 0; deg <= 180; deg += 0.5) {
                double a = Math.toRadians(deg);
                double x = km * Math.cos(a), y = km * Math.sin(a);
                if (Polygons.contains(z1, x, y)
                        || (z2 != null && Polygons.contains(z2, x, y)))
                    max = deg;
                else if (max >= 0)
                    break;
            }
            if (max <= 0)
                return null;
            List<double[]> p = new ArrayList<>();
            int n = 16;
            for (int i = 0; i <= n; i++) {
                double a = Math.toRadians(-max + 2 * max * i / n);
                p.add(new double[] { km * Math.cos(a), km * Math.sin(a) });
            }
            return p.toArray(new double[0][]);
        }

        /** 0 outside, 1 = Zone I, 2 = Zone II, for a point in local km. */
        public int zoneAt(double xKm, double yKm) {
            if (Polygons.contains(zoneI(), xKm, yKm))
                return 1;
            double[][] z2 = zoneII();
            return z2 != null && Polygons.contains(z2, xKm, yKm) ? 2 : 0;
        }
    }

    static double[][] circle(double r) {
        int n = 72;
        double[][] p = new double[n][];
        for (int i = 0; i < n; i++) {
            double a = 2 * Math.PI * i / n;
            p[i] = new double[] { r * Math.cos(a), r * Math.sin(a) };
        }
        return p;
    }

    /**
     * Prediction from the effective downwind direction (toward, degrees)
     * and effective wind speed (km/h). Below 8 km/h: circular case.
     */
    public static Prediction predict(double kt, double eddDeg, double ewsKph,
            double angleDeg) {
        double ang = Double.isNaN(angleDeg) ? STD_ANGLE_DEG
                : Math.max(STD_ANGLE_DEG, Math.min(MAX_ANGLE_DEG, angleDeg));
        boolean circ = !(ewsKph >= MIN_EWS_KPH);
        return new Prediction(kt, eddDeg, ewsKph, ang, zoneIKm(kt, ewsKph),
                cloudRadiusKm(kt), circ);
    }

    /** Circular case with the Zone I distance given by a 3-digit EDM line. */
    public static Prediction circular(double kt, double zoneIKm) {
        return new Prediction(kt, Double.NaN, Double.NaN, STD_ANGLE_DEG,
                zoneIKm, cloudRadiusKm(kt), true);
    }

    /** A parsed EDM line: dddsss, dddsssaaa, or zzz (circular). */
    public static final class Edm {
        public final double eddDeg, ewsKph, angleDeg, zoneIKm;

        Edm(double eddDeg, double ewsKph, double angleDeg, double zoneIKm) {
            this.eddDeg = eddDeg;
            this.ewsKph = ewsKph;
            this.angleDeg = angleDeg;
            this.zoneIKm = zoneIKm;
        }

        public boolean isCircular() {
            return !Double.isNaN(zoneIKm);
        }
    }

    /**
     * Parse the digits of one EDM yield-group line (FM 3-3-1 3-6-9-digit
     * rule); letters and spaces are ignored. Null if not 3, 6 or 9 digits
     * or out of range.
     */
    public static Edm parseEdm(String s) {
        if (s == null)
            return null;
        StringBuilder d = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c >= '0' && c <= '9')
                d.append(c);
            else if (Character.isLetter(c) || c == ' ' || c == '/')
                continue;
            else
                return null;
        }
        String t = d.toString();
        try {
            if (t.length() == 3) {
                int z = Integer.parseInt(t);
                return z >= 1 ? new Edm(Double.NaN, Double.NaN, Double.NaN, z)
                        : null;
            }
            if (t.length() == 6 || t.length() == 9) {
                int dir = Integer.parseInt(t.substring(0, 3));
                int spd = Integer.parseInt(t.substring(3, 6));
                if (dir > 360 || spd < 1)
                    return null;
                double ang = Double.NaN;
                if (t.length() == 9) {
                    ang = Integer.parseInt(t.substring(6, 9));
                    if (ang < STD_ANGLE_DEG || ang > MAX_ANGLE_DEG)
                        return null;
                }
                return new Edm(dir % 360, spd, ang, Double.NaN);
            }
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    /** Polar plot distance (km) from flash-to-bang seconds. */
    public static double polarKm(double flashToBangS) {
        return flashToBangS * SOUND_KM_S;
    }
}
