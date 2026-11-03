package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class UxoCodecTest {

    private static UxoRecord sample() {
        UxoRecord r = UxoRecord.create("PROJ_MORTAR", true, false,
                UxoRecord.Where.PATH, UxoRecord.Placement.RB, "ALPHA-1",
                1793544725000L).withNumber(3);
        return r.withReport(new UxoRecord.Report(2, "Rusty 'shell' & fins <1 m>",
                "Ditch by the bridge", "Projected", "", "Road", "Traffic stopped",
                "Cordon 425 m, people kept out", UxoRecord.Priority.INDIRECT,
                "ALPHA-1 ch 3"));
    }

    @Test
    public void roundTrip() {
        UxoRecord r = sample();
        String xml = UxoCodec.toXml(r);
        assertTrue(xml.startsWith("<__blast_uxo "));
        UxoRecord d = UxoCodec.fromXml(xml);
        assertNotNull(d);
        assertEquals("PROJ_MORTAR", d.categoryId);
        assertEquals("Projectiles and Mortars", d.categoryLabel);
        assertEquals(425, d.cordonM);
        assertTrue(d.multiple);
        assertFalse(d.large);
        assertEquals(UxoRecord.Where.PATH, d.where);
        assertEquals(UxoRecord.Placement.RB, d.placement);
        assertEquals("ALPHA-1", d.byCallsign);
        assertEquals(1793544725000L, d.timeMillis);
        assertEquals(3, d.number);
        assertEquals(2, d.report.count);
        assertEquals("Rusty 'shell' & fins <1 m>", d.report.description);
        assertEquals("Ditch by the bridge", d.report.landmark);
        assertEquals("Projected", d.report.emplacement);
        assertEquals(UxoRecord.Priority.INDIRECT, d.report.priority);
        assertEquals("ALPHA-1 ch 3", d.report.contact);
        assertFalse(d.receivedMismatch);
        assertEquals(xml, UxoCodec.toXml(d));
        // Clear and reopen keep the report and number.
        UxoRecord c = d.cleared("BRAVO", 1793548325000L);
        UxoRecord c2 = UxoCodec.fromXml(UxoCodec.toXml(c));
        assertTrue(c2.isCleared());
        assertEquals("BRAVO", c2.clearedBy);
        assertEquals(3, c2.number);
        assertEquals(2, c2.report.count);
        assertFalse(UxoCodec.fromXml(UxoCodec.toXml(c2.reopened()))
                .isCleared());
    }

    @Test
    public void tamperedCordonIsIgnoredForKnownEdition() {
        Map<String, String> a = UxoCodec.encode(sample());
        a.put("cord_m", "5");
        UxoRecord d = UxoCodec.decode(a);
        assertEquals(425, d.cordonM);
        assertTrue(d.receivedMismatch);
        a.put("lbl", "Tiny thing");
        assertEquals("Projectiles and Mortars", UxoCodec.decode(a).categoryLabel);
        // "large" can't enlarge or shrink a typed category.
        a.put("large", "1");
        assertEquals(425, UxoCodec.decode(a).cordonM);
    }

    @Test
    public void unknownCategoryInKnownEditionIsRejected() {
        Map<String, String> a = UxoCodec.encode(sample());
        a.put("cat", "TRUCK");
        assertNull(UxoCodec.decode(a));
        a.remove("cat");
        assertNull(UxoCodec.decode(a));
    }

    @Test
    public void unknownEditionUsesCarriedCordonWithinBounds() {
        Map<String, String> a = new LinkedHashMap<>();
        a.put("src", "ATP4-32.2");
        a.put("ed", "2030-01-01");
        a.put("cat", "NEW_TYPE");
        a.put("lbl", "New type");
        a.put("cord_m", "900");
        UxoRecord d = UxoCodec.decode(a);
        assertNotNull(d);
        assertEquals(900, d.cordonM);
        assertFalse(d.isKnownEdition());
        assertEquals("New type", d.plainLabel());
        a.put("cord_m", "299");
        assertNull(UxoCodec.decode(a));
        a.put("cord_m", "30001");
        assertNull(UxoCodec.decode(a));
        a.put("cord_m", "abc");
        assertNull(UxoCodec.decode(a));
    }

    @Test
    public void unknownItemLargeFlag() {
        UxoRecord u = UxoRecord.create("UNKNOWN", false, true,
                UxoRecord.Where.AREA, UxoRecord.Placement.MAP, "X", 1);
        assertEquals(675, u.cordonM);
        UxoRecord d = UxoCodec.fromXml(UxoCodec.toXml(u));
        assertTrue(d.large);
        assertEquals(675, d.cordonM);
        // large is dropped for typed categories on create.
        assertFalse(UxoRecord.create("PROJ_MORTAR", false, true,
                UxoRecord.Where.PATH, UxoRecord.Placement.MAP, "X", 1).large);
    }

    @Test
    public void unknownAttributesAreKeptAndBounded() {
        Map<String, String> a = UxoCodec.encode(sample());
        a.put("future", "yes");
        for (int i = 0; i < 200; i++)
            a.put("x" + i, "v");
        a.put("bad name", "v");
        UxoRecord d = UxoCodec.decode(a);
        assertEquals("yes", d.extra.get("future"));
        assertTrue(d.extra.size() <= 64);
        assertFalse(d.extra.containsKey("bad name"));
        assertTrue(UxoCodec.toXml(d).contains("future=\"yes\""));
    }

    @Test
    public void reportTextIsCleanedAndCapped() {
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 500; i++)
            big.append('a');
        Map<String, String> a = UxoCodec.encode(sample());
        a.put("r_desc", "line1\nline2\u0001" + big);
        a.put("r_n", "-4");
        a.put("r_pri", "PANIC");
        a.put("n", "99999999");
        UxoRecord d = UxoCodec.decode(a);
        assertEquals(UxoCodec.MAX_REPORT_TEXT, d.report.description.length());
        assertTrue(d.report.description.startsWith("line1 line2a"));
        assertEquals(0, d.report.count);
        assertEquals(UxoRecord.Priority.UNSET, d.report.priority);
        assertEquals(0, d.number);
    }

    @Test
    public void notConfusedWithIedDetail() {
        String xml = UxoCodec.toXml(sample());
        assertNull(BlastCodec.fromXml(xml));
        assertNull(UxoCodec.fromXml("<__blast v=\"1\" src=\"CISA-BTSOC\" "
                + "ed=\"2025-08\" row=\"CAR\"/>"));
    }

    @Test
    public void fuzzNeverThrows() {
        Random rnd = new Random(42);
        String[] keys = UxoCodec.KNOWN;
        for (int i = 0; i < 20000; i++) {
            Map<String, String> a = new LinkedHashMap<>();
            int n = rnd.nextInt(12);
            for (int k = 0; k < n; k++) {
                String key = rnd.nextInt(5) == 0 ? "z" + rnd.nextInt(9)
                        : keys[rnd.nextInt(keys.length)];
                a.put(key, junk(rnd));
            }
            if (rnd.nextBoolean()) {
                a.put("src", "ATP4-32.2");
                a.put("ed", "2024-03-01");
                a.put("cat", UxoTable.pickerCategories().get(
                        rnd.nextInt(7)).id);
            }
            UxoRecord d = UxoCodec.decode(a);
            if (d != null) {
                assertTrue(d.cordonM >= UxoTable.MIN_CORDON_M);
                assertTrue(d.cordonM <= 30000);
                assertNotNull(UxoCodec.fromXml(UxoCodec.toXml(d)));
                UxoText.remarks(d);
                UxoText.nineLine(d, "X", "grid");
                UxoText.nineLine(d, null, null);
            }
        }
    }

    private static String junk(Random rnd) {
        switch (rnd.nextInt(6)) {
            case 0:
                return "";
            case 1:
                return Integer.toString(rnd.nextInt(100000) - 50000);
            case 2:
                return "1";
            case 3:
                return "&<>\"'\u0000￿";
            case 4:
                return "PATH";
            default: {
                StringBuilder sb = new StringBuilder();
                int len = rnd.nextInt(300);
                for (int i = 0; i < len; i++)
                    sb.append((char) rnd.nextInt(0x3000));
                return sb.toString();
            }
        }
    }
}
