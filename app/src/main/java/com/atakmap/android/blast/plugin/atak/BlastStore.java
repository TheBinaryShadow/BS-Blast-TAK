package com.atakmap.android.blast.plugin.atak;

import android.os.Handler;
import android.os.Looper;

import com.atakmap.android.blast.plugin.core.BlastCodec;
import com.atakmap.android.blast.plugin.core.BlastRecord;
import com.atakmap.android.blast.plugin.core.StandoffRow;
import com.atakmap.android.blast.plugin.core.Labels;
import com.atakmap.android.blast.plugin.core.Names;
import com.atakmap.android.blast.plugin.core.NucCodec;
import com.atakmap.android.blast.plugin.core.NucGeo;
import com.atakmap.android.blast.plugin.core.NucModel;
import com.atakmap.android.blast.plugin.core.NucRecord;
import com.atakmap.android.blast.plugin.core.NucText;
import com.atakmap.android.blast.plugin.core.UxoCodec;
import com.atakmap.android.blast.plugin.core.UxoRecord;
import com.atakmap.android.blast.plugin.core.UxoText;
import com.atakmap.android.drawing.DrawingToolsMapComponent;
import com.atakmap.android.drawing.mapItems.DrawingCircle;
import com.atakmap.android.drawing.mapItems.DrawingShape;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.maps.Shape;
import com.atakmap.android.user.PlacePointTool;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gov.tak.api.util.AttributeSet;

/**
 * Incidents on the map. An incident is a normal marker (CoT a-h-G, readable
 * by any TAK client) carrying the {@code __blast} detail, plus two native
 * ATAK drawing circles anchored to it:
 * <ul>
 * <li>{@code <uid>.mand} — mandatory evacuation (red, filled)</li>
 * <li>{@code <uid>.pref} — preferred evacuation line (green), amber fill
 * for the shelter-in-place band inside it</li>
 * </ul>
 * A UXO incident (1.1+) is the same kind of marker carrying the
 * {@code __blast_uxo} detail and one circle, {@code <uid>.cordon} (orange).
 * A nuclear reference incident (1.1+) carries {@code __blast_nuc} and up to
 * six circles ({@code .sdz .mdz .ldz .burn .rad .gi}) plus, with a wind,
 * three fallout outlines ({@code .fo1000 .fo100 .fo10}) that are redrawn
 * when the marker moves.
 * Ring UIDs are derived from the marker UID, so every client agrees on them
 * and updates merge. The rings are anchored to the marker (CoT
 * {@code p-p-CenterAnchor}), so moving the marker moves them everywhere.
 */
public final class BlastStore {

    private static final String TAG = "BlastStore";

    /** Marker metadata: the record as {@code <__blast .../>} XML. */
    public static final String META = "blast.data";
    /** Marker metadata: a UXO record as {@code <__blast_uxo .../>} XML. */
    public static final String META_UXO = "blast.uxo";
    /** Marker metadata: a nuclear record as {@code <__blast_nuc .../>} XML. */
    public static final String META_NUC = "blast.nuc";
    /** Marker metadata flag for fast lookup (both kinds). */
    public static final String META_FLAG = "blast.zone";
    /** Ring metadata: owning marker UID. */
    public static final String META_PARENT = "blast.parent";
    /** ATAK's store for details no handler claimed. */
    static final String OPAQUE = "opaque-details";

    /** Hostile ground (red diamond). 0.1 used a-u-G; those markers keep it. */
    public static final String MARKER_TYPE = "a-h-G";
    public static final String MAND_SUFFIX = ".mand";
    public static final String PREF_SUFFIX = ".pref";
    public static final String CORDON_SUFFIX = ".cordon";
    private static final String[] SUFFIXES = buildSuffixes();

    private static String[] buildSuffixes() {
        List<String> l = new ArrayList<>();
        l.add(PREF_SUFFIX);
        l.add(MAND_SUFFIX);
        l.add(CORDON_SUFFIX);
        for (NucText.Ring r : NucText.Ring.values())
            l.add(r.suffix);
        for (int rate : NucModel.FALLOUT_RATES)
            l.add(NucText.falloutSuffix(rate));
        return l.toArray(new String[0]);
    }

    static final int RED = 0xFFD50000;
    static final int RED_FILL = 0x55D50000;
    static final int GREEN = 0xFF00C853;
    static final int AMBER_FILL = 0x33FFAB00;
    static final int GREY = 0xFF9E9E9E;
    static final int GREY_FILL = 0x149E9E9E;
    /** UXO cordon: dark orange, distinct from the IED card's colours. */
    static final int ORANGE = 0xFFE65100;
    static final int ORANGE_FILL = 0x40E65100;
    /** Nuclear rings: FEMA's red / orange / yellow zones. */
    static final int NUC_SDZ = 0xFFB71C1C, NUC_SDZ_FILL = 0x40B71C1C;
    static final int NUC_MDZ = 0xFFFF6D00, NUC_MDZ_FILL = 0x30FF6D00;
    static final int NUC_LDZ = 0xFFFFD600, NUC_LDZ_FILL = 0x1AFFD600;
    static final int NUC_BURN = 0xFF00B8D4;
    static final int NUC_RAD = 0xFF536DFE;
    static final int NUC_GI = 0xFFE0E0E0;
    static final int[] NUC_FO = { 0xFF6A1B9A, 0xFF9C27B0, 0xFFCE93D8 };
    static final int[] NUC_FO_FILL = { 0x456A1B9A, 0x339C27B0, 0x22CE93D8 };
    static final int NO_FILL = 0x00000000;


    private final MapView mapView;
    private final android.content.SharedPreferences prefs;

    /**
     * UIDs of every incident this device has created, received or seen.
     * Persisted, so the list doesn't depend on any one ATAK lookup.
     */
    private final java.util.Set<String> registry = java.util.Collections
            .newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());
    private static final String PREF_REGISTRY = "blast.registry";

    /** What the last {@link #all()} found, for the debug line in the pane. */
    private volatile String lastScan = "";

    public BlastStore(MapView mapView) {
        this.mapView = mapView;
        this.prefs = com.atakmap.android.preference.AtakPreferences
                .getInstance(mapView.getContext()).getSharedPrefs();
        try {
            java.util.Set<String> saved = prefs.getStringSet(PREF_REGISTRY,
                    null);
            if (saved != null)
                registry.addAll(saved);
        } catch (RuntimeException e) {
            Log.w(TAG, "registry unreadable", e);
        }
    }

    // ------------------------------------------------------------------
    // Liveness and lookup.
    //
    // ATAK's UID lookup (MapView.getMapItem -> RootMapGroup.deepFindUID)
    // uses a UID index that can still return an item after it was removed
    // from the map. An item is only on the map while it belongs to a group.

    public static boolean isLive(MapItem mi) {
        return mi != null && mi.getGroup() != null;
    }

    /** The item with this UID if it is actually on the map, else null. */
    public MapItem liveItem(String uid) {
        if (uid == null)
            return null;
        MapItem mi = mapView.getMapItem(uid);
        return isLive(mi) ? mi : null;
    }

    /** The incident marker with this UID if it is on the map, else null. */
    public Marker liveMarker(String uid) {
        MapItem mi = liveItem(uid);
        if (mi instanceof Marker && isIncident(mi))
            return (Marker) mi;
        return null;
    }

    /** True for an incident marker of either kind. */
    public static boolean isIncident(MapItem mi) {
        return read(mi) != null || readUxo(mi) != null || readNuc(mi) != null;
    }

    /** True for a nuclear reference incident marker. */
    public static boolean isNuc(MapItem mi) {
        return readNuc(mi) != null;
    }

    /** True for a UXO incident marker. */
    public static boolean isUxo(MapItem mi) {
        return readUxo(mi) != null;
    }

    public void register(String uid) {
        if (uid != null && registry.add(uid))
            saveRegistry();
    }

    private void unregister(String uid) {
        if (uid != null && registry.remove(uid))
            saveRegistry();
    }

    private void saveRegistry() {
        try {
            prefs.edit().putStringSet(PREF_REGISTRY,
                    new java.util.HashSet<>(registry)).apply();
        } catch (RuntimeException e) {
            Log.w(TAG, "registry not saved", e);
        }
    }

    public String lastScan() {
        return lastScan;
    }

    // ------------------------------------------------------------------
    // Reading

    /**
     * Store the record on an item: Blast's own metadata plus a copy in
     * ATAK's opaque-details, so the detail survives (and is re-sent) even
     * while the plugin isn't loaded.
     */
    static void store(MapItem item, String xml) {
        store(item, xml, META, BlastCodec.DETAIL);
    }

    /** Store a UXO record (see {@link #store(MapItem, String)}). */
    static void storeUxo(MapItem item, String xml) {
        store(item, xml, META_UXO, UxoCodec.DETAIL);
    }

    /** Store a nuclear record (see {@link #store(MapItem, String)}). */
    static void storeNuc(MapItem item, String xml) {
        store(item, xml, META_NUC, NucCodec.DETAIL);
    }

    private static void store(MapItem item, String xml, String meta,
            String detail) {
        item.setMetaString(meta, xml);
        item.setMetaBoolean(META_FLAG, true);
        try {
            AttributeSet opaque = item.getMetaAttributeSet(OPAQUE);
            if (opaque == null)
                opaque = new AttributeSet();
            opaque.setAttribute(detail, xml);
            item.setMetaAttributeSet(OPAQUE, opaque);
        } catch (RuntimeException e) {
            Log.w(TAG, "opaque mirror failed", e);
        }
    }

    /**
     * The record on a marker; null if none. If only ATAK's opaque copy
     * exists (detail received while Blast wasn't loaded), it is adopted
     * into Blast's metadata.
     */
    public static BlastRecord read(MapItem item) {
        if (!(item instanceof Marker))
            return null;
        try {
            String xml = item.getMetaString(META, null);
            if (xml != null) {
                BlastRecord r = BlastCodec.fromXml(xml);
                if (r != null)
                    return r;
            }
            AttributeSet opaque = item.getMetaAttributeSet(OPAQUE);
            if (opaque != null && opaque.containsAttribute(BlastCodec.DETAIL)) {
                String s = opaque.getStringAttribute(BlastCodec.DETAIL, null);
                BlastRecord r = BlastCodec.fromXml(s);
                if (r != null) {
                    item.setMetaString(META, BlastCodec.toXml(r));
                    item.setMetaBoolean(META_FLAG, true);
                    return r;
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "unreadable blast data on " + item.getUID(), e);
        }
        return null;
    }

    /** The UXO record on a marker; null if none (adopts the opaque copy). */
    public static UxoRecord readUxo(MapItem item) {
        if (!(item instanceof Marker))
            return null;
        try {
            String xml = item.getMetaString(META_UXO, null);
            if (xml != null) {
                UxoRecord r = UxoCodec.fromXml(xml);
                if (r != null)
                    return r;
            }
            AttributeSet opaque = item.getMetaAttributeSet(OPAQUE);
            if (opaque != null && opaque.containsAttribute(UxoCodec.DETAIL)) {
                String s = opaque.getStringAttribute(UxoCodec.DETAIL, null);
                UxoRecord r = UxoCodec.fromXml(s);
                if (r != null) {
                    item.setMetaString(META_UXO, UxoCodec.toXml(r));
                    item.setMetaBoolean(META_FLAG, true);
                    return r;
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "unreadable UXO data on " + item.getUID(), e);
        }
        return null;
    }

    /** The nuclear record on a marker; null if none (adopts the opaque copy). */
    public static NucRecord readNuc(MapItem item) {
        if (!(item instanceof Marker))
            return null;
        try {
            String xml = item.getMetaString(META_NUC, null);
            if (xml != null) {
                NucRecord r = NucCodec.fromXml(xml);
                if (r != null)
                    return r;
            }
            AttributeSet opaque = item.getMetaAttributeSet(OPAQUE);
            if (opaque != null && opaque.containsAttribute(NucCodec.DETAIL)) {
                String s = opaque.getStringAttribute(NucCodec.DETAIL, null);
                NucRecord r = NucCodec.fromXml(s);
                if (r != null) {
                    item.setMetaString(META_NUC, NucCodec.toXml(r));
                    item.setMetaBoolean(META_FLAG, true);
                    return r;
                }
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "unreadable nuclear data on " + item.getUID(), e);
        }
        return null;
    }

    /** True for one of our rings or fallout outlines. */
    public static boolean isRing(MapItem item) {
        if (!(item instanceof DrawingCircle) && !(item instanceof DrawingShape))
            return false;
        String uid = item.getUID();
        return uid != null && suffixOf(uid) != null
                && item.hasMetaValue(META_PARENT);
    }

    /** The zone marker for a marker or one of its rings, else null. */
    public Marker zoneMarkerFor(MapItem item) {
        if (item == null)
            return null;
        if (item instanceof Marker && isIncident(item))
            return (Marker) item;
        if (item instanceof DrawingCircle || item instanceof DrawingShape) {
            String parent = item.getMetaString(META_PARENT, null);
            if (parent == null) {
                String uid = item.getUID();
                String sfx = uid == null ? null : suffixOf(uid);
                if (sfx != null)
                    parent = uid.substring(0, uid.length() - sfx.length());
            }
            if (parent != null)
                return liveMarker(parent);
        }
        return null;
    }

    /**
     * All incident markers on the map, from three sources so no single ATAK
     * lookup can hide them: the registry, ATAK's UID index
     * (RootMapGroup.getAllItems) and a walk of the map groups. Only items
     * that are on the map (in a group) count.
     */
    public List<Marker> all() {
        final java.util.LinkedHashMap<String, Marker> found =
                new java.util.LinkedHashMap<>();
        int fromRegistry = 0, fromIndex = 0, fromWalk = 0;
        for (String uid : new ArrayList<>(registry)) {
            Marker m = liveMarker(uid);
            if (m != null && found.put(m.getUID(), m) == null)
                fromRegistry++;
        }
        try {
            for (MapItem mi : mapView.getRootGroup().getAllItems()) {
                if (mi instanceof Marker && isLive(mi)
                        && (mi.hasMetaValue(META) || mi.hasMetaValue(META_UXO)
                                || mi.hasMetaValue(META_NUC)
                                || mi.hasMetaValue(OPAQUE))
                        && isIncident(mi)
                        && found.put(mi.getUID(), (Marker) mi) == null)
                    fromIndex++;
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "index listing failed", e);
        }
        final List<Marker> walked = new ArrayList<>();
        try {
            mapView.getRootGroup().deepForEachItem(
                    new MapGroup.MapItemsCallback() {
                        @Override
                        public boolean onItemFunction(MapItem item) {
                            if (item instanceof Marker
                                    && (item.hasMetaValue(META)
                                            || item.hasMetaValue(META_UXO)
                                            || item.hasMetaValue(META_NUC)
                                            || item.hasMetaValue(OPAQUE))
                                    && isIncident(item))
                                walked.add((Marker) item);
                            return false;
                        }
                    });
        } catch (RuntimeException e) {
            Log.w(TAG, "group walk failed", e);
        }
        for (Marker m : walked)
            if (found.put(m.getUID(), m) == null)
                fromWalk++;
        for (String uid : found.keySet())
            register(uid);
        // Forget UIDs ATAK doesn't know at all any more (deleted while Blast
        // wasn't loaded). Items not loaded yet are found again by the index
        // or the walk and re-registered.
        boolean pruned = false;
        for (String uid : new ArrayList<>(registry)) {
            if (!found.containsKey(uid) && mapView.getMapItem(uid) == null)
                pruned |= registry.remove(uid);
        }
        if (pruned)
            saveRegistry();
        lastScan = "found " + found.size() + " (registry " + fromRegistry
                + "/" + registry.size() + ", index +" + fromIndex
                + ", walk +" + fromWalk + ", walk total " + walked.size() + ")";
        return new ArrayList<>(found.values());
    }

    /**
     * Pick up markers whose {@code __blast} detail ATAK stored as opaque
     * text before the plugin loaded.
     */
    public int adoptAll() {
        final int[] n = { 0 };
        try {
            mapView.getRootGroup().deepForEachItem(
                    new MapGroup.MapItemsCallback() {
                        @Override
                        public boolean onItemFunction(MapItem item) {
                            if (item instanceof Marker
                                    && !item.getMetaBoolean(META_FLAG, false)
                                    && item.hasMetaValue(OPAQUE)
                                    && isIncident(item))
                                n[0]++;
                            return false;
                        }
                    });
        } catch (RuntimeException e) {
            Log.w(TAG, "adopt failed", e);
        }
        return n[0];
    }

    /** The incident's name (its callsign). */
    public static String nameOf(Marker m) {
        String cs = m.getMetaString("callsign", null);
        if (cs == null || cs.isEmpty())
            cs = m.getTitle();
        return cs == null ? "" : cs;
    }

    /**
     * Incident number: the one stored in the detail (survives renames),
     * else the "BLAST n" at the start of the name (0.1-0.5 incidents).
     */
    public static int numberOf(Marker m) {
        NucRecord n = readNuc(m);
        if (n != null)
            return Math.max(n.number, Names.nucNumberIn(nameOf(m)));
        UxoRecord u = readUxo(m);
        if (u != null)
            return Math.max(u.number, Names.uxoNumberIn(nameOf(m)));
        BlastRecord r = read(m);
        int stored = r != null ? r.number : 0;
        return Math.max(stored, Names.numberIn(nameOf(m)));
    }

    /** Next BLAST number (bomb-threat card incidents only). */
    public int nextNumber() {
        int max = 0;
        for (Marker m : all())
            if (!isUxo(m) && !isNuc(m))
                max = Math.max(max, numberOf(m));
        return max + 1;
    }

    /** Next UXO number (UXO incidents have their own sequence). */
    public int nextUxoNumber() {
        int max = 0;
        for (Marker m : all())
            if (isUxo(m))
                max = Math.max(max, numberOf(m));
        return max + 1;
    }

    /** Next NUC number (own sequence). */
    public int nextNucNumber() {
        int max = 0;
        for (Marker m : all())
            if (isNuc(m))
                max = Math.max(max, numberOf(m));
        return max + 1;
    }

    // ------------------------------------------------------------------
    // Writing

    /** Place a new zone; returns its marker. */
    public Marker create(GeoPoint at, BlastRecord r) {
        int number = nextNumber();
        r = r.withNumber(number);
        String name = Names.initial(number, r.rowLabel);
        String uid = UUID.randomUUID().toString();
        Marker m = new PlacePointTool.MarkerCreator(at)
                .setUid(uid)
                .setType(MARKER_TYPE)
                .setCallsign(name)
                .showCotDetails(false)
                .setShowNewRadial(false)
                .setAllowDuplicativeNames(true)
                .setArchive(true)
                .placePoint();
        if (m == null)
            return null;
        register(m.getUID());
        write(m, r);
        return m;
    }

    /** Place a new UXO incident; returns its marker. */
    public Marker createUxo(GeoPoint at, UxoRecord r) {
        int number = nextUxoNumber();
        r = r.withNumber(number);
        String name = Names.initialUxo(number, r.plainLabel());
        String uid = UUID.randomUUID().toString();
        Marker m = new PlacePointTool.MarkerCreator(at)
                .setUid(uid)
                .setType(MARKER_TYPE)
                .setCallsign(name)
                .showCotDetails(false)
                .setShowNewRadial(false)
                .setAllowDuplicativeNames(true)
                .setArchive(true)
                .placePoint();
        if (m == null)
            return null;
        register(m.getUID());
        writeUxo(m, r);
        return m;
    }

    /** Place a new nuclear reference incident; returns its marker. */
    public Marker createNuc(GeoPoint at, NucRecord r) {
        int number = nextNucNumber();
        r = r.withNumber(number);
        String name = Names.initialNuc(number, r.plainLabel());
        String uid = UUID.randomUUID().toString();
        Marker m = new PlacePointTool.MarkerCreator(at)
                .setUid(uid)
                .setType(MARKER_TYPE)
                .setCallsign(name)
                .showCotDetails(false)
                .setShowNewRadial(false)
                .setAllowDuplicativeNames(true)
                .setArchive(true)
                .placePoint();
        if (m == null)
            return null;
        register(m.getUID());
        writeNuc(m, r);
        return m;
    }

    /** Store a nuclear record and bring its rings and labels up to date. */
    public boolean writeNuc(Marker m, NucRecord r) {
        if (!isLive(m)) {
            Log.w(TAG, "not writing to removed incident " + m.getUID());
            return false;
        }
        storeNuc(m, NucCodec.toXml(r));
        String current = nameOf(m);
        String wanted = Names.withStatus(current.isEmpty()
                ? Names.initialNuc(Math.max(1, r.number), r.plainLabel())
                : current, r.isCleared());
        if (!wanted.equals(current))
            setName(m, wanted);
        m.setMetaString("remarks", NucText.mergeRemarks(
                m.getMetaString("remarks", null), r));
        ensureNuc(m, r);
        m.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
        return true;
    }

    /** Create or restyle a nuclear incident's circles and fallout outlines. */
    public void ensureNuc(Marker m, NucRecord r) {
        if (!isLive(m) || r == null)
            return;
        watch(m);
        followMoves(m);
        boolean grey = r.isCleared();
        for (NucText.Ring ring : NucText.Ring.values()) {
            int stroke, fill;
            int line = Shape.BASIC_LINE_STYLE_SOLID;
            double weight = 3d;
            switch (ring) {
                case SDZ:
                    stroke = NUC_SDZ;
                    fill = NUC_SDZ_FILL;
                    break;
                case MDZ:
                    stroke = NUC_MDZ;
                    fill = NUC_MDZ_FILL;
                    break;
                case LDZ:
                    stroke = NUC_LDZ;
                    fill = NUC_LDZ_FILL;
                    break;
                case BURN:
                    stroke = NUC_BURN;
                    fill = NO_FILL;
                    line = Shape.BASIC_LINE_STYLE_DASHED;
                    break;
                case RAD:
                    stroke = NUC_RAD;
                    fill = NO_FILL;
                    break;
                default:
                    stroke = NUC_GI;
                    fill = NO_FILL;
                    line = Shape.BASIC_LINE_STYLE_DOTTED;
                    weight = 2d;
            }
            if (grey) {
                stroke = GREY;
                fill = fill == NO_FILL ? NO_FILL : GREY_FILL;
            }
            circle(m, ring.suffix, ring.radiusM(r),
                    NucText.ringTitle(r, ring), stroke, fill, line, weight);
        }
        ensureFallout(m, r);
    }

    /** Draw, move or remove the fallout outlines to match the record. */
    void ensureFallout(Marker m, NucRecord r) {
        GeoPoint gz = m.getPoint();
        boolean draw = r.drawsFallout() && gz != null && gz.isValid();
        for (int i = 0; i < NucModel.FALLOUT_RATES.length; i++) {
            int rate = NucModel.FALLOUT_RATES[i];
            String uid = m.getUID() + NucText.falloutSuffix(rate);
            MapItem existing = liveItem(uid);
            if (!draw) {
                if (existing != null)
                    removePart(existing);
                continue;
            }
            DrawingShape sh;
            boolean added = false;
            if (existing instanceof DrawingShape) {
                sh = (DrawingShape) existing;
            } else if (existing != null) {
                Log.w(TAG, "uid " + uid + " is not a shape; not drawing");
                continue;
            } else {
                sh = new DrawingShape(mapView, uid);
                ATAKUtilities.setAuthorInformation(sh);
                intendedRemovals.remove(uid);
                added = true;
            }
            double[][] ll = NucGeo.fallout(r, rate, gz.getLatitude(),
                    gz.getLongitude());
            if (ll == null)
                continue;
            GeoPointMetaData[] pts = new GeoPointMetaData[ll.length];
            for (int k = 0; k < ll.length; k++)
                pts[k] = GeoPointMetaData.wrap(new GeoPoint(ll[k][0], ll[k][1]));
            sh.setPoints(pts);
            sh.setClosed(true);
            sh.setMetaString(META_PARENT, m.getUID());
            sh.setMetaBoolean("editable", false);
            sh.setMovable(false);
            sh.setTitle(NucText.falloutTitle(r, rate));
            sh.setStrokeColor(r.isCleared() ? GREY : NUC_FO[i]);
            sh.setFillColor(r.isCleared() ? GREY_FILL : NUC_FO_FILL[i]);
            sh.setStrokeWeight(2d);
            if (added) {
                MapGroup g = DrawingToolsMapComponent.getGroup();
                if (g == null)
                    g = mapView.getRootGroup();
                g.addItem(sh);
            }
            sh.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
            watch(sh);
        }
    }

    /** Remove one part on purpose, without deleting the whole incident. */
    private void removePart(MapItem item) {
        watched.remove(item);
        item.removeOnGroupChangedListener(groupWatcher);
        // The plugin's ITEM_REMOVED listener also sees this removal; it must
        // not be taken as a delete of the whole incident.
        intendedRemovals.add(item.getUID());
        item.removeFromGroup();
    }

    /** UIDs removed on purpose (see {@link #removePart}). */
    private final java.util.Set<String> intendedRemovals = java.util.Collections
            .newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());

    /** Markers whose fallout follows them when moved. */
    private final java.util.Set<PointMapItem> following = java.util.Collections
            .synchronizedSet(java.util.Collections.newSetFromMap(
                    new java.util.WeakHashMap<PointMapItem, Boolean>()));

    private void followMoves(Marker m) {
        if (following.add(m))
            m.addOnPointChangedListener(moveWatcher);
    }

    private final PointMapItem.OnPointChangedListener moveWatcher =
            new PointMapItem.OnPointChangedListener() {
                @Override
                public void onPointChanged(final PointMapItem item) {
                    main.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (item instanceof Marker && isLive(item)) {
                                    NucRecord r = readNuc(item);
                                    if (r != null)
                                        ensureFallout((Marker) item, r);
                                }
                            } catch (RuntimeException e) {
                                Log.e(TAG, "fallout redraw failed", e);
                            }
                        }
                    });
                }
            };

    /** One anchored, read-only circle. */
    private void circle(Marker m, String suffix, double radiusM, String title,
            int stroke, int fill, int lineStyle, double weight) {
        String uid = m.getUID() + suffix;
        MapItem existing = liveItem(uid);
        DrawingCircle c;
        boolean added = false;
        if (existing instanceof DrawingCircle) {
            c = (DrawingCircle) existing;
        } else if (existing != null) {
            Log.w(TAG, "uid " + uid + " is not a circle; not drawing ring");
            return;
        } else {
            c = new DrawingCircle(mapView, uid);
            ATAKUtilities.setAuthorInformation(c);
            c.setCenterMarker(m);
            added = true;
        }
        c.setMetaString(META_PARENT, m.getUID());
        c.setMetaBoolean("editable", false);
        c.setMovable(false);
        if (Math.abs(c.getRadius() - radiusM) > 0.01)
            c.setRadius(radiusM);
        c.setTitle(title);
        c.setStrokeColor(stroke);
        c.setFillColor(fill);
        c.setStrokeWeight(weight);
        c.setBasicLineStyle(lineStyle);
        if (c.getCenterMarker() != m)
            c.setCenterMarker(m);
        if (added) {
            MapGroup g = DrawingToolsMapComponent.getGroup();
            if (g == null)
                g = mapView.getRootGroup();
            g.addItem(c);
            if (c.getCenterMarker() != m)
                c.setCenterMarker(m);
        }
        c.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
        watch(c);
    }

    /**
     * Store a UXO record on its marker and bring the cordon and labels up
     * to date. False (and nothing changed) if the marker is gone.
     */
    public boolean writeUxo(Marker m, UxoRecord r) {
        if (!isLive(m)) {
            Log.w(TAG, "not writing to removed incident " + m.getUID());
            return false;
        }
        storeUxo(m, UxoCodec.toXml(r));
        String current = nameOf(m);
        String wanted = Names.withStatus(current.isEmpty()
                ? Names.initialUxo(Math.max(1, r.number), r.plainLabel())
                : current, r.isCleared());
        if (!wanted.equals(current))
            setName(m, wanted);
        m.setMetaString("remarks", UxoText.mergeRemarks(
                m.getMetaString("remarks", null), r));
        ensureCordon(m, r);
        m.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
        return true;
    }

    /** Draw or update whatever the incident needs (either kind). */
    public void ensureAll(Marker m) {
        NucRecord n = readNuc(m);
        if (n != null) {
            ensureNuc(m, n);
            return;
        }
        UxoRecord u = readUxo(m);
        if (u != null)
            ensureCordon(m, u);
        else
            ensureRings(m, read(m));
    }

    /**
     * Store a record on its marker and bring rings and labels up to date.
     * Returns false (and changes nothing) if the marker is no longer on the
     * map, so a stale card can't resurrect a deleted incident.
     */
    public boolean write(Marker m, BlastRecord r) {
        if (!isLive(m)) {
            Log.w(TAG, "not writing to removed incident " + m.getUID());
            return false;
        }
        store(m, BlastCodec.toXml(r));
        // The name is the callsign, which anyone can change (with or
        // without Blast). Only the CLEARED suffix follows the status here.
        String current = nameOf(m);
        String wanted = Names.withStatus(current.isEmpty()
                ? Names.initial(Math.max(1, r.number), r.rowLabel) : current,
                r.isCleared());
        if (!wanted.equals(current))
            setName(m, wanted);
        // Remarks can be edited by anyone; keep their text, replace ours.
        m.setMetaString("remarks", Labels.mergeRemarks(
                m.getMetaString("remarks", null), r));
        ensureRings(m, r);
        m.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
        return true;
    }

    private static void setName(Marker m, String name) {
        m.setMetaString("callsign", name);
        m.setTitle(name);
    }

    /**
     * Rename an incident. Returns false if the name is empty after
     * cleaning. The CLEARED suffix is kept or added to match the status.
     */
    public boolean rename(Marker m, String typed) {
        String clean = Names.sanitize(typed);
        NucRecord nr = readNuc(m);
        if (nr != null) {
            if (clean == null || !isLive(m))
                return false;
            if (nr.number == 0) {
                int n = numberOf(m);
                if (n > 0)
                    nr = nr.withNumber(n);
            }
            setName(m, Names.withStatus(clean, nr.isCleared()));
            return writeNuc(m, nr);
        }
        UxoRecord u = readUxo(m);
        if (u != null) {
            if (clean == null || !isLive(m))
                return false;
            if (u.number == 0) {
                int n = numberOf(m);
                if (n > 0)
                    u = u.withNumber(n);
            }
            setName(m, Names.withStatus(clean, u.isCleared()));
            return writeUxo(m, u);
        }
        BlastRecord r = read(m);
        if (clean == null || r == null || !isLive(m))
            return false;
        // Record the number in the detail before the old name (which may
        // hold it, e.g. "BLAST 3 ...") is replaced, so the incident still
        // counts for numbering after the rename.
        if (r.number == 0) {
            int n = numberOf(m);
            if (n > 0)
                r = r.withNumber(n);
        }
        setName(m, Names.withStatus(clean, r.isCleared()));
        return write(m, r);
    }

    /**
     * Create missing rings and restyle existing ones. Does nothing for a
     * marker that is no longer on the map (rings must never be orphaned).
     */
    public void ensureRings(Marker m, BlastRecord r) {
        if (!isLive(m))
            return;
        if (r == null)
            r = read(m);
        if (r == null)
            return;
        watch(m);
        ring(m, r, true);
        ring(m, r, false);
    }

    private void ring(Marker m, BlastRecord r, boolean mandatory) {
        String uid = m.getUID() + (mandatory ? MAND_SUFFIX : PREF_SUFFIX);
        // A removed ring can linger in ATAK's UID index; only reuse it if it
        // is still on the map, otherwise draw a fresh one.
        MapItem existing = liveItem(uid);
        DrawingCircle c;
        boolean added = false;
        if (existing instanceof DrawingCircle) {
            c = (DrawingCircle) existing;
        } else if (existing != null) {
            Log.w(TAG, "uid " + uid + " is not a circle; not drawing ring");
            return;
        } else {
            c = new DrawingCircle(mapView, uid);
            ATAKUtilities.setAuthorInformation(c);
            c.setCenterMarker(m);
            added = true;
        }
        StandoffRow row = r.row();
        double radiusM = mandatory ? row.mandatoryRadiusM()
                : row.preferredRadiusM();
        c.setMetaString(META_PARENT, m.getUID());
        c.setMetaBoolean("editable", false);
        c.setMovable(false);
        if (Math.abs(c.getRadius() - radiusM) > 0.01)
            c.setRadius(radiusM);
        c.setTitle(mandatory ? Labels.mandatoryRing(r)
                : Labels.preferredRing(r));
        if (r.isCleared()) {
            c.setStrokeColor(GREY);
            c.setFillColor(GREY_FILL);
        } else if (mandatory) {
            c.setStrokeColor(RED);
            c.setFillColor(RED_FILL);
        } else {
            c.setStrokeColor(GREEN);
            c.setFillColor(AMBER_FILL);
        }
        c.setStrokeWeight(mandatory ? 3d : 4d);
        if (c.getCenterMarker() != m)
            c.setCenterMarker(m);
        if (added) {
            MapGroup g = DrawingToolsMapComponent.getGroup();
            if (g == null)
                g = mapView.getRootGroup();
            g.addItem(c);
            // Re-anchor after the group add (which may reset the centre).
            if (c.getCenterMarker() != m)
                c.setCenterMarker(m);
        }
        c.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
        watch(c);
    }

    /** Create or restyle a UXO incident's cordon circle. */
    public void ensureCordon(Marker m, UxoRecord r) {
        if (!isLive(m) || r == null)
            return;
        watch(m);
        String uid = m.getUID() + CORDON_SUFFIX;
        MapItem existing = liveItem(uid);
        DrawingCircle c;
        boolean added = false;
        if (existing instanceof DrawingCircle) {
            c = (DrawingCircle) existing;
        } else if (existing != null) {
            Log.w(TAG, "uid " + uid + " is not a circle; not drawing cordon");
            return;
        } else {
            c = new DrawingCircle(mapView, uid);
            ATAKUtilities.setAuthorInformation(c);
            c.setCenterMarker(m);
            added = true;
        }
        c.setMetaString(META_PARENT, m.getUID());
        c.setMetaBoolean("editable", false);
        c.setMovable(false);
        if (Math.abs(c.getRadius() - r.cordonM) > 0.01)
            c.setRadius(r.cordonM);
        c.setTitle(UxoText.ringTitle(r));
        c.setStrokeColor(r.isCleared() ? GREY : ORANGE);
        c.setFillColor(r.isCleared() ? GREY_FILL : ORANGE_FILL);
        c.setStrokeWeight(4d);
        if (c.getCenterMarker() != m)
            c.setCenterMarker(m);
        if (added) {
            MapGroup g = DrawingToolsMapComponent.getGroup();
            if (g == null)
                g = mapView.getRootGroup();
            g.addItem(c);
            if (c.getCenterMarker() != m)
                c.setCenterMarker(m);
        }
        c.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
        watch(c);
    }

    /** The marker and its rings, marker first (send order). */
    public List<MapItem> items(Marker m) {
        List<MapItem> out = new ArrayList<>();
        out.add(m);
        for (String sfx : SUFFIXES) {
            MapItem part = liveItem(m.getUID() + sfx);
            if (part != null)
                out.add(part);
        }
        return out;
    }

    /** Remove the rings that belong to a marker UID (local only). */
    public void removeRings(String markerUid) {
        for (String sfx : SUFFIXES) {
            MapItem c = liveItem(markerUid + sfx);
            if (c != null)
                c.removeFromGroup();
        }
    }

    /** Remove the whole incident (marker and both rings) on this device. */
    public void delete(Marker m) {
        deleteIncident(m.getUID());
    }

    /** Remove the marker and rings for a marker UID, whichever still exist. */
    public void deleteIncident(String markerUid) {
        if (markerUid == null)
            return;
        unregister(markerUid);
        MapItem m = liveItem(markerUid);
        if (m != null)
            m.removeFromGroup();
        removeRings(markerUid);
    }

    /** Marker UID for a zone marker or ring UID. */
    static String incidentUid(String uid) {
        if (uid == null)
            return null;
        String sfx = suffixOf(uid);
        return sfx == null ? uid : uid.substring(0, uid.length() - sfx.length());
    }

    /** The ring suffix a UID ends with, or null. */
    static String suffixOf(String uid) {
        for (String sfx : SUFFIXES)
            if (uid.endsWith(sfx))
                return sfx;
        return null;
    }

    // ------------------------------------------------------------------
    // Deleting any part of an incident (ATAK delete, remote delete, the
    // pane) removes all of it. ATAK can fire "removed" when it moves an item
    // between groups, so the item must still be missing after a short wait.

    static final long GONE_CHECK_MS = 750;

    private final Handler main = new Handler(Looper.getMainLooper());

    /**
     * Items already carrying {@link #groupWatcher}. ATAK's listener list
     * keeps duplicates, so each item must be watched once. Weak, so removed
     * items can be collected.
     */
    private final java.util.Set<MapItem> watched = java.util.Collections
            .synchronizedSet(java.util.Collections.newSetFromMap(
                    new java.util.WeakHashMap<MapItem, Boolean>()));

    private void watch(MapItem item) {
        if (watched.add(item))
            item.addOnGroupChangedListener(groupWatcher);
    }

    private final MapItem.OnGroupChangedListener groupWatcher =
            new MapItem.OnGroupChangedListener() {
                @Override
                public void onItemAdded(MapItem item, MapGroup group) {
                }

                @Override
                public void onItemRemoved(MapItem item, MapGroup group) {
                    partRemoved(item.getUID());
                }
            };

    /** Called when a zone marker or ring may have been deleted. */
    public void partRemoved(final String uid) {
        if (uid != null && intendedRemovals.remove(uid))
            return;
        main.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    if (liveItem(uid) == null)
                        deleteIncident(incidentUid(uid));
                } catch (RuntimeException e) {
                    Log.e(TAG, "incident cleanup failed for " + uid, e);
                }
            }
        }, GONE_CHECK_MS);
    }

    /** Stop watching (plugin stop). Items keep their data. */
    public void dispose() {
        main.removeCallbacksAndMessages(null);
        List<MapItem> items;
        synchronized (watched) {
            items = new ArrayList<>(watched);
            watched.clear();
        }
        for (MapItem mi : items)
            mi.removeOnGroupChangedListener(groupWatcher);
        List<PointMapItem> moving;
        synchronized (following) {
            moving = new ArrayList<>(following);
            following.clear();
        }
        for (PointMapItem pm : moving)
            pm.removeOnPointChangedListener(moveWatcher);
    }
}
