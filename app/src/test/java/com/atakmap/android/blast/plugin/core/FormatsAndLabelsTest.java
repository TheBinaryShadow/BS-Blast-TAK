package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.atakmap.android.blast.plugin.core.ZoneCalculator.MassUnit;

import org.junit.Test;

public class FormatsAndLabelsTest {

    private static final StandoffTable T = StandoffTable.CISA_2025_08;
    private static final long T0 = 1793385900000L; // 2026-10-30T18:45:00Z

    @Test
    public void conversionsAreExact() {
        assertEquals(0.3048, Formats.ftToM(1), 0);
        assertEquals(365.76, Formats.ftToM(1200), 1e-9);
        assertEquals(2.2046226218, Formats.kgToLb(1), 1e-9);
    }

    @Test
    public void cardMetresRoundOutward() {
        for (StandoffRow r : T.rows()) {
            assertTrue(Formats.cardMetres(r.mandatoryFt) >= Formats
                    .ftToM(r.mandatoryFt));
            assertTrue(Formats.cardMetres(r.preferredFt) >= Formats
                    .ftToM(r.preferredFt));
            assertTrue(Formats.cardMetres(r.preferredFt) - Formats
                    .ftToM(r.preferredFt) < 1);
        }
        assertEquals("70 ft (22 m)", Formats.cardDistance(70));
        assertEquals("1,200 ft (366 m)", Formats.cardDistance(1200));
        assertEquals("9,300 ft (2,835 m)", Formats.cardDistance(9300));
        // 1 m exactly = 3.28084 ft; 0.3048 m * 1000 = 304.8 → 305
        assertEquals(305, Formats.cardMetres(1000));
    }

    @Test
    public void numbers() {
        assertEquals("0", Formats.thousands(0));
        assertEquals("999", Formats.thousands(999));
        assertEquals("1,000", Formats.thousands(1000));
        assertEquals("60,000", Formats.thousands(60000));
        assertEquals("1,234,567", Formats.thousands(1234567));
        assertEquals("-1,000", Formats.thousands(-1000));
        assertEquals("800", Formats.quantity(800));
        assertEquals("661.4", Formats.quantity(661.3868));
        assertEquals("5.1", Formats.quantity(5.0001));
        assertEquals("450 ft (137 m)", Formats.measured(Formats.ftToM(450)));
        // Card distances stay in feet; far readouts switch to mi/km.
        assertEquals("9,999 ft (3,048 m)", Formats.measured(Formats.ftToM(9999)));
        assertEquals("1.9 mi (3.0 km)", Formats.measured(Formats.ftToM(10000)));
        assertEquals("12.2 mi (19.6 km)", Formats.measured(19577));
        assertEquals("10.3 mi (16.6 km)", Formats.measured(16626));
        assertEquals("124 mi (200 km)", Formats.measured(200000));
    }

    @Test
    public void times() {
        assertEquals("301845Z OCT 26", Formats.dtg(T0));
        assertEquals("2026-10-30T18:45:00.000Z", Formats.iso(T0));
        assertEquals(T0, Formats.parseIso("2026-10-30T18:45:00.000Z"));
        assertEquals(T0, Formats.parseIso("2026-10-30T18:45:00Z"));
        assertEquals(-1, Formats.parseIso("yesterday"));
        assertEquals(-1, Formats.parseIso(""));
        assertEquals("Aug 2025", Formats.editionLabel("2025-08"));
        assertEquals("2025", Formats.editionLabel("2025"));
    }

    @Test
    public void labelsForClientsWithoutThePlugin() {
        BlastRecord r = BlastRecord.create(
                ZoneCalculator.forQuantity(T, 800, MassUnit.LB), "VIPER", T0);
        assertEquals("MANDATORY EVAC 400 ft (122 m)", Labels.mandatoryRing(r));
        assertEquals("PREFERRED EVAC 2,400 ft+ (732 m) / SHELTER INSIDE",
                Labels.preferredRing(r));
        String rem = Labels.remarks(r);
        assertTrue(rem, rem.startsWith("[BLAST] SUV/Van (1,000 lb): "
                + "MANDATORY EVAC within 400 ft (122 m); SHELTER-IN-PLACE "
                + "401-2,399 ft"));
        assertTrue(rem, rem.contains("PREFERRED EVAC 2,400 ft+ (732 m)"));
        assertTrue(rem, rem.contains("CISA/FBI Bomb Threat Stand-Off Card, "
                + "Aug 2025, SUV/Van row."));
        assertTrue(rem, rem.contains("Estimate 800 lb, next row at or above."));
        assertTrue(rem, rem.contains("Placed by VIPER 301845Z OCT 26."));
        assertFalse(rem.contains("CLEARED"));

        BlastRecord c = r.cleared("ROOK", T0 + 60_000);
        assertTrue(Labels.remarks(c).startsWith("[BLAST] CLEARED. "));
        assertTrue(Labels.provenance(c).endsWith(
                "CLEARED by ROOK 301846Z OCT 26."));
        assertTrue(Labels.mandatoryRing(c).startsWith("CLEARED "));

        String[][] rows = Labels.zoneRows(r);
        assertEquals("0–400 ft", rows[0][1]);
        assertEquals("0–122 m", rows[0][2]);
        assertEquals("401–2,399 ft", rows[1][1]);
        assertEquals("123–731 m", rows[1][2]);
        assertEquals("2,400 ft+", rows[2][1]);
        assertEquals("732+ m", rows[2][2]);
        assertTrue(Labels.metresNote(r).contains("ERG2024 p. 367"));

        // Pipe bomb: ERG publishes 21 m (the conversion would round to 22).
        BlastRecord pipe = BlastRecord.create(
                ZoneCalculator.forRow(T, "PIPE_BOMB"), "X", T0);
        assertEquals("MANDATORY EVAC 70 ft (21 m)", Labels.mandatoryRing(pipe));
        // A row this build doesn't know falls back to outward conversion.
        java.util.Map<String, String> a = BlastCodec.encode(pipe);
        a.put("ed", "2030-01");
        BlastRecord other = BlastCodec.decode(a);
        assertEquals("MANDATORY EVAC 70 ft (22 m)", Labels.mandatoryRing(other));
        assertFalse(Labels.metresNote(other).contains("ERG"));
    }

    @Test
    public void noPercentSignsAnywhere() {
        // Lesson from Illum: no literal % may reach String.format paths.
        StringBuilder all = new StringBuilder(Guidance.CARD_ACTIONS)
                .append(Guidance.DISCLAIMER);
        for (String s : Guidance.CAVEATS)
            all.append(s);
        BlastRecord r = BlastRecord.create(ZoneCalculator.forRow(T, "CAR"),
                "X", T0);
        all.append(Labels.remarks(r)).append(Labels.provenance(r));
        assertFalse(all.toString().contains("%"));
    }
}
