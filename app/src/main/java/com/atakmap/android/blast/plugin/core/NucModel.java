package com.atakmap.android.blast.plugin.core;

/**
 * Nuclear detonation reference rings (Blast 1.1). Protective planning
 * estimates only; every number here comes from a US government source and
 * is traceable to a paragraph, table or figure:
 * <ul>
 * <li><b>Damage zones</b>: FEMA, Planning Guidance for Response to a Nuclear
 * Detonation, 3rd ed. (2022), pp. 18-20: Severe / Moderate / Light Damage
 * Zones end about 0.5 / 1 / 3 miles from a 10 kT ground-level detonation;
 * ranges grow "by a factor of two" per tenfold yield (Fig. 5), i.e. the
 * cube-root scaling of Glasstone &amp; Dolan §3.61.</li>
 * <li><b>Air-burst envelopes</b>: no burst height is taken or shown. Each
 * damage zone is multiplied by the ratio of the maximum ground range of its
 * overpressure (over all heights) to the surface-burst range, read from
 * Glasstone &amp; Dolan (1977) Fig. 3.73c (1 kt): 8 psi 1,683/1,068 ft,
 * 2 psi 4,199/2,516 ft, 1 psi 7,006/3,807 ft. Zone overpressures from FEMA
 * 2nd ed. (2010): SDZ edge 5-8 psi, MDZ edge 2-3 psi, LDZ edge 0.5 psi.
 * Glasstone's charts stop at 1 psi, so the LDZ ratio (2.0) extends the
 * rising trend (1.67 at 2 psi, 1.84 at 1 psi) and is flagged.</li>
 * <li><b>Burns</b>: second-degree burns to bare skin, 50% probability, using
 * the lowest-threshold curve of Glasstone Fig. 12.64; slant range for that
 * exposure from Fig. 7.42 (air bursts, 12-mile visibility). Surface bursts
 * deliver 0.18/0.35 of the air-burst exposure at the same distance
 * (§7.101, Table 7.88), so the air chart is read at threshold x 0.35/0.18.</li>
 * <li><b>Initial radiation</b>: 100 rad (1 Gy) total gamma + neutron dose
 * outdoors, unshielded: Figs. 8.33a/8.64a (fission, &le;100 kt) and
 * 8.33b/8.64b (thermonuclear, &gt;100 kt). Contact surface bursts: gamma x
 * Table 8.37 factor, neutrons x 1/2 (§8.64-8.65).</li>
 * <li><b>Fallout</b>: Glasstone Table 9.93 (H+1 reference dose-rate
 * contours, contact surface burst, 100% fission, 15 mph), wind factor
 * §9.96-9.97, shape after Fig. 9.93.</li>
 * </ul>
 * For air bursts every slant range is drawn as a ground range (always at
 * least the true ground distance), so no height is ever needed. Graph
 * readings are digitized from the 1977 PDF at 300 dpi.
 */
public final class NucModel {

    /** Model id carried in the CoT detail; bump when any number changes. */
    public static final String MODEL_ID = "BLAST-NUC";
    public static final String MODEL_ED = "2026-11";

    public static final double MIN_KT = 0.1;
    public static final double MAX_KT = 20000;

    public static final double M_PER_MI = 1609.344;
    static final double M_PER_YD = 0.9144;

    /** Burst condition as the user knows it. */
    public enum Burst {
        SURFACE("Surface / ground level"), AIR("Air burst (envelope)"),
        UNKNOWN("Unknown (worst case of both)");

        public final String label;

        Burst(String label) {
            this.label = label;
        }

        public static Burst parse(String s) {
            for (Burst b : values())
                if (b.name().equals(s))
                    return b;
            return UNKNOWN;
        }
    }

    // ------------------------------------------------------------------
    // FEMA damage zones

    /** FEMA 3rd ed. pp. 18-19, 10 kT at ground level, miles. */
    static final double SDZ_10KT_MI = 0.5, MDZ_10KT_MI = 1.0,
            LDZ_10KT_MI = 3.0;

    /**
     * Air-burst envelope / surface ratios (Glasstone Fig. 3.73c). SDZ uses
     * the larger of the 5-8 psi readings (8 psi), MDZ the 2 psi reading,
     * LDZ an extension below Glasstone's 1 psi curve.
     */
    static final double AIR_SDZ = 1683.0 / 1068.0;
    static final double AIR_MDZ = 4199.0 / 2516.0;
    static final double AIR_LDZ = 2.0;

    private NucModel() {
    }

    static double cube(double kt) {
        return Math.cbrt(kt / 10.0);
    }

    public static double sdzM(double kt, Burst b) {
        return SDZ_10KT_MI * M_PER_MI * cube(kt) * (b == Burst.SURFACE ? 1
                : AIR_SDZ);
    }

    public static double mdzM(double kt, Burst b) {
        return MDZ_10KT_MI * M_PER_MI * cube(kt) * (b == Burst.SURFACE ? 1
                : AIR_MDZ);
    }

    public static double ldzM(double kt, Burst b) {
        return LDZ_10KT_MI * M_PER_MI * cube(kt) * (b == Burst.SURFACE ? 1
                : AIR_LDZ);
    }

    // ------------------------------------------------------------------
    // Thermal: Fig. 12.64 threshold, Fig. 7.42 range

    static final double[] BURN_KT = { 1, 2, 5, 10, 20, 50, 100, 500, 1000,
            2000, 5000, 10000 };
    /** Second-degree, 50%, lowest-threshold curve, cal/cm² (Fig. 12.64). */
    static final double[] BURN_Q = { 3.55, 3.61, 3.73, 3.85, 4.03, 4.29,
            4.51, 5.17, 5.46, 5.83, 6.29, 6.63 };

    static final double[] TH_KT = { 1, 2, 5, 10, 20, 50, 100, 200, 500, 1000,
            2000, 5000, 10000 };
    static final double[] TH_Q = { 3, 5, 8, 12, 25, 50 };
    /** Fig. 7.42 slant range (miles) for TH_Q[i] at TH_KT[j]. */
    static final double[][] TH_MI = {
            { 0.566, 0.817, 1.297, 1.788, 2.391, 3.433, 4.469, 5.792, 8.107,
                    10.451, 12.958, 16.926, 20.579 },
            { 0.437, 0.624, 0.990, 1.382, 1.881, 2.797, 3.738, 4.887, 6.841,
                    8.690, 10.795, 14.397, 17.712 },
            { 0.350, 0.491, 0.784, 1.107, 1.514, 2.269, 3.058, 4.049, 5.778,
                    7.350, 9.233, 12.485, 15.665 },
            { 0.282, 0.404, 0.645, 0.919, 1.287, 1.936, 2.577, 3.418, 4.943,
                    6.463, 8.203, 10.948, 13.457 },
            { 0.200, 0.282, 0.444, 0.621, 0.856, 1.318, 1.798, 2.432, 3.618,
                    4.842, 6.347, 8.919, 11.519 },
            { 0.141, 0.196, 0.311, 0.441, 0.618, 0.968, 1.345, 1.849, 2.809,
                    3.769, 4.989, 7.140, 9.303 },
    };

    /** Effective thermal partition, air (Table 7.88) and contact surface. */
    static final double F_AIR = 0.35, F_SURFACE = 0.18;

    /** Radiant exposure for second-degree burns at this yield, cal/cm². */
    public static double burnThreshold(double kt) {
        return logLog(BURN_KT, BURN_Q, kt);
    }

    /** Distance to which second-degree burns to bare skin are likely. */
    public static double burnM(double kt, Burst b) {
        double q = burnThreshold(kt);
        double air = thermalRangeMi(kt, q);
        if (b == Burst.SURFACE)
            return thermalRangeMi(kt, q * F_AIR / F_SURFACE) * M_PER_MI;
        return air * M_PER_MI;
    }

    /** Fig. 7.42: slant range (mi) for exposure q at yield kt (air burst). */
    static double thermalRangeMi(double kt, double q) {
        double[] r = new double[TH_Q.length];
        for (int i = 0; i < TH_Q.length; i++)
            r[i] = logLog(TH_KT, TH_MI[i], kt);
        // r falls as q rises: interpolate log r against log q.
        double[] lq = new double[TH_Q.length];
        double[] lr = new double[TH_Q.length];
        for (int i = 0; i < TH_Q.length; i++) {
            lq[i] = Math.log(TH_Q[i]);
            lr[i] = Math.log(r[i]);
        }
        return Math.exp(lin(lq, lr, Math.log(q)));
    }

    // ------------------------------------------------------------------
    // Initial radiation: Figs. 8.33a/b (gamma), 8.64a/b (neutron)

    /** Doses of the figure curves, rad (tissue). */
    static final double[] RAD_D = { 30, 100, 300, 1000, 3000, 10000 };
    static final double[] FIS_KT = { 1, 2, 5, 10, 20, 50, 100 };
    static final double[] TN_KT = { 100, 200, 500, 1000, 2000, 5000, 10000,
            20000 };
    /** Slant range (yards) for RAD_D[i] at FIS_KT[j]: Fig. 8.33a. */
    static final double[][] GAMMA_FIS = {
            { 1467, 1636, 1863, 2048, 2257, 2557, 2794 },
            { 1199, 1358, 1574, 1749, 1950, 2241, 2484 },
            { 965, 1111, 1311, 1473, 1668, 1962, 2204 },
            { 748, 878, 1060, 1212, 1393, 1656, 1863 },
            { 562, 687, 856, 992, 1153, 1397, 1607 },
            { 416, 504, 634, 753, 905, 1140, 1334 },
    };
    /** Fig. 8.64a. */
    static final double[][] NEUT_FIS = {
            { 1373, 1508, 1685, 1817, 1950, 2129, 2265 },
            { 1177, 1305, 1471, 1596, 1721, 1890, 2020 },
            { 983, 1109, 1273, 1396, 1518, 1685, 1812 },
            { 818, 933, 1086, 1200, 1316, 1472, 1592 },
            { 657, 769, 914, 1022, 1135, 1284, 1396 },
            { 490, 596, 736, 841, 948, 1093, 1203 },
    };
    /** Fig. 8.33b. */
    static final double[][] GAMMA_TN = {
            { 2658, 2916, 3281, 3615, 4073, 4784, 5458, 6226 },
            { 2341, 2605, 2968, 3288, 3721, 4401, 5050, 5841 },
            { 2035, 2310, 2682, 2996, 3404, 4045, 4695, 5514 },
            { 1723, 1972, 2342, 2661, 3058, 3672, 4291, 5098 },
            { 1477, 1697, 2034, 2342, 2730, 3319, 3891, 4604 },
            { 1189, 1402, 1704, 1994, 2372, 2931, 3462, 4142 },
    };
    /** Fig. 8.64b. */
    static final double[][] NEUT_TN = {
            { 2249, 2405, 2612, 2768, 2924, 3132, 3293, 3458 },
            { 2008, 2160, 2365, 2518, 2670, 2873, 3027, 3186 },
            { 1795, 1940, 2138, 2285, 2433, 2631, 2778, 2931 },
            { 1562, 1706, 1900, 2045, 2187, 2379, 2525, 2675 },
            { 1353, 1494, 1681, 1822, 1963, 2152, 2295, 2444 },
            { 1124, 1262, 1448, 1586, 1725, 1911, 2053, 2199 },
    };
    /** Table 8.37, gamma correction for contact surface bursts. */
    static final double[] SURF_G_KT = { 1, 50, 100, 300, 700, 2000, 5000,
            20000 };
    static final double[] SURF_G = { 2.0 / 3.0, 2.0 / 3.0, 1, 1.25, 1.5, 2,
            3, 3 };
    static final double SURF_N = 0.5;

    /** The ring's dose, rad: FEMA Fig. 7 "radiation injuries (100 rad)". */
    public static final double RAD_RING_RAD = 100;

    public static double surfaceGammaFactor(double kt) {
        if (kt <= SURF_G_KT[0])
            return SURF_G[0];
        double[] lk = new double[SURF_G_KT.length];
        for (int i = 0; i < lk.length; i++)
            lk[i] = Math.log(SURF_G_KT[i]);
        return lin(lk, SURF_G, Math.log(Math.min(kt, 20000)));
    }

    /** Total initial dose (rad) at slant range r metres, unshielded. */
    public static double initialDose(double kt, double rM, boolean surface) {
        double g = gammaDose(kt, rM);
        double n = neutronDose(kt, rM);
        if (surface) {
            g *= surfaceGammaFactor(kt);
            n *= SURF_N;
        }
        return g + n;
    }

    /** Air-burst gamma dose (rad) at slant range rM: Fig. 8.33a or b. */
    static double gammaDose(double kt, double rM) {
        return figDose(GAMMA_FIS, GAMMA_TN, kt, rM);
    }

    /** Air-burst neutron dose (rad) at slant range rM: Fig. 8.64a or b. */
    static double neutronDose(double kt, double rM) {
        return figDose(NEUT_FIS, NEUT_TN, kt, rM);
    }

    /**
     * Fission figure up to 100 kt, thermonuclear above. The thermonuclear
     * curves start slightly below the fission ones at 100 kt, so just above
     * 100 kt the larger of the two (fission held at 100 kt) is used: the
     * rings never shrink as the yield rises.
     */
    private static double figDose(double[][] fis, double[][] tn, double kt,
            double rM) {
        double ry = rM / M_PER_YD;
        if (kt <= 100)
            return dose(fis, FIS_KT, kt, ry);
        return Math.max(dose(tn, TN_KT, kt, ry), dose(fis, FIS_KT, 100, ry));
    }

    /** Distance at which the initial dose falls to {@code rad}. */
    public static double radiationM(double kt, Burst b, double rad) {
        if (b == Burst.UNKNOWN)
            return Math.max(radiationM(kt, Burst.AIR, rad),
                    radiationM(kt, Burst.SURFACE, rad));
        boolean surface = b == Burst.SURFACE;
        double lo = 1, hi = 20000;
        for (int i = 0; i < 80; i++) {
            double mid = 0.5 * (lo + hi);
            if (initialDose(kt, mid, surface) > rad)
                lo = mid;
            else
                hi = mid;
        }
        return lo;
    }

    public static double radiationM(double kt, Burst b) {
        return radiationM(kt, b, RAD_RING_RAD);
    }

    /** Dose at range ry (yards) from one figure: log dose linear in range. */
    private static double dose(double[][] fig, double[] ks, double kt,
            double ry) {
        int n = RAD_D.length;
        double[] r = new double[n];
        for (int i = 0; i < n; i++)
            r[i] = linLog(ks, fig[i], kt);
        // r[i] decreases with dose; pick the bracketing segment (extend the
        // end segments beyond the plotted doses).
        int k = 0;
        while (k < n - 2 && ry < r[k + 1])
            k++;
        double a = r[k], b = r[k + 1];
        double la = Math.log(RAD_D[k]), lb = Math.log(RAD_D[k + 1]);
        if (a == b)
            return RAD_D[k];
        return Math.exp(la + (lb - la) * (ry - a) / (b - a));
    }

    // ------------------------------------------------------------------
    // Fallout: Table 9.93

    /** H+1 reference dose rates drawn, rad/h (subset of Table 9.93). */
    public static final int[] FALLOUT_RATES = { 1000, 100, 10 };
    /** All Table 9.93 rows: rate, downwind a, b(exp 0.45); width c, d; gz e, f. */
    static final double[][] T993 = {
            { 3000, 0.95, 0.0076, 0.86, 0.026, 0.58 },
            { 1000, 1.8, 0.036, 0.76, 0.060, 0.57 },
            { 300, 4.5, 0.13, 0.66, 0.20, 0.48 },
            { 100, 8.9, 0.38, 0.60, 0.39, 0.42 },
            { 30, 16, 0.76, 0.56, 0.53, 0.41 },
            { 10, 24, 1.4, 0.53, 0.68, 0.41 },
            { 3, 30, 2.2, 0.50, 0.89, 0.41 },
            { 1, 40, 3.3, 0.48, 1.5, 0.41 },
    };
    static final double DOWNWIND_EXP = 0.45;
    public static final double WIND_REF_MPH = 15;
    public static final double WIND_MIN_MPH = 8, WIND_MAX_MPH = 45;

    /** Wind factor on downwind distance (§9.96-9.97). */
    public static double windFactor(double mph) {
        double v = Math.max(WIND_MIN_MPH, Math.min(WIND_MAX_MPH, mph));
        return v >= WIND_REF_MPH ? 1 + (v - WIND_REF_MPH) / 60.0
                : 1 + (v - WIND_REF_MPH) / 30.0;
    }

    /** Idealized contour dimensions in metres. */
    public static final class Contour {
        public final int rate;
        public final double downwindM, maxWidthM, gzWidthM;

        Contour(int rate, double downwindM, double maxWidthM,
                double gzWidthM) {
            this.rate = rate;
            this.downwindM = downwindM;
            this.maxWidthM = maxWidthM;
            this.gzWidthM = gzWidthM;
        }

        /** Upwind extent: half the ground-zero width (§9.93). */
        public double upwindM() {
            return gzWidthM / 2;
        }
    }

    /** Table 9.93 contour for an H+1 rate in the table; null otherwise. */
    public static Contour contour(double kt, int rate, double windMph) {
        for (double[] row : T993) {
            if ((int) row[0] != rate)
                continue;
            double down = row[1] * Math.pow(kt, DOWNWIND_EXP)
                    * windFactor(windMph);
            double maxW = row[2] * Math.pow(kt, row[3]);
            double gzW = row[4] * Math.pow(kt, row[5]);
            // At low yields the ground-zero width can exceed the "maximum".
            maxW = Math.max(maxW, gzW);
            return new Contour(rate, down * M_PER_MI, maxW * M_PER_MI,
                    gzW * M_PER_MI);
        }
        return null;
    }

    /** Where along the axis the contour is widest (after Fig. 9.93). */
    static final double WIDEST_AT = 0.8;
    static final int CAP_STEPS = 12, UPWIND_STEPS = 12;

    /**
     * Outline in local metres: x downwind, y to the left; GZ at origin.
     * Closed (first point not repeated). Upwind semicircle, straight
     * widening sides to the widest point, elliptical cap to the tip.
     */
    public static double[][] outline(Contour c) {
        double L = c.downwindM, h0 = c.gzWidthM / 2, h1 = c.maxWidthM / 2;
        double xw = WIDEST_AT * L;
        java.util.List<double[]> right = new java.util.ArrayList<>();
        // Right side (y < 0) from GZ to the tip.
        right.add(new double[] { 0, -h0 });
        right.add(new double[] { xw, -h1 });
        for (int i = 1; i < CAP_STEPS; i++) {
            double t = (Math.PI / 2) * i / CAP_STEPS;
            right.add(new double[] { xw + (L - xw) * Math.sin(t),
                    -h1 * Math.cos(t) });
        }
        java.util.List<double[]> pts = new java.util.ArrayList<>(right);
        pts.add(new double[] { L, 0 });
        for (int i = right.size() - 1; i >= 0; i--)
            pts.add(new double[] { right.get(i)[0], -right.get(i)[1] });
        // Upwind semicircle of radius h0 from (0, h0) round to (0, -h0).
        for (int i = 1; i < UPWIND_STEPS; i++) {
            double t = Math.PI / 2 + Math.PI * i / UPWIND_STEPS;
            pts.add(new double[] { h0 * Math.cos(t), h0 * Math.sin(t) });
        }
        return pts.toArray(new double[0][]);
    }

    /** Decay of H+1 rate to time t hours (t^-1.2, §9.15). */
    public static double decay(double hours) {
        return Math.pow(Math.max(hours, 0.5), -1.2);
    }

    /** "50 miles": FEMA 3rd ed. p. 44 initial Get Inside area. */
    public static final double GET_INSIDE_M = 50 * M_PER_MI;

    // ------------------------------------------------------------------
    // Interpolation helpers

    /** Log-log interpolation; ends extrapolated along the end segments. */
    static double logLog(double[] xs, double[] ys, double x) {
        double[] lx = new double[xs.length], ly = new double[ys.length];
        for (int i = 0; i < xs.length; i++) {
            lx[i] = Math.log(xs[i]);
            ly[i] = Math.log(ys[i]);
        }
        return Math.exp(lin(lx, ly, Math.log(x)));
    }

    /** y linear in log x; ends extrapolated along the end segments. */
    static double linLog(double[] xs, double[] ys, double x) {
        double[] lx = new double[xs.length];
        for (int i = 0; i < xs.length; i++)
            lx[i] = Math.log(xs[i]);
        return lin(lx, ys, Math.log(x));
    }

    /** Piecewise-linear, xs ascending, extrapolating the end segments. */
    static double lin(double[] xs, double[] ys, double x) {
        int n = xs.length;
        int i = 0;
        if (x >= xs[n - 1])
            i = n - 2;
        else
            while (i < n - 2 && x > xs[i + 1])
                i++;
        double t = (x - xs[i]) / (xs[i + 1] - xs[i]);
        return ys[i] + (ys[i + 1] - ys[i]) * t;
    }

    /** True if the yield is inside a figure's plotted range (no extrapolation). */
    public static boolean extrapolated(double kt) {
        return kt < 1 || kt > 10000;
    }

    public static boolean validYield(double kt) {
        return !Double.isNaN(kt) && kt >= MIN_KT && kt <= MAX_KT;
    }
}
