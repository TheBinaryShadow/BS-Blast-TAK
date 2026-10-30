package com.atakmap.android.blast.plugin.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A published standoff table, stored as versioned data with its provenance.
 * Values are copied verbatim from the source and asserted by golden tests;
 * never edit a value without updating {@link #edition}.
 */
public final class StandoffTable {

    /** Stable source id carried in CoT ({@code src="CISA-BTSOC"}). */
    public final String sourceId;
    /** Edition as printed on the source, ISO year-month ({@code ed="2025-08"}). */
    public final String edition;
    /** Short human name for labels. */
    public final String shortName;
    /** Full title as published. */
    public final String title;
    /** Publishing organisations. */
    public final String publisher;
    /** Where the source can be obtained. */
    public final String url;
    /** Column heading for the quantity, as printed. */
    public final String quantityHeading;
    private final List<StandoffRow> rows;

    public StandoffTable(String sourceId, String edition, String shortName,
            String title, String publisher, String url,
            String quantityHeading, List<StandoffRow> rows) {
        this.sourceId = sourceId;
        this.edition = edition;
        this.shortName = shortName;
        this.title = title;
        this.publisher = publisher;
        this.url = url;
        this.quantityHeading = quantityHeading;
        List<StandoffRow> sorted = new ArrayList<>(rows);
        Collections.sort(sorted, (a, b) -> Integer.compare(a.capacityLb,
                b.capacityLb));
        this.rows = Collections.unmodifiableList(sorted);
    }

    /** Rows in increasing capacity order. */
    public List<StandoffRow> rows() {
        return rows;
    }

    public StandoffRow row(String id) {
        if (id == null)
            return null;
        for (StandoffRow r : rows)
            if (r.id.equals(id))
                return r;
        return null;
    }

    public StandoffRow largest() {
        return rows.get(rows.size() - 1);
    }

    /** "Aug 2025" from "2025-08". */
    public String editionLabel() {
        return Formats.editionLabel(edition);
    }

    // ------------------------------------------------------------------

    /**
     * DHS/DOJ Bomb Threat Stand-Off Card, CISA Office for Bombing Prevention
     * with the FBI Counter-IED Unit, marked "As of Aug 2025".
     * Feet only on the card. Values re-checked Oct 30, 2026 against a public
     * DHS copy of the card (identical rows).
     */
    public static final StandoffTable CISA_2025_08 = new StandoffTable(
            "CISA-BTSOC",
            "2025-08",
            "CISA/FBI Bomb Threat Stand-Off Card",
            "DHS/DOJ Bomb Threat Stand-Off Card",
            "CISA Office for Bombing Prevention and FBI Counter-IED Unit",
            "https://www.cisa.gov/resources-tools/resources/dhs-doj-bomb-threat-stand-card",
            "Explosives capacity",
            java.util.Arrays.asList(
                    new StandoffRow("PIPE_BOMB", "Pipe Bomb", 5, 70, 71, 1199, 1200),
                    new StandoffRow("SUICIDE_BOMBER", "Suicide Bomber", 20, 110, 111, 1699, 1700),
                    new StandoffRow("BRIEFCASE", "Briefcase/Suitcase", 50, 150, 151, 1849, 1850),
                    new StandoffRow("CAR", "Car", 500, 320, 321, 1899, 1900),
                    new StandoffRow("SUV_VAN", "SUV/Van", 1000, 400, 401, 2399, 2400),
                    new StandoffRow("SMALL_TRUCK", "Small Delivery Truck", 4000, 640, 641, 3799, 3800),
                    new StandoffRow("CONTAINER_TRUCK", "Container/Water Truck", 10000, 860, 861, 5099, 5100),
                    new StandoffRow("SEMI_TRAILER", "Semi-Trailer", 60000, 1570, 1571, 9299, 9300)));

    /** Tables this build knows, newest first. */
    public static StandoffTable find(String sourceId, String edition) {
        if (CISA_2025_08.sourceId.equals(sourceId)
                && CISA_2025_08.edition.equals(edition))
            return CISA_2025_08;
        return null;
    }

    /** The default table for new zones. */
    public static StandoffTable current() {
        return CISA_2025_08;
    }
}
