package com.atakmap.android.blast.plugin.atak;

import com.atakmap.android.blast.plugin.core.BlastRecord;
import com.atakmap.android.blast.plugin.core.Exports;
import com.atakmap.android.blast.plugin.core.UxoRecord;
import com.atakmap.android.importexport.CotEventFactory;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Marker;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.filesystem.FileSystemUtils;
import com.atakmap.coremap.io.IOProviderFactory;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Writes one incident to atak/export/Blast/ as a data package (.zip),
 * KMZ or GPX. File names come from {@link Exports#fileStem}, which keeps
 * only letters, digits, '-' and '_' (names can arrive from the network).
 */
public final class Exporter {

    public enum Format {
        DATA_PACKAGE(".zip", "Data package (ATAK / WinTAK / iTAK)"),
        KMZ(".kmz", "KMZ (Google Earth)"),
        GPX(".gpx", "GPX (Garmin)");

        public final String extension;
        public final String label;

        Format(String extension, String label) {
            this.extension = extension;
            this.label = label;
        }
    }

    private final BlastStore store;

    public Exporter(BlastStore store) {
        this.store = store;
    }

    public static File directory() {
        return FileSystemUtils.getItem(FileSystemUtils.EXPORT_DIRECTORY
                + File.separator + "Blast");
    }

    /** Write one format; returns the file. */
    public File export(Marker m, Format format) throws IOException {
        BlastRecord r = BlastStore.read(m);
        UxoRecord u = BlastStore.readUxo(m);
        GeoPoint gp = m.getPoint();
        if ((r == null && u == null) || gp == null || !gp.isValid())
            throw new IOException("not an incident");
        String name = BlastStore.nameOf(m);
        long now = System.currentTimeMillis();
        Exports.Incident in = r == null ? null : new Exports.Incident(name,
                gp.getLatitude(), gp.getLongitude(), r);

        File dir = directory();
        if (!IOProviderFactory.exists(dir) && !IOProviderFactory.mkdirs(dir))
            throw new IOException("cannot create export folder");
        File out = new File(dir, Exports.fileStem(name, now)
                + format.extension);
        // The stem only has [A-Za-z0-9_-]; check anyway that the file
        // stays inside the export folder.
        if (!out.getCanonicalFile().getParentFile()
                .equals(dir.getCanonicalFile()))
            throw new IOException("bad export path");

        switch (format) {
            case DATA_PACKAGE: {
                Map<String, String> cot = new LinkedHashMap<>();
                for (MapItem mi : store.items(m)) {
                    CotEvent ev = CotEventFactory.createCotEvent(mi);
                    if (ev != null && ev.isValid())
                        cot.put(mi.getUID(), ev.toString());
                }
                if (cot.isEmpty())
                    throw new IOException("nothing to export");
                writeZip(out, Exports.dataPackage(UUID.randomUUID().toString(),
                        name, cot));
                break;
            }
            case KMZ:
                writeZip(out, u != null
                        ? Exports.kmzUxo(name, gp.getLatitude(),
                                gp.getLongitude(), u)
                        : Exports.kmz(in));
                break;
            case GPX:
            default: {
                try (OutputStream os = IOProviderFactory.getOutputStream(out)) {
                    String gpx = u != null
                            ? Exports.gpxUxo(name, gp.getLatitude(),
                                    gp.getLongitude(), u, now)
                            : Exports.gpx(in, now);
                    os.write(gpx.getBytes("UTF-8"));
                }
                break;
            }
        }
        return out;
    }

    private static void writeZip(File out, Map<String, byte[]> files)
            throws IOException {
        try (OutputStream os = IOProviderFactory.getOutputStream(out)) {
            Exports.zip(files, os);
        }
    }
}
