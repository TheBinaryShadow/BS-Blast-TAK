package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.List;

public class UxoTextTest {

    private static UxoRecord rec(String cat, boolean multi) {
        return UxoRecord.create(cat, multi, false, UxoRecord.Where.PATH,
                UxoRecord.Placement.MAP, "ALPHA-1", 1793544725000L)
                .withNumber(1);
    }

    @Test
    public void feetRoundUpToTen() {
        assertEquals(990, UxoText.cordonFt(300)); // 984.25 ft
        assertEquals(1240, UxoText.cordonFt(375)); // 1,230.3 ft
        assertEquals(1560, UxoText.cordonFt(475)); // 1,558.4 ft
        assertEquals(1970, UxoText.cordonFt(600)); // 1,968.5 ft
        assertEquals(2220, UxoText.cordonFt(675)); // 2,214.6 ft
        assertEquals(2630, UxoText.cordonFt(800)); // 2,624.7 ft
        for (int m = 300; m < 30000; m += 7)
            assertTrue(UxoText.cordonFt(m) * 0.3048 >= m - 1e-9);
        assertEquals("375 m (1,240 ft)", UxoText.cordon(375));
    }

    @Test
    public void ringTitleAndBasis() {
        UxoRecord r = rec("PROJ_MORTAR", false);
        assertEquals("UXO CORDON 375 m (1,240 ft) MIN - fragments may go "
                + "farther", UxoText.ringTitle(r));
        assertTrue(UxoText.ringTitle(r.cleared("B", 2)).startsWith(
                "CLEARED UXO CORDON"));
        assertEquals("Projectiles and Mortars, 375 m. ATP 4-32.2 Fig. 31, "
                + "01 Mar 2024.", UxoText.basis(r));
        assertTrue(UxoText.basis(rec("PROJ_MORTAR", true))
                .contains("+50 m for more than one item"));
        assertTrue(UxoText.basis(rec("UNKNOWN", false))
                .contains("300 m minimum"));
    }

    @Test
    public void remarksBlockIsMergeable() {
        UxoRecord r = rec("BOMB_DISPENSER", false);
        String block = UxoText.remarks(r);
        assertTrue(block.startsWith("[BLAST-UXO] Aircraft bomb or dispenser."));
        assertTrue(block.contains("Minimum cordon 675 m / 2,220 ft"));
        assertTrue(block.contains("Fragments may travel farther."));
        assertTrue(block.endsWith(Labels.REMARKS_END));
        String mine = "Seen by farmer.\n\n";
        String merged = UxoText.mergeRemarks(mine + block + "\nMore notes",
                r.cleared("B", 2));
        assertTrue(merged.startsWith(mine + "[BLAST-UXO] CLEARED."));
        assertTrue(merged.endsWith("\nMore notes"));
        // IED merge never touches the UXO block and vice versa.
        assertEquals(-1, block.indexOf(Labels.REMARKS_PREFIX));
    }

    @Test
    public void reportDetailsReachTheRemarks() {
        UxoRecord r = rec("PROJ_MORTAR", false).withReport(
                new UxoRecord.Report(1, "Rusty shell", "By the barn", "", "",
                        "", "", "", UxoRecord.Priority.MINOR, ""));
        String block = UxoText.remarks(r);
        assertTrue(block.contains("Description: Rusty shell."));
        assertTrue(block.contains("Location: By the barn."));
        assertTrue(block.contains("Priority: Minor."));
    }

    @Test
    public void drillsMatchTheSituation() {
        assertTrue(UxoText.drill(UxoRecord.Where.PATH)
                .contains("Leave the way you came"));
        String area = UxoText.drill(UxoRecord.Where.AREA);
        assertTrue(area.contains("don't move your feet"));
        assertTrue(area.contains("Don't try to retrace your footsteps"));
        assertFalse(area.contains("Leave the way you came"));
        assertTrue(UxoText.drill(UxoRecord.Where.VEHICLE)
                .contains("Don't reverse"));
        assertEquals("", UxoText.drill(UxoRecord.Where.UNSET));
    }

    @Test
    public void doNowHasTheRightExtras() {
        List<String> base = UxoText.doNow(rec("PROJ_MORTAR", false));
        assertTrue(join(base).contains("within 50 m"));
        assertFalse(join(base).contains("nose or tail"));
        assertTrue(join(UxoText.doNow(rec("MISSILE_ROCKET", false)))
                .contains("nose or tail"));
        assertTrue(join(UxoText.doNow(rec("LARGE_GUIDED_MISSILE", false)))
                .contains("nose or tail"));
        assertTrue(join(UxoText.doNow(rec("GREN_MINE_SUBMUN", false)))
                .contains("expect more"));
        assertTrue(join(UxoText.doNow(rec("STOCKPILE", false)))
                .contains("burning"));
        assertTrue(join(UxoText.doNow(rec("PROJ_MORTAR", true)))
                .contains("spread out"));
    }

    @Test
    public void positionCheck() {
        UxoText.You in = UxoText.check(375, 210, 5);
        assertTrue(in.inside);
        assertEquals(165, in.toCordonM, 1e-9);
        assertEquals("689 ft (210 m) from the item: INSIDE the 375 m cordon.",
                in.headline(375));
        assertTrue(in.guidance(375).startsWith("Move out to at least 375 m"));
        assertNull(in.boundaryWarning());
        UxoText.You out = UxoText.check(375, 380, 15);
        assertFalse(out.inside);
        assertTrue(out.nearBoundary);
        assertNotNull(out.boundaryWarning());
        assertFalse(UxoText.check(375, 400, 15).nearBoundary);
        assertTrue(UxoText.check(375, 380, Double.NaN).nearBoundary);
        assertFalse(UxoText.check(375, 375, 0).inside);
        assertNull(UxoText.check(375, Double.NaN, 5));
        assertNull(UxoText.check(0, 10, 5));
        assertArrayEquals(new int[] { 2, 1 }, UxoText.count(375,
                new double[] { 10, Double.POSITIVE_INFINITY, 375, Double.NaN }));
        assertArrayEquals(new int[] { 0, 0 }, UxoText.count(375, null));
    }

    @Test
    public void nineLine() {
        UxoRecord r = UxoRecord.create("PROJ_MORTAR", true, false,
                UxoRecord.Where.PATH, UxoRecord.Placement.RB, "ALPHA-1",
                1793544725000L).withReport(new UxoRecord.Report(2,
                        "Rusty, fins", "Ditch by bridge", "Projected", "", "",
                        "Road closed", "", UxoRecord.Priority.INDIRECT, ""));
        String s = UxoText.nineLine(r, "ALPHA-1", "18S UJ 23371 06519");
        String[] lines = s.split("\n");
        assertEquals("EOD 9-LINE (UXO)", lines[0]);
        assertTrue(lines[1].startsWith("1. DTG: "));
        assertTrue(lines[1].endsWith("Z NOV 26"));
        assertEquals("2. UNIT/LOCATION: ALPHA-1, 18S UJ 23371 06519 (approx.), "
                + "Ditch by bridge", lines[2]);
        assertEquals("3. CONTACT: ALPHA-1", lines[3]);
        assertEquals("4. TYPE: Projectiles and Mortars, Projected, 2 items, "
                + "Rusty, fins (observed without touching, disturbing or "
                + "approaching)", lines[4]);
        assertEquals("5. CBRN: None seen", lines[5]);
        assertEquals("6. RESOURCES THREATENED: -", lines[6]);
        assertEquals("7. IMPACT ON MISSION: Road closed", lines[7]);
        assertEquals("8. PROTECTIVE MEASURES: Cordon 425 m (1,400 ft) advised",
                lines[8]);
        assertEquals("9. RECOMMENDED PRIORITY: INDIRECT", lines[9]);
        assertEquals(10, lines.length);
    }

    @Test
    public void simpleReport() {
        UxoRecord r = rec("GREN_MINE_SUBMUN", false);
        String s = UxoText.simpleReport(r, "18S UJ 1 2");
        assertTrue(s.startsWith("SUSPECTED UNEXPLODED ORDNANCE\nWHAT: "
                + "Grenade, landmine or bomblet\n"));
        assertTrue(s.contains("WHERE: 18S UJ 1 2\n"));
        assertTrue(s.contains("MEASURES: cordon 300 m (990 ft) advised"));
        assertTrue(s.endsWith("Nobody has touched or moved it."));
        assertTrue(UxoText.simpleReport(r, null).contains("(no position)"));
    }

    @Test
    public void referenceNumbersMatchTheTables() {
        StringBuilder all = new StringBuilder();
        for (UxoReference.Section s : UxoReference.sections()) {
            assertFalse(s.title.isEmpty());
            for (String p : s.body)
                all.append(p).append('\n');
        }
        String t = all.toString();
        for (UxoTable.Category c : UxoTable.categories())
            assertTrue(c.id, t.contains("(" + c.cordonM + " m)"));
        assertTrue(t.contains(UxoReference.IMAS_CREDIT));
        // 155 mm example: DESR V3.E3.T3 HFD 415 ft / 126.5 m.
        assertTrue(t.contains("127 m (415 ft)"));
        assertTrue(t.contains("204 m (670 ft)"));
        assertEquals(126.5, Formats.ftToM(415), 0.05);
        assertEquals(204.2, Formats.ftToM(670), 0.05);
        // 155 mm and Mk 82 maximum fragment ranges from DESR EQN V5.E3.T1-3.
        assertEquals(821, robustMfdM(155), 1);
        assertEquals(1194, robustMfdM(273.05), 1);
    }

    /** DESR 6055.09 Table V5.E3.T1, robust munitions, metric (EQN T1-3). */
    private static double robustMfdM(double diameterMm) {
        double l = Math.log(diameterMm);
        return Math.exp(2.57561 + 0.961245 * l - 0.028054 * l * l);
    }

    @Test
    public void desrEquationReproducesTheTable() {
        assertEquals(371.8, robustMfdM(50.80), 0.05);
        assertEquals(811.2, robustMfdM(152.40), 0.05);
        assertEquals(1139.3, robustMfdM(254.00), 0.05);
        assertEquals(1537.2, robustMfdM(406.40), 0.05);
    }

    @Test
    public void namesAndNumbers() {
        assertEquals("UXO 2 Artillery shell or mortar",
                Names.initialUxo(2, "Artillery shell or mortar"));
        assertEquals(2, Names.uxoNumberIn("UXO 2 Artillery shell or mortar"));
        assertEquals(0, Names.uxoNumberIn("BLAST 2 Car"));
        assertEquals(0, Names.numberIn("UXO 2 Car"));
        assertEquals(7, Names.uxoNumberIn("UXO 7 CLEARED"));
    }

    private static String join(List<String> l) {
        StringBuilder sb = new StringBuilder();
        for (String s : l)
            sb.append(s).append('\n');
        return sb.toString();
    }
}
