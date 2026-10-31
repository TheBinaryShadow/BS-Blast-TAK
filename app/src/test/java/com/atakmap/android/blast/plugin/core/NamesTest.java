package com.atakmap.android.blast.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class NamesTest {

    @Test
    public void clearedSuffixFollowsStatusAndKeepsCustomNames() {
        assertEquals("BLAST 2 Car CLEARED", Names.withStatus("BLAST 2 Car", true));
        assertEquals("BLAST 2 Car", Names.withStatus("BLAST 2 Car CLEARED", false));
        // A renamed incident keeps its name through clear / reopen.
        assertEquals("Main St car CLEARED",
                Names.withStatus("Main St car", true));
        assertEquals("Main St car",
                Names.withStatus("Main St car CLEARED", false));
        // Never doubles up, and doesn't cut words that merely end in CLEARED.
        assertEquals("X CLEARED", Names.withStatus("X CLEARED CLEARED", true));
        assertEquals("UNCLEARED", Names.base("UNCLEARED"));
        assertEquals("", Names.base(null));
    }

    @Test
    public void sanitize() {
        assertEquals("Main St car", Names.sanitize("  Main \t St\n car "));
        assertNull(Names.sanitize("   "));
        assertNull(Names.sanitize(null));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++)
            sb.append('x');
        assertEquals(Names.MAX_LENGTH, Names.sanitize(sb.toString()).length());
    }

    @Test
    public void numbers() {
        assertEquals(3, Names.numberIn("BLAST 3 SUV/Van"));
        assertEquals(12, Names.numberIn("BLAST 12 Car CLEARED"));
        assertEquals(0, Names.numberIn("Main St car"));
        assertEquals(0, Names.numberIn("BLASTED 3"));
        assertEquals(0, Names.numberIn(null));
        assertEquals("BLAST 3 SUV/Van", Names.initial(3, "SUV/Van"));
    }

    @Test
    public void numberTravelsInTheDetail() {
        BlastRecord r = BlastRecord.create(ZoneCalculator.forRow(
                StandoffTable.CISA_2025_08, "CAR"), "X", 0).withNumber(7);
        BlastRecord back = BlastCodec.fromXml(BlastCodec.toXml(r));
        assertEquals(7, back.number);
        assertEquals(7, back.cleared("Y", 1).reopened().number);
        assertEquals(0, BlastCodec.fromXml(BlastCodec.toXml(
                r.withNumber(0))).number);
    }
}
