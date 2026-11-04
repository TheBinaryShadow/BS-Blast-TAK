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

public class NucCodecTest {

    private static NucRecord sample() {
        return NucRecord.create(10, NucModel.Burst.SURFACE, "FEMA_10", 270,
                15, "ALPHA-1", 1793544725000L).withNumber(2);
    }

    @Test
    public void roundTrip() {
        NucRecord r = sample();
        String xml = NucCodec.toXml(r);
        assertTrue(xml.startsWith("<__blast_nuc "));
        NucRecord d = NucCodec.fromXml(xml);
        assertNotNull(d);
        assertEquals(10, d.yieldKt, 0);
        assertEquals(NucModel.Burst.SURFACE, d.burst);
        assertEquals("FEMA_10", d.presetId);
        assertEquals(NucPresets.byId("FEMA_10").label, d.presetLabel);
        assertEquals(270, d.windFromDeg, 0);
        assertEquals(15, d.windMph, 0);
        assertEquals(90, d.downwindDeg(), 1e-9);
        assertEquals("ALPHA-1", d.byCallsign);
        assertEquals(2, d.number);
        assertTrue(d.isKnownModel());
        assertTrue(d.drawsFallout());
        assertFalse(d.receivedMismatch);
        assertEquals(xml, NucCodec.toXml(d));
        NucRecord c = NucCodec.fromXml(NucCodec.toXml(d.cleared("B", 5)));
        assertTrue(c.isCleared());
        assertEquals("B", c.clearedBy);
        assertFalse(NucCodec.fromXml(NucCodec.toXml(c.reopened())).isCleared());
        NucRecord nw = NucCodec.fromXml(NucCodec.toXml(d.withWind(
                Double.NaN, Double.NaN)));
        assertFalse(nw.hasWind());
        assertFalse(nw.drawsFallout());
    }

    @Test
    public void numbersArePlain() {
        assertEquals("0.1", NucCodec.num(0.1));
        assertEquals("20000", NucCodec.num(20000));
        assertEquals("24.8", NucCodec.num(24.8));
        assertEquals("0", NucCodec.num(0));
    }

    @Test
    public void tamperedRadiiAreRecomputed() {
        Map<String, String> a = NucCodec.encode(sample());
        a.put("ldz_m", "50");
        NucRecord d = NucCodec.decode(a);
        assertEquals(NucModel.ldzM(10, NucModel.Burst.SURFACE), d.radii.ldzM,
                1e-9);
        assertFalse("out-of-order radii are not 'sane'", d.receivedMismatch);
        a.put("ldz_m", "5000");
        assertTrue(NucCodec.decode(a).receivedMismatch);
        // A preset that doesn't match the yield is dropped.
        a.put("pre", "US_W88");
        assertEquals("", NucCodec.decode(a).presetId);
    }

    @Test
    public void badYieldOrModelRejected() {
        Map<String, String> a = NucCodec.encode(sample());
        a.put("y_kt", "0.01");
        assertNull(NucCodec.decode(a));
        a.put("y_kt", "NaN");
        assertNull(NucCodec.decode(a));
        a.put("y_kt", "Infinity");
        assertNull(NucCodec.decode(a));
        a.put("y_kt", "50000");
        assertNull(NucCodec.decode(a));
        a = NucCodec.encode(sample());
        a.remove("mdl");
        assertNull(NucCodec.decode(a));
    }

    @Test
    public void unknownModelUsesCarriedRadiiWithinBounds() {
        Map<String, String> a = NucCodec.encode(sample());
        a.put("ed", "2030-01");
        NucRecord d = NucCodec.decode(a);
        assertNotNull(d);
        assertFalse(d.isKnownModel());
        assertFalse(d.drawsFallout());
        assertEquals(Math.round(NucModel.sdzM(10, NucModel.Burst.SURFACE)),
                Math.round(d.radii.sdzM));
        a.put("sdz_m", "1");
        assertNull(NucCodec.decode(a));
        a.put("sdz_m", "900000");
        assertNull(NucCodec.decode(a));
        a.put("sdz_m", "800");
        a.put("mdz_m", "700");
        assertNull("zones out of order", NucCodec.decode(a));
    }

    @Test
    public void badWindIsDropped() {
        Map<String, String> a = NucCodec.encode(sample());
        a.put("wfrom", "400");
        assertFalse(NucCodec.decode(a).hasWind());
        a.put("wfrom", "90");
        a.put("wmph", "0");
        assertFalse(NucCodec.decode(a).hasWind());
        a.put("wmph", "20");
        assertTrue(NucCodec.decode(a).hasWind());
    }

    @Test
    public void notConfusedWithOtherDetails() {
        String xml = NucCodec.toXml(sample());
        assertNull(BlastCodec.fromXml(xml));
        assertNull(UxoCodec.fromXml(xml));
        assertNull(NucCodec.fromXml("<__blast_uxo v=\"1\" src=\"ATP4-32.2\"/>"));
    }

    @Test
    public void fuzzNeverThrows() {
        Random rnd = new Random(7);
        String[] keys = NucCodec.KNOWN;
        for (int i = 0; i < 20000; i++) {
            Map<String, String> a = new LinkedHashMap<>();
            int n = rnd.nextInt(14);
            for (int k = 0; k < n; k++)
                a.put(keys[rnd.nextInt(keys.length)], junk(rnd));
            if (rnd.nextBoolean()) {
                a.put("mdl", NucModel.MODEL_ID);
                a.put("ed", rnd.nextBoolean() ? NucModel.MODEL_ED : "x");
                a.put("y_kt", Double.toString(0.1 + rnd.nextDouble() * 100));
            }
            NucRecord d = NucCodec.decode(a);
            if (d != null) {
                assertTrue(d.radii.sdzM > 0);
                assertNotNull(NucCodec.fromXml(NucCodec.toXml(d)));
                NucText.remarks(d);
                NucText.provenance(d);
                NucText.falloutLine(d);
                NucText.where(d, rnd.nextDouble() * 1e5, -rnd.nextDouble() * 1e5);
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
                return Double.toString(rnd.nextDouble() * 400);
            case 3:
                return "&<>\"'\u0000";
            case 4:
                return "SURFACE";
            default: {
                StringBuilder sb = new StringBuilder();
                int len = rnd.nextInt(80);
                for (int i = 0; i < len; i++)
                    sb.append((char) rnd.nextInt(0x3000));
                return sb.toString();
            }
        }
    }
}
