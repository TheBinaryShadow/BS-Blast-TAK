package com.atakmap.android.blast.plugin.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * The Reference tab: offline text for when there is time to read. Blast's
 * own words, based on the cited public sources; IMAS definitions are quoted
 * with the attribution the United Nations requires. Numbers here are
 * golden-tested against the tables they quote.
 */
public final class UxoReference {

    private UxoReference() {
    }

    /** IMAS copyright notice: required credit for quoted IMAS text. */
    public static final String IMAS_CREDIT =
            "Used and reproduced with permission of the United Nations.";

    public static final class Section {
        public final String id;
        public final String title;
        /** Paragraphs; a line starting with "• " is a bullet. */
        public final List<String> body;

        Section(String id, String title, String... body) {
            this.id = id;
            this.title = title;
            this.body = Collections.unmodifiableList(Arrays.asList(body));
        }
    }

    public static final String RINGS = "RINGS";

    private static final List<Section> SECTIONS = Collections
            .unmodifiableList(Arrays.asList(
                    new Section("FIND", "If you find something",
                            "Recognize, Retreat, Report (US DoD 3Rs):",
                            "• RECOGNIZE: anything that looks like military "
                                    + "ordnance, or is out of place, could "
                                    + "explode. Old, rusty or damaged items "
                                    + "are still dangerous.",
                            "• RETREAT: don't touch, move or disturb it. "
                                    + "Leave carefully the way you came; "
                                    + "don't run.",
                            "• REPORT: call the emergency number (or the "
                                    + "local police, army or mine action "
                                    + "centre) once you are away from it. "
                                    + "Say what you saw, where and when.",
                            "Stay away from hazardous areas and suspicious "
                                    + "objects. Do not touch, do not approach "
                                    + "suspicious objects (IMAS risk "
                                    + "education messages).",
                            "Never compromise safety to identify an item "
                                    + "(ATP 4-32.2). Look from a safe "
                                    + "distance, from cover, with binoculars "
                                    + "or zoom."),
                    new Section("AREA", "On foot in a minefield or bomblet area",
                            "(UNMAS Safety Handbook, paraphrased)",
                            "• Stop. Stay still and don't move your feet.",
                            "• Warn the people around you. Call for help, "
                                    + "but keep others away. Don't rush in "
                                    + "to help an injured person: you will "
                                    + "endanger yourself and others.",
                            "• Look around: mines, tripwires, signs? Find "
                                    + "the nearest surface you know is safe "
                                    + "(paved road, well-used path, "
                                    + "concrete).",
                            "• If there is no safe area you can reach "
                                    + "without stepping on unknown ground, "
                                    + "don't move. Wait for help.",
                            "Retracing your footsteps is not a safe way "
                                    + "out unless you have been trained: "
                                    + "footprints are rarely visible and a "
                                    + "tripwire you missed on the way in can "
                                    + "still be there."),
                    new Section("VEHICLE", "In a vehicle",
                            "(UNMAS Safety Handbook, paraphrased)",
                            "• Stop. Don't reverse and don't turn the "
                                    + "steering wheel.",
                            "• Stay in the vehicle unless it is on fire or "
                                    + "in a life-threatening position. There "
                                    + "are probably more mines nearby.",
                            "• Call for help; use the horn to get attention, "
                                    + "but keep people away.",
                            "• If you must get out, step only into the "
                                    + "vehicle's own wheel tracks, facing "
                                    + "back the way you came, one person at "
                                    + "a time, at least 5 m apart."),
                    new Section("RADIO", "Radios and phones",
                            "Some items can be set off by radio "
                                    + "transmissions. ATAK itself is a "
                                    + "transmitter.",
                            "• ATP 4-32.2: transmit only from at least 100 ft "
                                    + "(30 m).",
                            "• UNMAS Safety Handbook: no radio or phone "
                                    + "transmissions closer than 50 m.",
                            "Blast's rule: get outside the cordon (300 m or "
                                    + "more) first, then report."),
                    new Section("MARK", "Marking",
                            "Mark the area, not the item, and only from where "
                                    + "you already are.",
                            "• Never go closer to place a marker. ATP 4-32.2: "
                                    + "no closer than where you first "
                                    + "recognized it.",
                            "• Never leave a safe path to place a sign, and "
                                    + "never mark individual mines (UNMAS).",
                            "• A map point in Blast is often the safest mark: "
                                    + "place it by tapping the map or by range "
                                    + "and bearing, not by walking to it."),
                    new Section("TYPES", "What am I looking at?",
                            "The six types Blast uses are those of ATP 4-32.2 "
                                    + "Fig. 31. Descriptions are general; many "
                                    + "items look different, and damaged or "
                                    + "buried items are hard to recognize. If "
                                    + "unsure, choose Unknown.",
                            "• Grenade, landmine or bomblet (300 m): "
                                    + "hand-sized or smaller. Grenades: "
                                    + "egg or can shaped, sometimes with a "
                                    + "lever. Mines: round or square, often "
                                    + "partly buried, sometimes with wires. "
                                    + "Bomblets (submunitions): small balls, "
                                    + "cans or cylinders, often with ribbons, "
                                    + "vanes or a small parachute; usually "
                                    + "many spread over an area.",
                            "• Artillery shell or mortar (375 m): shells are "
                                    + "pointed cylinders, often with a "
                                    + "copper band near the base; mortars "
                                    + "are teardrop shaped with tail fins.",
                            "• Rocket or missile (475 m): long tubes with "
                                    + "fins, from shoulder-fired rockets to "
                                    + "artillery rockets (USAF AFVA 32-4022: "
                                    + "often 5 to 8 ft long).",
                            "• Large or guided missile (600 m): large, often "
                                    + "with wings or control fins.",
                            "• Aircraft bomb or dispenser (675 m): large, "
                                    + "with tail fins (AFVA 32-4022: often 3 "
                                    + "to 6 ft long). Dispensers open to "
                                    + "release bomblets.",
                            "• Pile or cache of ordnance (800 m): stacks of "
                                    + "ammunition or several items together.",
                            "• Drone-dropped munitions: often small (grenades "
                                    + "or mortar bombs with added tails); "
                                    + "use the matching size, or Unknown.",
                            "For photographs, see the GICHD Explosive "
                                    + "Ordnance Guide for Ukraine (3rd "
                                    + "edition): gichd.org."),
                    new Section(RINGS, "What the rings mean",
                            "The cordon is the distance ATP 4-32.2 Fig. 31 "
                                    + "gives for each type (01 Mar 2024): the "
                                    + "minimum for people in the open while "
                                    + "the item is left alone. It is not a "
                                    + "safe line. The same publication warns "
                                    + "that fragments may travel farther.",
                            "Example, a 155 mm artillery shell:",
                            "• 127 m (415 ft): DoD hazardous fragment "
                                    + "distance if one shell goes off by "
                                    + "accident (DESR 6055.09, Table "
                                    + "V3.E3.T3).",
                            "• 375 m: Blast's cordon (ATP 4-32.2 Fig. 31).",
                            "• About 820 m: DoD default maximum fragment "
                                    + "range for a 6.1 in (155 mm) robust "
                                    + "munition (DESR 6055.09, Table "
                                    + "V5.E3.T1). Rare fragments can reach "
                                    + "this far.",
                            "Example, a 500 lb Mk 82 bomb: 204 m (670 ft) "
                                    + "accidental-detonation fragment "
                                    + "distance; 675 m cordon; about 1,190 m "
                                    + "maximum fragment range.",
                            "Shaped-charge (anti-tank) items can throw a jet "
                                    + "up to about 1,800 m along their axis "
                                    + "(IMAS TNMA 10.20/01): never stand in "
                                    + "line with the nose or tail.",
                            "Authorities set their own cordons. For WWII "
                                    + "aircraft bombs found in Europe and "
                                    + "Japan they have ranged from about "
                                    + "300 m to 1.5 km. For planned "
                                    + "demolitions, mine action teams keep "
                                    + "the public roughly 0.8 to 1.6 km "
                                    + "away (IMAS TNMA 10.20/01). Always "
                                    + "follow their instructions.",
                            "More than one item: ATP 4-32.2 adds 50 m or "
                                    + "more. Unknown item: the 300 m minimum; "
                                    + "if it looks bomb or missile sized, "
                                    + "Blast uses the bomb distance (675 m)."),
                    new Section("PROS", "How responders work",
                            "The 5-Cs (ATP 4-32.2):",
                            "• CONFIRM from a safe distance, with cover and "
                                    + "binoculars.",
                            "• CLEAR everyone to a safe distance (300 m "
                                    + "minimum).",
                            "• CORDON the area to all foot and vehicle "
                                    + "traffic; set an incident control point.",
                            "• CHECK for more items: 5 m and 25 m checks "
                                    + "around where you stop.",
                            "• CONTROL the cordon until EOD, police or medics "
                                    + "take over.",
                            "The EOD 9-line report (ATP 4-32.2 Table 2) is on "
                                    + "every UXO card (REPORT). Priorities:",
                            "• IMMEDIATE: stops movement or threatens vital "
                                    + "assets.",
                            "• INDIRECT: slows movement or threatens "
                                    + "important assets.",
                            "• MINOR: reduces movement or threatens "
                                    + "non-critical assets.",
                            "• NO THREAT: little or no effect at present.",
                            UxoText.PRIORITY_NOTE),
                    new Section("TERMS", "Terms (IMAS 04.10)",
                            "• Explosive ordnance (EO): mine action's "
                                    + "response to mines, cluster munitions, "
                                    + "unexploded ordnance, abandoned "
                                    + "ordnance, booby traps, other devices "
                                    + "and improvised explosive devices.",
                            "• Unexploded ordnance (UXO): \"explosive "
                                    + "ordnance that has been primed, fuzed, "
                                    + "armed or otherwise prepared for use "
                                    + "or used. It may have been fired, "
                                    + "dropped, launched or projected yet "
                                    + "remains unexploded either through "
                                    + "malfunction or design or for any "
                                    + "other reason.\"",
                            "• Abandoned explosive ordnance (AXO): "
                                    + "\"explosive ordnance that has not been "
                                    + "used during an armed conflict, that "
                                    + "has been left behind or dumped by a "
                                    + "party to an armed conflict, and which "
                                    + "is no longer under control of the "
                                    + "party that left it behind or dumped "
                                    + "it.\"",
                            "• Explosive remnants of war (ERW): \"Unexploded "
                                    + "Ordnance (UXO) and Abandoned Explosive "
                                    + "Ordnance (AXO).\"",
                            "• Mine: \"munition designed to be placed under, "
                                    + "on or near the ground or other surface "
                                    + "area and to be exploded by the "
                                    + "presence, proximity or contact of a "
                                    + "person or a vehicle.\"",
                            "• Unexploded submunition: \"an explosive "
                                    + "submunition that has been dispersed or "
                                    + "released by, or otherwise separated "
                                    + "from, a cluster munition and has "
                                    + "failed to explode as intended.\"",
                            IMAS_CREDIT),
                    new Section("SOURCES", "Sources",
                            "• ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / "
                                    + "AFTTP 3-2.12, Multi-Service TTP for "
                                    + "Explosive Ordnance, 01 Mar 2024 "
                                    + "(Distribution A): Fig. 31 (p. 58), "
                                    + "Table 1 (p. 18), para 3.a (p. 19), "
                                    + "Ch. II warning (p. 11), Table 2 "
                                    + "(p. 26).",
                            "• DESR 6055.09, DoD Explosives Safety Standards, "
                                    + "Edition 1, Change 2, 25 Nov 2025: "
                                    + "Tables V3.E3.T2, V3.E3.T3, V5.E3.T1.",
                            "• UNMAS Landmines, ERW and IED Safety Handbook, "
                                    + "3rd edition, 2015: sections 3.4.4, "
                                    + "4.1, 4.2 (paraphrased).",
                            "• IMAS 04.10 Ed. 2 Am. 12 (2024), IMAS 12.10 "
                                    + "Ed. 2 Am. 3 (2020), TNMA 10.20/01 "
                                    + "(2013), TNMA 12.10/01 (2022).",
                            "• USAF AFVA 32-4022, UXO Recognition and "
                                    + "Reporting Chart, 30 Dec 2015.",
                            "• US DoD 3Rs Explosives Safety Education "
                                    + "(denix.osd.mil/uxo).",
                            "Blast is not endorsed by any of these "
                                    + "organizations.")));

    public static List<Section> sections() {
        return SECTIONS;
    }
}
