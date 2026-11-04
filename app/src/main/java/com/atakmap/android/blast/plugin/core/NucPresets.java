package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Yield presets for the nuclear reference mode. Weapon yields are public,
 * unofficial estimates (cited per entry); ranged estimates use their upper
 * value so the rings are not understated. Presets are only a convenience:
 * the user can type any yield.
 */
public final class NucPresets {

    public enum Group {
        PLANNING("Planning scenarios"), HISTORICAL("Historical detonations"),
        FIELDED("Fielded weapons (public estimates)"), YIELD("By yield");

        public final String label;

        Group(String label) {
            this.label = label;
        }
    }

    public static final class Preset {
        public final String id;
        public final Group group;
        /** Short label, e.g. "US W76-1 (Trident II)". */
        public final String label;
        public final double kt;
        /** How the estimate is stated, e.g. "90 kt" or "10-100 kt (upper)". */
        public final String note;
        public final String source;

        Preset(String id, Group group, String label, double kt, String note,
                String source) {
            this.id = id;
            this.group = group;
            this.label = label;
            this.kt = kt;
            this.note = note;
            this.source = source;
        }
    }

    static final String FEMA = "FEMA Planning Guidance, 3rd ed. (2022)";
    static final String NN_US = "Kristensen et al., Nuclear Notebook: United "
            + "States nuclear weapons, 2025 (Bull. At. Sci.)";
    static final String NN_RU = "Kristensen et al., Nuclear Notebook: Russian "
            + "nuclear weapons, 2026 (Bull. At. Sci.)";
    static final String NN_CN = "Kristensen et al., Nuclear Notebook: Chinese "
            + "nuclear weapons, 2026 (Bull. At. Sci.)";
    static final String NN_UK = "Kristensen et al., Nuclear Notebook: United "
            + "Kingdom nuclear weapons, 2024 (Bull. At. Sci.)";
    static final String SIPRI_FR = "SIPRI Yearbook 2023, French nuclear forces";
    static final String NN_SA = "Nuclear Notebook: Indian (2026) and Pakistani "
            + "(2023) nuclear weapons (Bull. At. Sci.)";
    static final String NN_KP = "Nuclear Notebook: North Korean nuclear "
            + "weapons, 2024 (Bull. At. Sci.)";
    static final String LANL = "LANL LA-8819 (Hiroshima, Nagasaki)";

    private static final List<Preset> ALL;

    static {
        List<Preset> l = new ArrayList<>();
        Group P = Group.PLANNING, H = Group.HISTORICAL, F = Group.FIELDED,
                Y = Group.YIELD;
        l.add(new Preset("FEMA_0.1", P, "Improvised device, 0.1 kT", 0.1,
                "0.1 kT", FEMA));
        l.add(new Preset("FEMA_1", P, "Improvised device, 1 kT", 1, "1 kT",
                FEMA));
        l.add(new Preset("FEMA_10", P, "Improvised device, 10 kT (FEMA's "
                + "main scenario)", 10, "10 kT", FEMA));
        l.add(new Preset("HIROSHIMA", H, "Hiroshima, 1945", 15,
                "about 15 kt (air burst)", LANL));
        l.add(new Preset("NAGASAKI", H, "Nagasaki, 1945", 21,
                "about 21 kt (air burst)", LANL));
        l.add(new Preset("BRAVO", H, "Castle Bravo test, 1954", 15000,
                "about 15 Mt (surface)", "Bulletin of the Atomic Scientists, "
                        + "2021"));

        l.add(new Preset("US_W76_2", F, "US W76-2 (Trident II, low yield)", 8,
                "8 kt", NN_US));
        l.add(new Preset("US_B61_12", F, "US B61-12 bomb", 50,
                "0.3-50 kt (upper)", NN_US));
        l.add(new Preset("US_W76_1", F, "US W76-1 (Trident II)", 90, "90 kt",
                NN_US));
        l.add(new Preset("US_W80_1", F, "US W80-1 (air-launched cruise "
                + "missile)", 150, "5-150 kt (upper)", NN_US));
        l.add(new Preset("US_W87", F, "US W87 (Minuteman III)", 300, "300 kt",
                NN_US));
        l.add(new Preset("US_W78", F, "US W78 (Minuteman III)", 335, "335 kt",
                NN_US));
        l.add(new Preset("US_W88", F, "US W88 (Trident II)", 455, "455 kt",
                NN_US));
        l.add(new Preset("RU_ISKANDER", F, "Russia Iskander-M", 100,
                "10-100 kt (upper)", NN_RU));
        l.add(new Preset("RU_YARS", F, "Russia Yars (RS-24) / Bulava", 100,
                "100 kt", NN_RU));
        l.add(new Preset("RU_ORESHNIK", F, "Russia Oreshnik", 250, "250 kt",
                NN_RU));
        l.add(new Preset("RU_SARMAT", F, "Russia Sarmat (RS-28)", 500,
                "500 kt (uncertain)", NN_RU));
        l.add(new Preset("RU_TOPOL_M", F, "Russia Topol-M / RS-20V", 800,
                "800 kt (RS-20V 500-800)", NN_RU));
        l.add(new Preset("CN_425", F, "China DF-41 / DF-31 / DF-26 / JL-3",
                425, "425 kt", NN_CN));
        l.add(new Preset("CN_JL2", F, "China JL-2", 700, "700 kt", NN_CN));
        l.add(new Preset("CN_DF5A", F, "China DF-5A", 4500, "4.5 Mt", NN_CN));
        l.add(new Preset("UK_TRIDENT", F, "UK Trident II", 100, "100 kt",
                NN_UK));
        l.add(new Preset("FR_M51", F, "France M51 (TN 75 / TNO)", 100,
                "100 kt", SIPRI_FR));
        l.add(new Preset("SA_MISSILE", F, "India / Pakistan missiles", 40,
                "10-40 kt (upper)", NN_SA));
        l.add(new Preset("KP", F, "North Korea", 20, "10-20 kt (upper)",
                NN_KP));

        double[] ys = { 0.1, 1, 10, 20, 50, 100, 300, 500, 1000, 5000, 10000,
                20000 };
        for (double y : ys)
            l.add(new Preset("Y_" + Formats.decimalTrim(y), Y, yieldLabel(y), y,
                    yieldLabel(y), ""));
        ALL = Collections.unmodifiableList(l);
    }

    private NucPresets() {
    }

    public static List<Preset> all() {
        return ALL;
    }

    public static List<Preset> group(Group g) {
        List<Preset> out = new ArrayList<>();
        for (Preset p : ALL)
            if (p.group == g)
                out.add(p);
        return out;
    }

    public static Preset byId(String id) {
        if (id == null)
            return null;
        for (Preset p : ALL)
            if (p.id.equals(id))
                return p;
        return null;
    }

    /** "15 kt", "0.1 kt", "1.2 Mt". */
    public static String yieldLabel(double kt) {
        if (kt >= 1000)
            return Formats.decimalTrim(kt / 1000.0) + " Mt";
        return Formats.decimalTrim(kt) + " kt";
    }
}
