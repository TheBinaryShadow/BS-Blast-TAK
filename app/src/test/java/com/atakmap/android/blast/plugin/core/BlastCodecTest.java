package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.atakmap.android.blast.plugin.core.ZoneCalculator.MassUnit;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class BlastCodecTest {

    private static final StandoffTable T = StandoffTable.CISA_2025_08;
    private static final long T0 = 1793385900000L; // 2026-10-30T18:45:00Z

    private static BlastRecord suvEstimate() {
        return BlastRecord.create(
                ZoneCalculator.forQuantity(T, 800, MassUnit.LB), "VIPER 1-1",
                T0);
    }

    @Test
    public void encodeMatchesDocumentedFormat() {
        assertEquals("<__blast v=\"1\" src=\"CISA-BTSOC\" ed=\"2025-08\" "
                + "row=\"SUV_VAN\" lbl=\"SUV/Van\" cap_lb=\"1000\" "
                + "mand_ft=\"400\" sip_ft=\"2399\" pref_ft=\"2400\" "
                + "req=\"800\" req_u=\"lb\" by=\"VIPER 1-1\" "
                + "t=\"2026-10-30T18:45:00.000Z\" st=\"ACTIVE\"/>",
                BlastCodec.toXml(suvEstimate()));
    }

    @Test
    public void roundTrip() {
        BlastRecord a = suvEstimate().cleared("ROOK", T0 + 3_600_000);
        BlastRecord b = BlastCodec.fromXml(BlastCodec.toXml(a));
        assertNotNull(b);
        assertEquals(BlastCodec.toXml(a), BlastCodec.toXml(b));
        assertTrue(b.isCleared());
        assertEquals("ROOK", b.clearedBy);
        assertEquals(T0 + 3_600_000, b.clearedMillis);
        assertEquals(800, b.requestedValue, 0);
        assertEquals(MassUnit.LB, b.requestedUnit);
        assertFalse(b.disagreesWithTable());
        assertEquals(BlastRecord.Status.ACTIVE, b.reopened().status);
        assertEquals("", b.reopened().clearedBy);
    }

    @Test
    public void everyRowRoundTrips() {
        for (StandoffRow r : T.rows()) {
            BlastRecord rec = BlastRecord.create(
                    ZoneCalculator.forRow(T, r.id), "X", T0);
            BlastRecord back = BlastCodec.fromXml(BlastCodec.toXml(rec));
            assertEquals(r.mandatoryFt, back.mandatoryFt);
            assertEquals(r.shelterToFt, back.shelterToFt);
            assertEquals(r.preferredFt, back.preferredFt);
            assertEquals(r.capacityLb, back.capacityLb);
            assertFalse(back.hasRequest());
        }
    }

    @Test
    public void unknownAttributesAreKept() {
        String xml = "<__blast v=\"2\" src=\"CISA-BTSOC\" ed=\"2025-08\" "
                + "row=\"CAR\" mand_ft=\"320\" sip_ft=\"1899\" pref_ft=\"1900\" "
                + "op=\"x&amp;y\" future_ring='1234'/>";
        BlastRecord r = BlastCodec.fromXml(xml);
        assertEquals(2, r.version);
        assertEquals("x&y", r.extra.get("op"));
        assertEquals("1234", r.extra.get("future_ring"));
        String again = BlastCodec.toXml(r);
        assertTrue(again, again.contains("op=\"x&amp;y\""));
        assertTrue(again, again.contains("future_ring=\"1234\""));
        // Label and capacity filled from the known table.
        assertEquals("Car", r.rowLabel);
        assertEquals(500, r.capacityLb);
    }

    @Test
    public void unknownEditionUsesCarriedValues() {
        Map<String, String> a = new LinkedHashMap<>();
        a.put("src", "CISA-BTSOC");
        a.put("ed", "2028-01");
        a.put("row", "CAR");
        a.put("lbl", "Car");
        a.put("cap_lb", "500");
        a.put("mand_ft", "330");
        a.put("sip_ft", "1999");
        a.put("pref_ft", "2000");
        BlastRecord r = BlastCodec.decode(a);
        assertNotNull(r);
        assertNull(r.table());
        assertEquals(330, r.mandatoryFt);
        assertEquals(2000, r.preferredFt);
        assertEquals("CISA-BTSOC", r.sourceName());
        assertEquals("Jan 2028", Formats.editionLabel(r.edition));
    }

    @Test
    public void invalidDistancesFallBackToKnownRowOrReject() {
        Map<String, String> a = new LinkedHashMap<>();
        a.put("src", "CISA-BTSOC");
        a.put("ed", "2025-08");
        a.put("row", "SUV_VAN");
        a.put("mand_ft", "900"); // bigger than preferred: invalid
        a.put("pref_ft", "100");
        BlastRecord r = BlastCodec.decode(a);
        assertEquals(400, r.mandatoryFt);
        assertEquals(2400, r.preferredFt);

        a.put("ed", "1999-01"); // unknown edition and invalid values
        assertNull(BlastCodec.decode(a));

        Map<String, String> empty = new LinkedHashMap<>();
        assertNull(BlastCodec.decode(empty));
        assertNull(BlastCodec.decode(null));
        assertNull(BlastCodec.fromXml("<other a='1'/>"));
        assertNull(BlastCodec.fromXml("garbage"));
        assertNull(BlastCodec.fromXml(null));
    }

    @Test
    public void hostileTextIsEscapedAndCapped() {
        BlastRecord r = BlastRecord.create(ZoneCalculator.forRow(T, "CAR"),
                "A\"<b>&'c\n", T0);
        String xml = BlastCodec.toXml(r);
        assertFalse(xml.contains("<b>"));
        BlastRecord back = BlastCodec.fromXml(xml);
        assertEquals("A\"<b>&'c", back.byCallsign); // newline dropped

        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 500; i++)
            longName.append('x');
        Map<String, String> a = BlastCodec.encode(r);
        a.put("by", longName.toString());
        assertEquals(BlastCodec.MAX_TEXT, BlastCodec.decode(a).byCallsign
                .length());
    }

    @Test
    public void opaqueDetailText() {
        // What ATAK stores in "opaque-details" when no handler is loaded.
        String opaque = "<__blast row=\"PIPE_BOMB\" src=\"CISA-BTSOC\" "
                + "ed=\"2025-08\" mand_ft=\"70\" sip_ft=\"1199\" "
                + "pref_ft=\"1200\" st=\"CLEARED\" cby=\"ROOK\"></__blast>";
        BlastRecord r = BlastCodec.fromXml(opaque);
        assertEquals("PIPE_BOMB", r.rowId);
        assertTrue(r.isCleared());
        Map<String, String> attrs = BlastCodec.parseAttributes(
                "<detail><x/><__blast  a = 'q' b=\"&#65;&#x42;\" /></detail>",
                "__blast");
        assertEquals("q", attrs.get("a"));
        assertEquals("AB", attrs.get("b"));
        assertNull(BlastCodec.parseAttributes("<__blast a=\"unterminated",
                "__blast"));
        assertNull(BlastCodec.parseAttributes("<__blastx a='1'/>",
                "__blast"));
    }

    // ------------------------------------------------------------------
    // Audit (Oct 31, 2026): received data is untrusted.

    private static Map<String, String> suv() {
        Map<String, String> a = new LinkedHashMap<>();
        a.put("src", "CISA-BTSOC");
        a.put("ed", "2025-08");
        a.put("row", "SUV_VAN");
        a.put("lbl", "SUV/Van");
        a.put("cap_lb", "1000");
        a.put("mand_ft", "400");
        a.put("sip_ft", "2399");
        a.put("pref_ft", "2400");
        return a;
    }

    @Test
    public void knownEditionTableWinsOverTamperedDistances() {
        Map<String, String> a = suv();
        a.put("mand_ft", "10");
        a.put("sip_ft", "19");
        a.put("pref_ft", "20");
        a.put("lbl", "Pipe Bomb");
        BlastRecord r = BlastCodec.decode(a);
        assertEquals(400, r.mandatoryFt);
        assertEquals(2399, r.shelterToFt);
        assertEquals(2400, r.preferredFt);
        assertEquals(1000, r.capacityLb);
        assertEquals("SUV/Van", r.rowLabel);
        assertTrue(r.receivedMismatch);
        assertTrue(r.disagreesWithTable());
        // Re-encoding sends the table's values, not the tampered ones.
        assertTrue(BlastCodec.toXml(r).contains("mand_ft=\"400\""));
        // A matching record is not flagged.
        assertFalse(BlastCodec.decode(suv()).receivedMismatch);
        assertFalse(BlastCodec.decode(suv()).disagreesWithTable());
    }

    @Test
    public void unknownEditionIsCappedAndChecked() {
        Map<String, String> a = suv();
        a.put("ed", "2030-01");
        a.put("mand_ft", "1000");
        a.put("sip_ft", "99999");
        a.put("pref_ft", "100000");
        assertEquals(100000, BlastCodec.decode(a).preferredFt);
        a.put("pref_ft", "100001");
        assertNull(BlastCodec.decode(a));
        a.put("pref_ft", "-5");
        assertNull(BlastCodec.decode(a));
        a.put("pref_ft", "99999999999999999999");
        assertNull(BlastCodec.decode(a));
    }

    @Test
    public void incidentNumberIsClamped() {
        Map<String, String> a = suv();
        a.put("n", "2147483647");
        assertEquals(0, BlastCodec.decode(a).number);
        a.put("n", "-3");
        assertEquals(0, BlastCodec.decode(a).number);
        a.put("n", "999999");
        assertEquals(999999, BlastCodec.decode(a).number);
        a.put("n", "x");
        assertEquals(0, BlastCodec.decode(a).number);
    }

    @Test
    public void unknownAttributesAreBounded() {
        Map<String, String> a = suv();
        for (int i = 0; i < 500; i++)
            a.put("x" + i, "v");
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 1000; i++)
            big.append('y');
        a.put("huge", big.toString());
        BlastRecord r = BlastCodec.decode(a);
        assertEquals(BlastCodec.MAX_ATTRIBUTES, r.extra.size());
        assertFalse(r.extra.containsKey("huge"));
    }

    /**
     * Random and malformed input must never throw, and anything that
     * decodes must be a consistent, drawable record.
     */
    @Test
    public void fuzzDecoderNeverThrowsAndOnlyAcceptsConsistentRecords() {
        java.util.Random rnd = new java.util.Random(20261031L);
        String[] keys = { "v", "src", "ed", "row", "lbl", "cap_lb", "mand_ft",
                "sip_ft", "pref_ft", "req", "req_u", "by", "t", "st", "cby",
                "ct", "n", "zz", "a:b", "" };
        String[] vals = { "", "0", "-1", "1", "70", "1200", "2400", "100000",
                "100001", "1e9", "NaN", "Infinity", "2147483648", "CISA-BTSOC",
                "2025-08", "2030-01", "SUV_VAN", "CAR", "NOPE", "kg", "lb",
                "CLEARED", "ACTIVE", "2026-10-30T18:45:00Z", "<&\"'>",
                "\u0000\n\t", "\u202e\u0301" };
        for (int i = 0; i < 20000; i++) {
            Map<String, String> a = new LinkedHashMap<>();
            int n = rnd.nextInt(14);
            for (int k = 0; k < n; k++)
                a.put(keys[rnd.nextInt(keys.length)],
                        rnd.nextInt(5) == 0 ? Integer.toString(rnd.nextInt())
                                : vals[rnd.nextInt(vals.length)]);
            BlastRecord r = BlastCodec.decode(a);
            if (r == null)
                continue;
            assertTrue(r.mandatoryFt > 0);
            assertTrue(r.shelterToFt > r.mandatoryFt);
            assertTrue(r.preferredFt > r.shelterToFt);
            assertTrue(r.preferredFt <= BlastCodec.MAX_FT);
            assertTrue(r.number >= 0 && r.number <= BlastRecord.MAX_NUMBER);
            StandoffRow row = r.row();
            assertTrue(row.mandatoryRadiusM() > 0);
            assertTrue(row.preferredRadiusM() > row.mandatoryRadiusM());
            // Round trip is stable.
            BlastRecord back = BlastCodec.fromXml(BlastCodec.toXml(r));
            assertNotNull(back);
            assertEquals(BlastCodec.toXml(r), BlastCodec.toXml(back));
            Labels.remarks(r);
            Labels.provenance(r);
        }
        // Raw text through the XML attribute reader.
        String alphabet = "<>/=\"' _ab:&#;x0123456789__blast";
        for (int i = 0; i < 20000; i++) {
            StringBuilder sb = new StringBuilder();
            if (rnd.nextBoolean())
                sb.append("<__blast ");
            int len = rnd.nextInt(60);
            for (int k = 0; k < len; k++)
                sb.append(alphabet.charAt(rnd.nextInt(alphabet.length())));
            BlastCodec.fromXml(sb.toString());
        }
    }
}
