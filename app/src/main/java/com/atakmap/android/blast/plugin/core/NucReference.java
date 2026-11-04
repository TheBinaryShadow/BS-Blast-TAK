package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reference tab sections for the nuclear mode. FEMA, LLNL and Glasstone &amp;
 * Dolan are US government works; numbers are golden-tested against
 * {@link NucModel}.
 */
public final class NucReference {

    public static final String ACTIONS = "NUC_ACTIONS";
    public static final String RINGS = "NUC_RINGS";
    public static final String MIL = "NUC_MIL";
    public static final String NBC = "NUC_NBC";

    private NucReference() {
    }

    private static final double[] TABLE_KT = { 0.1, 1, 10, 100, 1000 };

    /** "0.1 kt: severe 0.11 mi, moderate 0.22 mi, light 0.65 mi". */
    static String zoneLine(double kt) {
        NucModel.Burst s = NucModel.Burst.SURFACE;
        return "• " + NucPresets.yieldLabel(kt) + " surface: severe "
                + NucText.dist(NucModel.sdzM(kt, s)) + ", moderate "
                + NucText.dist(NucModel.mdzM(kt, s)) + ", light "
                + NucText.dist(NucModel.ldzM(kt, s)) + ".";
    }

    private static List<UxoReference.Section> build() {
        List<UxoReference.Section> l = new ArrayList<>();
        l.add(new UxoReference.Section(ACTIONS,
                "Nuclear detonation: what to do",
                "FEMA: \"Get Inside, Stay Inside, Stay Tuned\" is the most "
                        + "important protective action, because it reduces "
                        + "fallout exposure.",
                "• GET INSIDE a basement or the middle of a large, dense "
                        + "building. It is best to be in a shelter when "
                        + "fallout arrives. Any shelter is better than being "
                        + "outside for long.",
                "• STAY INSIDE for 12-24 hours, unless given other "
                        + "instructions. Leave earlier only for fire, "
                        + "building collapse or a medical emergency.",
                "• STAY TUNED: AM/FM radio is best; TV, phone or internet "
                        + "if available.",
                "FEMA gives this message to everyone within 50 miles until "
                        + "fallout areas are identified. Fallout will likely "
                        + "take 10 minutes or more to arrive.",
                "If you see an intense, unexpected flash, take cover at "
                        + "once, away from windows: the blast wave can "
                        + "follow by seconds.",
                "Caught outside while fallout fell? Once inside, remove the "
                        + "outer layer of clothing and shoes (this can remove "
                        + "up to 90% of radioactive material, FEMA 2024) and "
                        + "brush or wipe off exposed skin and hair."));
        List<String> rings = new ArrayList<>();
        rings.add("The damage zones are FEMA's: Severe, Moderate and Light "
                + "Damage Zones end about 0.5, 1 and 3 miles from a 10 kT "
                + "ground-level detonation, and grow about twofold for each "
                + "tenfold increase in yield (cube-root scaling):");
        for (double kt : TABLE_KT)
            rings.add(zoneLine(kt));
        rings.add("Air burst: Blast never asks for a burst height. Each zone "
                + "is enlarged by the ratio of the farthest ground range of "
                + "its overpressure (over all burst heights) to the surface "
                + "range, from Glasstone & Dolan Fig. 3.73c: severe x1.58, "
                + "moderate x1.67, light x2.0 (the light ratio extends "
                + "Glasstone's lowest, 1 psi, curve). Burn and radiation "
                + "rings use the air values with slant range drawn as "
                + "ground range. Each ring is an outer bound; no single "
                + "burst produces all of them. Unknown draws the larger of "
                + "surface and air for every ring, plus fallout.");
        rings.add("Burns: 50% chance of second-degree burns to bare skin with "
                + "a direct view of the fireball, using the most sensitive "
                + "skin curve of Glasstone Fig. 12.64 (about 3.9 cal/cm² at "
                + "10 kt, 5.5 at 1 Mt) and the clear-day (12-mile "
                + "visibility) ranges of Fig. 7.42. A surface burst "
                + "delivers about half the heat of an air burst (thermal "
                + "partition 0.18 vs 0.35).");
        rings.add("Initial radiation: 100 rad (1 Gy) total gamma and neutron "
                + "dose outdoors without shielding (Glasstone Figs. 8.33 and "
                + "8.64; surface bursts corrected by Table 8.37 and §8.65). "
                + "FEMA uses the same 100 rad line for \"radiation "
                + "injuries\". It is received within the first minute.");
        rings.add("Fallout: idealized H+1 reference dose-rate contours for a "
                + "contact surface burst, 100% fission, from Glasstone Table "
                + "9.93, scaled for wind speed (§9.96-9.97, valid about 8-45 "
                + "mph) and drawn downwind of the wind you enter. Real "
                + "fallout follows the winds aloft, which can differ from "
                + "the surface wind. Drawn only for surface or unknown "
                + "bursts.");
        rings.add("Accuracy: blast ±20% (Glasstone §5.142), thermal ±25% "
                + "(§7.85), initial radiation within a factor of 0.5-2 for "
                + "fission and 0.25-1.5 for thermonuclear weapons (Figs. "
                + "8.33a/b).");
        l.add(new UxoReference.Section(RINGS, "Nuclear rings: what they mean",
                rings.toArray(new String[0])));
        l.add(new UxoReference.Section("NUC_SHELTER",
                "Shelter: protection factors",
                "A protection factor (PF) is how many times less radiation "
                        + "you receive inside than outside (LLNL-TR-410067, "
                        + "Table 1):",
                "• Wood-frame house: 2-3. Vehicle: 1.7-2.",
                "• House basement: 10-20.",
                "• Apartment building, lower floors: 10; upper floors "
                        + "(core): 100.",
                "• Concrete blockhouse: 10-150 (9-inch walls), 30-1,000 "
                        + "(12-inch walls). Underground, 3 ft cover: 5,000.",
                "LLNL rates shelter Good at PF 100 (core of a large office "
                        + "building, basement of a multistory building), "
                        + "Adequate at PF 10 (periphery or top floor of "
                        + "office buildings, shallow basements) and "
                        + "Inadequate at PF 3 (cars, light single-story "
                        + "buildings without basements). FEMA calls shelter "
                        + "adequate when it cuts the dose tenfold or more."));
        l.add(new UxoReference.Section("NUC_FALLOUT",
                "Fallout and dose rate",
                "For every sevenfold increase in time after the explosion, "
                        + "the dose rate decreases by a factor of ten "
                        + "(Glasstone & Dolan §9.15): 100 R/h at 1 hour is "
                        + "about 10 R/h at 7 hours and 1 R/h at 2 days.",
                "• Fraction of the H+1 rate left: H+2 0.44, H+6 0.12, H+7 "
                        + "0.10, H+24 0.022, H+48 0.0096.",
                "FEMA's radiation zones are set by measurement, never by a "
                        + "model:",
                "• Dangerous Radiation Zone: above 10 R/h outdoors. Can reach "
                        + "tens of miles downwind in the first hours, then "
                        + "shrinks fast.",
                "• Hot Zone: 0.01 R/h (10 mR/h) to 10 R/h.",
                "Blast's contours are H+1 reference rates. Fallout reaches "
                        + "distant points hours later, already decayed, so "
                        + "a contour is not the Dangerous Radiation Zone.",
                "Lack of visible fallout does not indicate the absence of "
                        + "fallout; appropriate radiation measurements are "
                        + "required (LLNL NARAC). Fallout at lower levels "
                        + "extends well beyond any drawn zone, particularly "
                        + "downwind."));
        l.add(new UxoReference.Section(MIL,
                "Military fallout prediction (Zones I and II)",
                "The military picture is the simplified fallout prediction of "
                        + "FM 3-3-1 (1994) and FM 3-11.3 (2006, Change 1 "
                        + "2009), the last public US versions. Current "
                        + "manuals (TM 3-11.32, NATO ATP-45) are restricted "
                        + "and may differ.",
                "• " + NucReports.ZONE_I,
                "• " + NucReports.ZONE_II,
                "• " + NucReports.OUTSIDE,
                "Method: Zone I downwind distance from FM 3-3-1 Table E-4 "
                        + "(yield and effective wind speed); Zone II is "
                        + "twice Zone I. Radial lines 20° either side of the "
                        + "effective downwind direction (40° sector) unless "
                        + "the EDM gives a wider angle; tangents from the "
                        + "cloud-radius semicircle of the yield group to the "
                        + "Zone I arc; dashed time-of-arrival arcs every "
                        + "effective wind speed (H+1, H+2, ...).",
                "Effective downwind message (EDM) lines: 6 digits = "
                        + "downwind direction and speed (090016 = toward "
                        + "090° at 16 km/h); 9 digits = plus an expanded "
                        + "angle; 3 digits = winds below 8 km/h, the Zone I "
                        + "radius in km, drawn as two circles. Use the line "
                        + "for the yield group (ALFA ≤2 kt, BRAVO ≤5, "
                        + "CHARLIE ≤30, DELTA ≤100, ECHO ≤300, FOXTROT ≤1 MT, "
                        + "GOLF ≤3 MT). EDMs older than 12 hours are not used "
                        + "(FM 3-3-1).",
                "Arrival: fallout may arrive as early as half the estimated "
                        + "time. If none has arrived by twice the estimate "
                        + "(or 12 hours, whichever is earlier), assume the "
                        + "area will not receive fallout.",
                "No fallout predictions are made for air bursts over land. "
                        + "Overlapping predictions take the higher zone. The "
                        + "prediction is an estimate only; its boundaries "
                        + "are not dose-rate contours.",
                "Table E-4 is used as published, except where it prints a "
                        + "smaller distance for a larger yield or wind (a "
                        + "few cells between 6 and 13 km): there the larger "
                        + "neighbouring value is used. Worked examples "
                        + "reproduced: 16 kt at 16 km/h = 18 km (FM 3-3-1); "
                        + "35 kt at 29 km/h ≈ 33 km (FM 3-11.3); 100 kt at 16 "
                        + "knots ≈ 30 nmi (FM 3-3-1)."));
        l.add(new UxoReference.Section(NBC, "NBC 1 NUC observer report",
                "The NBC 1 NUC is the observer's report (FM 3-11.3 Fig. G-3). "
                        + "Mandatory lines: BRAVO (your position and the "
                        + "direction to the burst), DELTA (time), GOLF "
                        + "(delivery), HOTEL (burst type). Optional: FOXTROT "
                        + "(GZ, actual or estimated), JULIET (flash-to-bang), "
                        + "KILO (crater), LIMA (cloud width), YANKEE (wind). "
                        + "ALFA (strike serial number) is assigned by the "
                        + "CBRN cell.",
                "• Do not look at the fireball. After the second shock wave "
                        + "has passed, uncover your eyes and note the time to "
                        + "the nearest minute.",
                "• " + NucReports.FLASH_HELP,
                "• " + NucReports.HOTEL_HELP,
                "• " + NucReports.WIDTH_HELP + " Cloud measurements are not "
                        + "taken after H+10 minutes.",
                "• GZ by polar plot: your position, the azimuth to the stem "
                        + "or cloud, and flash-to-bang x 0.35 km.",
                NucReports.PRECEDENCE));
        l.add(new UxoReference.Section("NUC_RES",
                "Radiation exposure status (military)",
                "Radiation exposure status (RES) by total cumulative dose "
                        + "(JP 3-11, 2008, Fig. D-2):",
                "• R0: 0-0.05 cGy. R1A: 0.05-0.5. R1B: 0.5-5. R1C: 5-10 "
                        + "(priority tasks only). R1D: 10-25 (critical tasks "
                        + "only). R1E: 25-75 (monitor for acute radiation "
                        + "syndrome). R2: 75-125 (further exposure exceeds "
                        + "moderate operational risk). R3: over 125 cGy "
                        + "(all further exposure exceeds emergency risk).",
                "Severity of radiological threat (Fig. D-3): negligible over "
                        + "20 cGy total or 0.01 cGy/h; marginal over 75 cGy "
                        + "or 0.5 cGy/h; critical over 200 cGy or 10 cGy/h; "
                        + "catastrophic over 450 cGy or 200 cGy/h.",
                "Recommended operational exposure guidance, cGy per mission "
                        + "(Fig. D-6), critical / priority / routine: "
                        + "extremely high risk 125 / 75 / 25; high 75 / 25 "
                        + "/ 5; moderate 25 / 5 / 0.5; low 5 / 0.5 / 0.5. "
                        + "The commander may set any OEG.",
                "1 rad = 1 cGy. These are military risk guides. For "
                        + "civilians, FEMA's advice is to shelter and avoid "
                        + "any dose that can be avoided."));
        l.add(new UxoReference.Section("NUC_DOSE",
                "Radiation dose and effects",
                "Approximate short-term whole-body dose effects for adults "
                        + "(LLNL-TR-410067, Table 2; deaths without / with "
                        + "medical treatment, nausea and vomiting within 4 "
                        + "hours):",
                "• 100 rad: deaths under 5% / 0%; symptoms 5-30%.",
                "• 200 rad: under 5% / under 5%; symptoms 60%.",
                "• 300 rad: 30-50% / 15-30%; symptoms 75%.",
                "• 600 rad: 95-100% / 50%; symptoms 100%.",
                "• 1,000 rad: 100% / 90% or more; symptoms 100%.",
                "Children and people with other injuries are at greater "
                        + "risk."));
        List<String> ys = new ArrayList<>();
        ys.add("Yield presets are public, unofficial estimates. Ranged "
                + "estimates use the upper value. Any yield from 0.1 kt to "
                + "50 Mt can be typed instead. Above 10 Mt every ring is extrapolated "
                + "beyond the source charts.");
        for (NucPresets.Preset p : NucPresets.all())
            if (p.group != NucPresets.Group.YIELD)
                ys.add("• " + p.label + ": " + p.note + ". " + p.source + ".");
        l.add(new UxoReference.Section("NUC_YIELDS", "Yield presets",
                ys.toArray(new String[0])));
        l.add(new UxoReference.Section("NUC_SOURCES", "Nuclear sources",
                "• FEMA, Planning Guidance for Response to a Nuclear "
                        + "Detonation, 3rd ed., 2022 (record of change "
                        + "06/12/2023). "
                        + "https://www.fema.gov/sites/default/files/"
                        + "documents/fema_nuc-detonation-planning-guide.pdf",
                "• FEMA, Nuclear Detonation Preparedness: Communicating in "
                        + "the Immediate Aftermath, 2nd ed., 2024.",
                "• Glasstone & Dolan, The Effects of Nuclear Weapons, 3rd "
                        + "ed., US DoD / DOE, 1977. "
                        + "https://www.osti.gov/servlets/purl/6852629",
                "• Buddemeier & Dillon, Key Response Planning Factors for "
                        + "the Aftermath of Nuclear Terrorism, "
                        + "LLNL-TR-410067, 2009.",
                "• FEMA, Planning Guidance, 2nd ed., 2010 (zone "
                        + "overpressures used for the air-burst ratios).",
                "• FM 3-3-1 / FMFM 11-18, Nuclear Contamination Avoidance, "
                        + "9 Sep 1994 (approved for public release): Table "
                        + "E-1, Table E-4, simplified fallout prediction.",
                "• FM 3-11.3 / MCWP 3-37.2A / NTTP 3-11.25 / AFTTP(I) "
                        + "3-2.56, CBRN Contamination Avoidance, 2 Feb 2006, "
                        + "Change 1 30 Apr 2009 (distribution unlimited): "
                        + "Zones I/II, NBC 1/3 NUC formats.",
                "• JP 3-11, Operations in CBRN Environments, 26 Aug 2008, "
                        + "Appendix D.",
                "• LLNL NARAC, Nuclear detonation briefing products "
                        + "(caveat wording).",
                "Glasstone & Dolan curves were read from the 1977 figures; "
                        + "Blast is not affiliated with or endorsed by any "
                        + "of these agencies."));
        return Collections.unmodifiableList(l);
    }

    private static final List<UxoReference.Section> SECTIONS = build();

    public static List<UxoReference.Section> sections() {
        return SECTIONS;
    }

    /** UXO and nuclear sections together, in tab order. */
    public static List<UxoReference.Section> allSections() {
        List<UxoReference.Section> l = new ArrayList<>(
                UxoReference.sections());
        l.addAll(SECTIONS);
        return Collections.unmodifiableList(l);
    }
}
