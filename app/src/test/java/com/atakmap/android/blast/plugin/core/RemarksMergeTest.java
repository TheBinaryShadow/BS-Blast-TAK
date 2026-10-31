package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Remarks are an ordinary field anyone can edit (with or without Blast).
 * Blast must replace only its own block and never drop what people wrote.
 */
public class RemarksMergeTest {

    private static final BlastRecord SUV = BlastRecord.create(
            ZoneCalculator.forRow(StandoffTable.CISA_2025_08, "SUV_VAN"),
            "VIPER", 1793385900000L);

    @Test
    public void emptyGivesTheBlock() {
        assertEquals(Labels.remarks(SUV), Labels.mergeRemarks(null, SUV));
        assertEquals(Labels.remarks(SUV), Labels.mergeRemarks("  ", SUV));
    }

    @Test
    public void ourBlockIsReplacedAndTextAroundItKept() {
        String old = "Gate code 1234.\n" + Labels.remarks(SUV)
                + "\nDevice under the blue car.";
        BlastRecord cleared = SUV.cleared("ROOK", 1793389500000L);
        String merged = Labels.mergeRemarks(old, cleared);
        assertEquals("Gate code 1234.\n" + Labels.remarks(cleared)
                + "\nDevice under the blue car.", merged);
        // Repeated updates don't grow the text.
        assertEquals(merged, Labels.mergeRemarks(merged, cleared));
    }

    @Test
    public void userTextWithoutOurBlockIsKeptBelow() {
        String merged = Labels.mergeRemarks("Device under the blue car.", SUV);
        assertEquals(Labels.remarks(SUV) + "\n\nDevice under the blue car.",
                merged);
    }

    @Test
    public void damagedBlockIsNeverDropped() {
        // Someone cut the end of our block: keep everything they left.
        String old = "[BLAST] SUV/Van edited by hand, see EOD";
        String merged = Labels.mergeRemarks(old, SUV);
        assertTrue(merged.startsWith(Labels.remarks(SUV)));
        assertTrue(merged.endsWith(old));
    }

    @Test
    public void blockFrom06IsRecognised() {
        // 0.2-0.6 remarks end with the same sentence.
        assertTrue(Labels.remarks(SUV).endsWith(Labels.REMARKS_END));
        assertTrue(Labels.remarks(SUV).startsWith(Labels.REMARKS_PREFIX));
    }
}
