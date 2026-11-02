package com.atakmap.android.blast.plugin.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code <__blast_uxo .../>} CoT detail. Separate from {@code __blast}
 * so Blast 1.0 clients keep it as an opaque detail and ignore it.
 *
 * <pre>
 * &lt;__blast_uxo v="1" src="ATP4-32.2" ed="2024-03-01" fig="31"
 *   cat="PROJ_MORTAR" lbl="Projectiles and Mortars" cord_m="375" multi="0"
 *   large="0" where="PATH" plc="RB" by="CALLSIGN" t="..." st="ACTIVE"
 *   cby="" ct="" n="3" r_n="1" r_desc="..." .../&gt;
 * </pre>
 *
 * Received data is untrusted. For the edition this build carries, the
 * cordon is recomputed from the type (a mismatch is flagged, never drawn).
 * For an edition this build lacks, the carried cordon is used if it is
 * within 300 m … 30 km.
 */
public final class UxoCodec {

    public static final String DETAIL = "__blast_uxo";

    static final String[] KNOWN = {
            "v", "src", "ed", "fig", "cat", "lbl", "cord_m", "multi", "large",
            "where", "plc", "by", "t", "st", "cby", "ct", "n", "r_n",
            "r_desc", "r_land", "r_emp", "r_cbrn", "r_res", "r_imp", "r_prot",
            "r_pri", "r_poc"
    };

    /** Longest report text kept (description and similar fields). */
    public static final int MAX_REPORT_TEXT = 200;

    private UxoCodec() {
    }

    public static Map<String, String> encode(UxoRecord r) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("v", Integer.toString(UxoRecord.VERSION));
        m.put("src", r.sourceId);
        m.put("ed", r.edition);
        if (r.isKnownEdition())
            m.put("fig", UxoTable.FIGURE);
        m.put("cat", r.categoryId);
        m.put("lbl", r.categoryLabel);
        m.put("cord_m", Integer.toString(r.cordonM));
        m.put("multi", r.multiple ? "1" : "0");
        if (r.large)
            m.put("large", "1");
        if (r.where != UxoRecord.Where.UNSET)
            m.put("where", r.where.name());
        if (r.placement != UxoRecord.Placement.UNKNOWN)
            m.put("plc", r.placement.name());
        put(m, "by", r.byCallsign);
        if (r.timeMillis > 0)
            m.put("t", Formats.iso(r.timeMillis));
        m.put("st", r.status.name());
        if (r.isCleared()) {
            put(m, "cby", r.clearedBy);
            if (r.clearedMillis > 0)
                m.put("ct", Formats.iso(r.clearedMillis));
        }
        if (r.number > 0)
            m.put("n", Integer.toString(r.number));
        UxoRecord.Report p = r.report;
        if (p.count > 0)
            m.put("r_n", Integer.toString(p.count));
        put(m, "r_desc", p.description);
        put(m, "r_land", p.landmark);
        put(m, "r_emp", p.emplacement);
        put(m, "r_cbrn", p.cbrn);
        put(m, "r_res", p.resources);
        put(m, "r_imp", p.impact);
        put(m, "r_prot", p.protective);
        if (p.priority != UxoRecord.Priority.UNSET)
            m.put("r_pri", p.priority.name());
        put(m, "r_poc", p.contact);
        for (Map.Entry<String, String> e : r.extra.entrySet())
            if (!m.containsKey(e.getKey()))
                m.put(e.getKey(), e.getValue());
        return m;
    }

    private static void put(Map<String, String> m, String k, String v) {
        if (v != null && !v.isEmpty())
            m.put(k, v);
    }

    /** Decode; null if the data can't describe a UXO incident. */
    public static UxoRecord decode(Map<String, String> a) {
        if (a == null)
            return null;
        String src = BlastCodec.text(a.get("src"));
        String ed = BlastCodec.text(a.get("ed"));
        String cat = BlastCodec.text(a.get("cat"));
        if (src.isEmpty() || cat.isEmpty())
            return null;
        int v = BlastCodec.parseInt(a.get("v"), 1);
        boolean multi = "1".equals(a.get("multi"));
        boolean large = "1".equals(a.get("large"));
        int carried = BlastCodec.parseInt(a.get("cord_m"), -1);
        String label = BlastCodec.text(a.get("lbl"));
        int cordon;
        boolean mismatch = false;
        if (UxoTable.isKnown(src, ed)) {
            UxoTable.Category c = UxoTable.category(cat);
            if (c == null)
                return null;
            large = large && c.isUnknown();
            // This build's table is authoritative for its own edition.
            cordon = UxoTable.cordonM(c.id, multi, large);
            mismatch = carried > 0 && carried != cordon;
            label = c.doctrineLabel;
        } else {
            if (carried < UxoTable.MIN_CORDON_M
                    || carried > UxoTable.MAX_CORDON_M)
                return null;
            cordon = carried;
            if (label.isEmpty())
                label = cat;
        }

        UxoRecord.Report report = new UxoRecord.Report(
                BlastCodec.parseInt(a.get("r_n"), 0),
                longText(a.get("r_desc")), longText(a.get("r_land")),
                BlastCodec.text(a.get("r_emp")), longText(a.get("r_cbrn")),
                longText(a.get("r_res")), longText(a.get("r_imp")),
                longText(a.get("r_prot")),
                UxoRecord.Priority.parse(a.get("r_pri")),
                longText(a.get("r_poc")));

        Map<String, String> extra = new LinkedHashMap<>();
        outer: for (Map.Entry<String, String> e : a.entrySet()) {
            if (extra.size() >= BlastCodec.MAX_ATTRIBUTES)
                break;
            for (String k : KNOWN)
                if (k.equals(e.getKey()))
                    continue outer;
            if (isName(e.getKey()) && e.getValue() != null
                    && e.getValue().length() <= 256)
                extra.put(e.getKey(), e.getValue());
        }

        return new UxoRecord(v, src, ed, cat, label, cordon, multi, large,
                UxoRecord.Where.parse(a.get("where")),
                UxoRecord.Placement.parse(a.get("plc")),
                BlastCodec.text(a.get("by")), Formats.parseIso(a.get("t")),
                BlastRecord.Status.parse(a.get("st")),
                BlastCodec.text(a.get("cby")), Formats.parseIso(a.get("ct")),
                BlastCodec.parseInt(a.get("n"), 0), report, extra, mismatch);
    }

    public static String toXml(UxoRecord r) {
        StringBuilder sb = new StringBuilder("<").append(DETAIL);
        for (Map.Entry<String, String> e : encode(r).entrySet()) {
            sb.append(' ').append(e.getKey()).append("=\"")
                    .append(BlastCodec.escape(e.getValue())).append('"');
        }
        return sb.append("/>").toString();
    }

    public static UxoRecord fromXml(String xml) {
        return decode(BlastCodec.parseAttributes(xml, DETAIL));
    }

    /**
     * Report text: like {@link BlastCodec#text} but up to
     * {@link #MAX_REPORT_TEXT} characters.
     */
    public static String longText(String s) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() && sb.length() < MAX_REPORT_TEXT; i++) {
            char c = s.charAt(i);
            if (c >= 0x20 && c != 0x7f)
                sb.append(c);
            else if (c == '\n' || c == '\t')
                sb.append(' ');
        }
        return sb.toString().trim();
    }

    private static boolean isName(String s) {
        if (s == null || s.isEmpty() || s.length() > 32)
            return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (!(Character.isLetterOrDigit(c) || c == '_' || c == '-'
                    || c == '.' || c == ':'))
                return false;
        }
        return Character.isLetter(s.charAt(0)) || s.charAt(0) == '_';
    }
}
