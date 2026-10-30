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
}
