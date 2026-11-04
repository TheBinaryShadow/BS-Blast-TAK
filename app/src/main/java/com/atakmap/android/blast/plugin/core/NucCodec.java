package com.atakmap.android.blast.plugin.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code <__blast_nuc .../>} CoT detail (Blast 1.1).
 *
 * <pre>
 * &lt;__blast_nuc v="1" mdl="BLAST-NUC" ed="2026-11" y_kt="10" burst="SURFACE"
 *   pre="FEMA_10" plbl="..." wfrom="270" wmph="15" by="CALLSIGN" t="..."
 *   st="ACTIVE" cby="" ct="" n="1" sdz_m="805" mdz_m="1609" ldz_m="4828"
 *   burn_m="..." rad_m="..."/&gt;
 * </pre>
 *
 * Received data is untrusted. For the model edition this build carries,
 * every radius is recomputed from yield and burst (a mismatch is flagged,
 * never drawn). For another edition the carried radii are used if each is
 * within {@link #MIN_RING_M} … {@link #MAX_RING_M} and they are in order.
 */
public final class NucCodec {

    public static final String DETAIL = "__blast_nuc";

    static final String[] KNOWN = { "v", "mdl", "ed", "y_kt", "burst", "pre",
            "plbl", "wfrom", "wmph", "by", "t", "st", "cby", "ct", "n",
            "sdz_m", "mdz_m", "ldz_m", "burn_m", "rad_m" };

    static final double MIN_RING_M = 10, MAX_RING_M = 500_000;

    private NucCodec() {
    }

    public static Map<String, String> encode(NucRecord r) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("v", Integer.toString(NucRecord.VERSION));
        m.put("mdl", r.modelId);
        m.put("ed", r.modelEd);
        m.put("y_kt", num(r.yieldKt));
        m.put("burst", r.burst.name());
        put(m, "pre", r.presetId);
        put(m, "plbl", r.presetLabel);
        if (r.hasWind()) {
            m.put("wfrom", num(r.windFromDeg));
            m.put("wmph", num(r.windMph));
        }
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
        m.put("sdz_m", Long.toString(Math.round(r.radii.sdzM)));
        m.put("mdz_m", Long.toString(Math.round(r.radii.mdzM)));
        m.put("ldz_m", Long.toString(Math.round(r.radii.ldzM)));
        m.put("burn_m", Long.toString(Math.round(r.radii.burnM)));
        m.put("rad_m", Long.toString(Math.round(r.radii.radM)));
        for (Map.Entry<String, String> e : r.extra.entrySet())
            if (!m.containsKey(e.getKey()))
                m.put(e.getKey(), e.getValue());
        return m;
    }

    /** Plain decimal, at most 3 places, no exponent. */
    static String num(double d) {
        long thousandths = Math.round(d * 1000.0);
        String s = Long.toString(Math.abs(thousandths));
        while (s.length() < 4)
            s = "0" + s;
        String whole = s.substring(0, s.length() - 3);
        String frac = s.substring(s.length() - 3);
        while (frac.endsWith("0"))
            frac = frac.substring(0, frac.length() - 1);
        return (thousandths < 0 ? "-" : "") + whole
                + (frac.isEmpty() ? "" : "." + frac);
    }

    private static void put(Map<String, String> m, String k, String v) {
        if (v != null && !v.isEmpty())
            m.put(k, v);
    }

    /** Decode; null if the data can't describe a nuclear incident. */
    public static NucRecord decode(Map<String, String> a) {
        if (a == null)
            return null;
        String mdl = BlastCodec.text(a.get("mdl"));
        String ed = BlastCodec.text(a.get("ed"));
        double kt = BlastCodec.parseDouble(a.get("y_kt"));
        if (mdl.isEmpty() || !NucModel.validYield(kt))
            return null;
        NucModel.Burst burst = NucModel.Burst.parse(a.get("burst"));
        int v = BlastCodec.parseInt(a.get("v"), 1);
        NucRecord.Radii carried = new NucRecord.Radii(
                BlastCodec.parseDouble(a.get("sdz_m")),
                BlastCodec.parseDouble(a.get("mdz_m")),
                BlastCodec.parseDouble(a.get("ldz_m")),
                BlastCodec.parseDouble(a.get("burn_m")),
                BlastCodec.parseDouble(a.get("rad_m")));
        NucRecord.Radii radii;
        boolean mismatch = false;
        String presetId = BlastCodec.text(a.get("pre"));
        String presetLabel = BlastCodec.text(a.get("plbl"));
        if (NucModel.MODEL_ID.equals(mdl) && NucModel.MODEL_ED.equals(ed)) {
            radii = NucRecord.Radii.of(kt, burst);
            mismatch = sane(carried) && !radii.sameAs(carried);
            NucPresets.Preset p = NucPresets.byId(presetId);
            if (p != null && Math.abs(p.kt - kt) < 1e-6) {
                presetLabel = p.label;
            } else {
                presetId = "";
                presetLabel = "";
            }
        } else {
            if (!sane(carried))
                return null;
            radii = carried;
        }
        double wfrom = BlastCodec.parseDouble(a.get("wfrom"));
        double wmph = BlastCodec.parseDouble(a.get("wmph"));
        if (!NucRecord.validWind(wfrom, wmph)) {
            wfrom = Double.NaN;
            wmph = Double.NaN;
        } else {
            wfrom = NucRecord.normDeg(wfrom);
        }

        Map<String, String> extra = new LinkedHashMap<>();
        outer: for (Map.Entry<String, String> e : a.entrySet()) {
            if (extra.size() >= BlastCodec.MAX_ATTRIBUTES)
                break;
            for (String k : KNOWN)
                if (k.equals(e.getKey()))
                    continue outer;
            if (UxoCodec.isName(e.getKey()) && e.getValue() != null
                    && e.getValue().length() <= 256)
                extra.put(e.getKey(), e.getValue());
        }
        return new NucRecord(v, mdl, ed, kt, burst, presetId, presetLabel,
                wfrom, wmph, BlastCodec.text(a.get("by")),
                Formats.parseIso(a.get("t")),
                BlastRecord.Status.parse(a.get("st")),
                BlastCodec.text(a.get("cby")), Formats.parseIso(a.get("ct")),
                BlastCodec.parseInt(a.get("n"), 0), radii, extra, mismatch);
    }

    /** Every radius usable and the damage zones in order. */
    static boolean sane(NucRecord.Radii r) {
        double[] all = { r.sdzM, r.mdzM, r.ldzM, r.burnM, r.radM };
        for (double d : all)
            if (Double.isNaN(d) || d < MIN_RING_M || d > MAX_RING_M)
                return false;
        return r.sdzM <= r.mdzM && r.mdzM <= r.ldzM;
    }

    public static String toXml(NucRecord r) {
        StringBuilder sb = new StringBuilder("<").append(DETAIL);
        for (Map.Entry<String, String> e : encode(r).entrySet()) {
            sb.append(' ').append(e.getKey()).append("=\"")
                    .append(BlastCodec.escape(e.getValue())).append('"');
        }
        return sb.append("/>").toString();
    }

    public static NucRecord fromXml(String xml) {
        return decode(BlastCodec.parseAttributes(xml, DETAIL));
    }
}
