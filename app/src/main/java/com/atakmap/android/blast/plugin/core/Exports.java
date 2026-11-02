package com.atakmap.android.blast.plugin.core;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File exports of one incident: ATAK data package (mission package v2),
 * KML/KMZ for Google Earth and GPX for Garmin. Pure Java; the ATAK layer
 * supplies the CoT XML for the data package.
 */
public final class Exports {

    private Exports() {
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

    /** What an export needs to know about one incident. */
    public static final class Incident {
        public final String name;
        public final double lat;
        public final double lon;
        public final BlastRecord record;

        public Incident(String name, double lat, double lon,
                BlastRecord record) {
            this.name = name == null || name.isEmpty() ? "BLAST" : name;
            this.lat = lat;
            this.lon = lon;
            this.record = record;
        }
    }

    // ------------------------------------------------------------------
    // Data package

    /**
     * MANIFEST/manifest.xml for a data package holding the given CoT
     * entries ({uid → zip entry name}). The first uid names the package.
     */
    public static String manifest(String packageUid, String name,
            Map<String, String> uidToEntry) {
        StringBuilder sb = new StringBuilder();
        sb.append("<MissionPackageManifest version=\"2\">\n");
        sb.append(" <Configuration>\n");
        sb.append("  <Parameter name=\"uid\" value=\"")
                .append(BlastCodec.escape(packageUid)).append("\"/>\n");
        sb.append("  <Parameter name=\"name\" value=\"")
                .append(BlastCodec.escape(name)).append("\"/>\n");
        sb.append(" </Configuration>\n");
        sb.append(" <Contents>\n");
        for (Map.Entry<String, String> e : uidToEntry.entrySet()) {
            sb.append("  <Content ignore=\"false\" zipEntry=\"")
                    .append(BlastCodec.escape(e.getValue())).append("\">\n");
            sb.append("   <Parameter name=\"uid\" value=\"")
                    .append(BlastCodec.escape(e.getKey())).append("\"/>\n");
            sb.append("  </Content>\n");
        }
        sb.append(" </Contents>\n");
        sb.append("</MissionPackageManifest>\n");
        return sb.toString();
    }

    /** Zip entry name for a CoT item: "&lt;uid&gt;/&lt;uid&gt;.cot" (uid made path-safe). */
    public static String cotEntry(String uid) {
        String safe = uid.replaceAll("[^A-Za-z0-9._-]", "_");
        return safe + "/" + safe + ".cot";
    }

    /** Entries of a data package: manifest plus one .cot per item. */
    public static Map<String, byte[]> dataPackage(String packageUid,
            String name, Map<String, String> uidToCotXml) {
        Map<String, String> entries = new LinkedHashMap<>();
        for (String uid : uidToCotXml.keySet())
            entries.put(uid, cotEntry(uid));
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("MANIFEST/manifest.xml",
                manifest(packageUid, name, entries).getBytes(UTF8));
        for (Map.Entry<String, String> e : uidToCotXml.entrySet())
            files.put(entries.get(e.getKey()), e.getValue().getBytes(UTF8));
        return files;
    }

    // ------------------------------------------------------------------
    // KML / KMZ

    public static String kml(Incident in) {
        BlastRecord r = in.record;
        StandoffRow row = r.row();
        boolean grey = r.isCleared();
        double[][] mand = Geo.ring(in.lat, in.lon, row.mandatoryRadiusM());
        double[][] pref = Geo.ring(in.lat, in.lon, row.preferredRadiusM());
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n");
        tag(sb, "name", in.name);
        tag(sb, "description", Labels.remarks(r));
        style(sb, "mand", grey ? "ff9e9e9e" : "ff0000d5",
                grey ? "149e9e9e" : "550000d5", 3, null);
        style(sb, "pref", grey ? "ff9e9e9e" : "ff53c800",
                grey ? "149e9e9e" : "3300abff", 4, null);
        style(sb, "device", null, null, 0, grey ? "ff9e9e9e" : "ff0000ff");
        sb.append("<Folder>\n");
        tag(sb, "name", in.name);
        // Shelter-in-place band: preferred ring with the mandatory ring cut out.
        sb.append("<Placemark>\n");
        tag(sb, "name", Labels.preferredRing(r));
        tag(sb, "description", "Shelter-in-place zone inside this line; "
                + "preferred evacuation at or beyond it.");
        sb.append("<styleUrl>#pref</styleUrl>\n<Polygon>\n<outerBoundaryIs>");
        linearRing(sb, pref);
        sb.append("</outerBoundaryIs>\n<innerBoundaryIs>");
        linearRing(sb, mand);
        sb.append("</innerBoundaryIs>\n</Polygon>\n</Placemark>\n");
        sb.append("<Placemark>\n");
        tag(sb, "name", Labels.mandatoryRing(r));
        tag(sb, "description", "Mandatory evacuation zone.");
        sb.append("<styleUrl>#mand</styleUrl>\n<Polygon>\n<outerBoundaryIs>");
        linearRing(sb, mand);
        sb.append("</outerBoundaryIs>\n</Polygon>\n</Placemark>\n");
        sb.append("<Placemark>\n");
        tag(sb, "name", in.name);
        tag(sb, "description", Labels.remarks(r));
        sb.append("<styleUrl>#device</styleUrl>\n<Point><coordinates>")
                .append(coord(in.lon)).append(',').append(coord(in.lat))
                .append(",0</coordinates></Point>\n</Placemark>\n");
        sb.append("</Folder>\n</Document>\n</kml>\n");
        return sb.toString();
    }

    private static void style(StringBuilder sb, String id, String line,
            String fill, int width, String icon) {
        sb.append("<Style id=\"").append(id).append("\">");
        if (icon != null)
            sb.append("<IconStyle><color>").append(icon)
                    .append("</color></IconStyle>");
        if (line != null)
            sb.append("<LineStyle><color>").append(line).append("</color><width>")
                    .append(width).append("</width></LineStyle>");
        if (fill != null)
            sb.append("<PolyStyle><color>").append(fill)
                    .append("</color></PolyStyle>");
        sb.append("</Style>\n");
    }

    private static void linearRing(StringBuilder sb, double[][] pts) {
        sb.append("<LinearRing><coordinates>");
        for (double[] p : pts)
            sb.append(coord(p[1])).append(',').append(coord(p[0])).append(",0 ");
        sb.setLength(sb.length() - 1);
        sb.append("</coordinates></LinearRing>");
    }

    /** KMZ entries: doc.kml. */
    public static Map<String, byte[]> kmz(Incident in) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("doc.kml", kml(in).getBytes(UTF8));
        return files;
    }

    /** KML for a UXO incident: the cordon polygon and the item point. */
    public static String kmlUxo(String name, double lat, double lon,
            UxoRecord r) {
        String nm = name == null || name.isEmpty() ? "UXO" : name;
        boolean grey = r.isCleared();
        double[][] cordon = Geo.ring(lat, lon, r.cordonM);
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n<Document>\n");
        tag(sb, "name", nm);
        tag(sb, "description", UxoText.remarks(r));
        // KML colours are aabbggrr: dark orange E65100 -> ff0051e6.
        style(sb, "cordon", grey ? "ff9e9e9e" : "ff0051e6",
                grey ? "149e9e9e" : "400051e6", 4, null);
        style(sb, "item", null, null, 0, grey ? "ff9e9e9e" : "ff0051e6");
        sb.append("<Folder>\n");
        tag(sb, "name", nm);
        sb.append("<Placemark>\n");
        tag(sb, "name", UxoText.ringTitle(r));
        tag(sb, "description", UxoText.basis(r));
        sb.append("<styleUrl>#cordon</styleUrl>\n<Polygon>\n<outerBoundaryIs>");
        linearRing(sb, cordon);
        sb.append("</outerBoundaryIs>\n</Polygon>\n</Placemark>\n");
        sb.append("<Placemark>\n");
        tag(sb, "name", nm);
        tag(sb, "description", UxoText.remarks(r));
        sb.append("<styleUrl>#item</styleUrl>\n<Point><coordinates>")
                .append(coord(lon)).append(',').append(coord(lat))
                .append(",0</coordinates></Point>\n</Placemark>\n");
        sb.append("</Folder>\n</Document>\n</kml>\n");
        return sb.toString();
    }

    public static Map<String, byte[]> kmzUxo(String name, double lat,
            double lon, UxoRecord r) {
        Map<String, byte[]> files = new LinkedHashMap<>();
        files.put("doc.kml", kmlUxo(name, lat, lon, r).getBytes(UTF8));
        return files;
    }

    /** GPX for a UXO incident: a waypoint and the cordon as a track. */
    public static String gpxUxo(String name, double lat, double lon,
            UxoRecord r, long nowMillis) {
        String nm = name == null || name.isEmpty() ? "UXO" : name;
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"Blast for ATAK\" "
                + "xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        sb.append("<metadata>");
        tagInline(sb, "name", nm);
        tagInline(sb, "desc", UxoText.basis(r));
        tagInline(sb, "time", Formats.iso(nowMillis));
        sb.append("</metadata>\n");
        sb.append("<wpt lat=\"").append(coord(lat)).append("\" lon=\"")
                .append(coord(lon)).append("\">");
        tagInline(sb, "name", nm);
        tagInline(sb, "cmt", UxoText.threat(r) + ": minimum cordon "
                + UxoText.cordon(r.cordonM));
        tagInline(sb, "desc", UxoText.remarks(r));
        tagInline(sb, "sym", GPX_SYMBOL);
        sb.append("</wpt>\n");
        track(sb, Names.base(nm) + " CORDON " + r.cordonM + " m",
                UxoText.ringTitle(r), Geo.ring(lat, lon, r.cordonM));
        sb.append("</gpx>\n");
        return sb.toString();
    }

    // ------------------------------------------------------------------
    // GPX 1.1

    /** Garmin symbol for the device waypoint (verify on a unit). */
    public static final String GPX_SYMBOL = "Danger Area";

    public static String gpx(Incident in, long nowMillis) {
        BlastRecord r = in.record;
        StandoffRow row = r.row();
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<gpx version=\"1.1\" creator=\"Blast for ATAK\" "
                + "xmlns=\"http://www.topografix.com/GPX/1/1\">\n");
        sb.append("<metadata>");
        tagInline(sb, "name", in.name);
        tagInline(sb, "desc", Labels.source(r));
        tagInline(sb, "time", Formats.iso(nowMillis));
        sb.append("</metadata>\n");
        sb.append("<wpt lat=\"").append(coord(in.lat)).append("\" lon=\"")
                .append(coord(in.lon)).append("\">");
        tagInline(sb, "name", in.name);
        tagInline(sb, "cmt", Labels.threat(r) + ": mandatory "
                + Formats.ft(row.mandatoryFt) + ", preferred "
                + Formats.ft(row.preferredFt) + "+");
        tagInline(sb, "desc", Labels.remarks(r));
        tagInline(sb, "sym", GPX_SYMBOL);
        sb.append("</wpt>\n");
        track(sb, Names.base(in.name) + " MAND "
                + row.mandatoryMetres() + " m", Labels.mandatoryRing(r),
                Geo.ring(in.lat, in.lon, row.mandatoryRadiusM()));
        track(sb, Names.base(in.name) + " PREF "
                + row.preferredMetres() + " m", Labels.preferredRing(r),
                Geo.ring(in.lat, in.lon, row.preferredRadiusM()));
        sb.append("</gpx>\n");
        return sb.toString();
    }

    private static void track(StringBuilder sb, String name, String desc,
            double[][] pts) {
        sb.append("<trk>");
        tagInline(sb, "name", name);
        tagInline(sb, "desc", desc);
        sb.append("<trkseg>\n");
        for (double[] p : pts)
            sb.append("<trkpt lat=\"").append(coord(p[0])).append("\" lon=\"")
                    .append(coord(p[1])).append("\"/>\n");
        sb.append("</trkseg></trk>\n");
    }

    // ------------------------------------------------------------------

    /** Write entries to a zip stream (the caller closes {@code out}). */
    public static void zip(Map<String, byte[]> files, OutputStream out)
            throws IOException {
        ZipOutputStream z = new ZipOutputStream(out);
        for (Map.Entry<String, byte[]> e : files.entrySet()) {
            z.putNextEntry(new ZipEntry(e.getKey()));
            z.write(e.getValue());
            z.closeEntry();
        }
        z.finish();
    }

    static String coord(double v) {
        return String.format(Locale.US, "%.7f", v);
    }

    private static void tag(StringBuilder sb, String name, String text) {
        tagInline(sb, name, text);
        sb.append('\n');
    }

    private static void tagInline(StringBuilder sb, String name, String text) {
        sb.append('<').append(name).append('>')
                .append(BlastCodec.escape(text)).append("</").append(name)
                .append('>');
    }

    /** "BLAST_2_Car_310102Z" style stem for exported files. */
    public static String fileStem(String name, long millis) {
        String dtg = Formats.dtg(millis);
        return Names.fileStem(name) + "_" + dtg.substring(0, 7);
    }
}
