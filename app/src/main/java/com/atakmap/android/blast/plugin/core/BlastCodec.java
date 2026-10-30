package com.atakmap.android.blast.plugin.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The {@code <__blast .../>} CoT detail: encode, decode, and a small XML
 * attribute reader for details ATAK kept as opaque text while the plugin
 * wasn't loaded.
 *
 * <pre>
 * &lt;__blast v="1" src="CISA-BTSOC" ed="2025-08" row="SUV_VAN" lbl="SUV/Van"
 *   cap_lb="1000" mand_ft="400" sip_ft="2399" pref_ft="2400" req="800" req_u="lb"
 *   by="CALLSIGN" t="2026-10-30T18:45:00.000Z" st="ACTIVE" cby="" ct=""/&gt;
 * </pre>
 *
 * Distances carried in the detail are what the sender drew and are shown as
 * received; they're only replaced from this build's table when missing or
 * invalid.
 */
public final class BlastCodec {

    public static final String DETAIL = "__blast";

    static final String[] KNOWN = {
            "v", "src", "ed", "row", "lbl", "cap_lb", "mand_ft", "sip_ft",
            "pref_ft", "req", "req_u", "by", "t", "st", "cby", "ct"
    };

    /** Longest text value kept from received data. */
    static final int MAX_TEXT = 64;

    private BlastCodec() {
    }

    public static Map<String, String> encode(BlastRecord r) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("v", Integer.toString(BlastRecord.VERSION));
        m.put("src", r.sourceId);
        m.put("ed", r.edition);
        m.put("row", r.rowId);
        m.put("lbl", r.rowLabel);
        m.put("cap_lb", Integer.toString(r.capacityLb));
        m.put("mand_ft", Integer.toString(r.mandatoryFt));
        m.put("sip_ft", Integer.toString(r.shelterToFt));
        m.put("pref_ft", Integer.toString(r.preferredFt));
        if (r.hasRequest()) {
            m.put("req", plain(r.requestedValue));
            m.put("req_u", r.requestedUnit.symbol);
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
        for (Map.Entry<String, String> e : r.extra.entrySet())
            if (!m.containsKey(e.getKey()))
                m.put(e.getKey(), e.getValue());
        return m;
    }

    private static void put(Map<String, String> m, String k, String v) {
        if (v != null && !v.isEmpty())
            m.put(k, v);
    }

    private static String plain(double d) {
        if (Math.abs(d - Math.rint(d)) < 1e-9 && Math.abs(d) < 1e15)
            return Long.toString((long) Math.rint(d));
        return Double.toString(d);
    }

    /** Decode; null if the data can't describe a zone. */
    public static BlastRecord decode(Map<String, String> a) {
        if (a == null)
            return null;
        String src = text(a.get("src"));
        String ed = text(a.get("ed"));
        String rowId = text(a.get("row"));
        if (src.isEmpty() || rowId.isEmpty())
            return null;
        int v = parseInt(a.get("v"), 1);

        StandoffTable table = StandoffTable.find(src, ed);
        StandoffRow known = table == null ? null : table.row(rowId);

        int mand = parseInt(a.get("mand_ft"), -1);
        int pref = parseInt(a.get("pref_ft"), -1);
        int sip = parseInt(a.get("sip_ft"), pref - 1);
        int cap = parseInt(a.get("cap_lb"), -1);
        boolean valid = mand > 0 && pref > mand && sip > mand && sip < pref
                && pref <= 1_000_000;
        if (!valid) {
            if (known == null)
                return null;
            mand = known.mandatoryFt;
            sip = known.shelterToFt;
            pref = known.preferredFt;
        }
        if (cap <= 0)
            cap = known != null ? known.capacityLb : 0;

        String label = text(a.get("lbl"));
        if (label.isEmpty())
            label = known != null ? known.label : rowId;

        double req = parseDouble(a.get("req"));
        if (!(req > 0) || Double.isInfinite(req))
            req = Double.NaN;

        Map<String, String> extra = new LinkedHashMap<>();
        outer: for (Map.Entry<String, String> e : a.entrySet()) {
            for (String k : KNOWN)
                if (k.equals(e.getKey()))
                    continue outer;
            if (isName(e.getKey()) && e.getValue() != null)
                extra.put(e.getKey(), e.getValue());
        }

        return new BlastRecord(v, src, ed, rowId, label, cap, mand, sip, pref,
                req, ZoneCalculator.MassUnit.parse(a.get("req_u")),
                text(a.get("by")), Formats.parseIso(a.get("t")),
                BlastRecord.Status.parse(a.get("st")), text(a.get("cby")),
                Formats.parseIso(a.get("ct")), extra);
    }

    // ------------------------------------------------------------------
    // XML text form (stored in marker metadata and in ATAK's opaque details)

    public static String toXml(BlastRecord r) {
        StringBuilder sb = new StringBuilder("<").append(DETAIL);
        for (Map.Entry<String, String> e : encode(r).entrySet()) {
            sb.append(' ').append(e.getKey()).append("=\"")
                    .append(escape(e.getValue())).append('"');
        }
        return sb.append("/>").toString();
    }

    public static BlastRecord fromXml(String xml) {
        return decode(parseAttributes(xml, DETAIL));
    }

    /**
     * Attributes of the first element named {@code element} in {@code xml},
     * or null. Handles both quote styles and the five XML entities plus
     * numeric character references.
     */
    public static Map<String, String> parseAttributes(String xml,
            String element) {
        if (xml == null)
            return null;
        int i = 0;
        int n = xml.length();
        while (i < n) {
            int lt = xml.indexOf('<', i);
            if (lt < 0)
                return null;
            int p = lt + 1;
            int nameStart = p;
            while (p < n && isNameChar(xml.charAt(p)))
                p++;
            String name = xml.substring(nameStart, p);
            if (!name.equals(element)) {
                i = p > lt + 1 ? p : lt + 1;
                continue;
            }
            Map<String, String> attrs = new LinkedHashMap<>();
            while (p < n) {
                while (p < n && Character.isWhitespace(xml.charAt(p)))
                    p++;
                if (p >= n)
                    return null;
                char c = xml.charAt(p);
                if (c == '/' || c == '>')
                    return attrs;
                int ks = p;
                while (p < n && isNameChar(xml.charAt(p)))
                    p++;
                if (p == ks)
                    return null;
                String key = xml.substring(ks, p);
                while (p < n && Character.isWhitespace(xml.charAt(p)))
                    p++;
                if (p >= n || xml.charAt(p) != '=')
                    return null;
                p++;
                while (p < n && Character.isWhitespace(xml.charAt(p)))
                    p++;
                if (p >= n)
                    return null;
                char q = xml.charAt(p);
                if (q != '"' && q != '\'')
                    return null;
                int vs = ++p;
                int ve = xml.indexOf(q, vs);
                if (ve < 0)
                    return null;
                attrs.put(key, unescape(xml.substring(vs, ve)));
                p = ve + 1;
            }
            return null;
        }
        return null;
    }

    static String escape(String s) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&apos;");
                    break;
                default:
                    if (c < 0x20)
                        sb.append("&#").append((int) c).append(';');
                    else
                        sb.append(c);
            }
        }
        return sb.toString();
    }

    static String unescape(String s) {
        if (s.indexOf('&') < 0)
            return s;
        StringBuilder sb = new StringBuilder(s.length());
        int i = 0;
        while (i < s.length()) {
            char c = s.charAt(i);
            if (c == '&') {
                int semi = s.indexOf(';', i);
                if (semi > i && semi - i <= 10) {
                    String ent = s.substring(i + 1, semi);
                    String rep = null;
                    if (ent.equals("amp"))
                        rep = "&";
                    else if (ent.equals("lt"))
                        rep = "<";
                    else if (ent.equals("gt"))
                        rep = ">";
                    else if (ent.equals("quot"))
                        rep = "\"";
                    else if (ent.equals("apos"))
                        rep = "'";
                    else if (ent.startsWith("#")) {
                        try {
                            int cp = ent.startsWith("#x") || ent.startsWith("#X")
                                    ? Integer.parseInt(ent.substring(2), 16)
                                    : Integer.parseInt(ent.substring(1));
                            if (Character.isValidCodePoint(cp))
                                rep = new String(Character.toChars(cp));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                    if (rep != null) {
                        sb.append(rep);
                        i = semi + 1;
                        continue;
                    }
                }
            }
            sb.append(c);
            i++;
        }
        return sb.toString();
    }

    private static boolean isNameChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '.'
                || c == ':';
    }

    private static boolean isName(String s) {
        if (s == null || s.isEmpty() || s.length() > 32)
            return false;
        for (int i = 0; i < s.length(); i++)
            if (!isNameChar(s.charAt(i)))
                return false;
        return Character.isLetter(s.charAt(0)) || s.charAt(0) == '_';
    }

    /** Trim, drop control characters, cap the length. */
    static String text(String s) {
        if (s == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length() && sb.length() < MAX_TEXT; i++) {
            char c = s.charAt(i);
            if (c >= 0x20 && c != 0x7f)
                sb.append(c);
        }
        return sb.toString().trim();
    }

    static int parseInt(String s, int def) {
        if (s == null)
            return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    static double parseDouble(String s) {
        if (s == null)
            return Double.NaN;
        try {
            return Double.parseDouble(s.trim());
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }
}
