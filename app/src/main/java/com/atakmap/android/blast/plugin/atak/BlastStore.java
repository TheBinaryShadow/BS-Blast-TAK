package com.atakmap.android.blast.plugin.atak;

import android.os.Handler;
import android.os.Looper;

import com.atakmap.android.blast.plugin.core.BlastCodec;
import com.atakmap.android.blast.plugin.core.BlastRecord;
import com.atakmap.android.blast.plugin.core.StandoffRow;
import com.atakmap.android.blast.plugin.core.Labels;
import com.atakmap.android.blast.plugin.core.Names;
import com.atakmap.android.drawing.DrawingToolsMapComponent;
import com.atakmap.android.drawing.mapItems.DrawingCircle;
import com.atakmap.android.maps.MapGroup;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.user.PlacePointTool;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.coremap.log.Log;
import com.atakmap.coremap.maps.coords.GeoPoint;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import gov.tak.api.util.AttributeSet;

/**
 * Zones on the map. A zone is a normal marker (CoT a-u-G, readable by any
 * TAK client) carrying the {@code __blast} detail, plus two native ATAK
 * drawing circles anchored to it:
 * <ul>
 * <li>{@code <uid>.mand} — mandatory evacuation (red, filled)</li>
 * <li>{@code <uid>.pref} — preferred evacuation line (green), amber fill
 * for the shelter-in-place band inside it</li>
 * </ul>
 * Ring UIDs are derived from the marker UID, so every client agrees on them
 * and updates merge. The rings are anchored to the marker (CoT
 * {@code p-p-CenterAnchor}), so moving the marker moves them everywhere.
 */
public final class BlastStore {

    private static final String TAG = "BlastStore";

    /** Marker metadata: the record as {@code <__blast .../>} XML. */
    public static final String META = "blast.data";
    /** Marker metadata flag for fast lookup. */
    public static final String META_FLAG = "blast.zone";
    /** Ring metadata: owning marker UID. */
    public static final String META_PARENT = "blast.parent";
    /** ATAK's store for details no handler claimed. */
    static final String OPAQUE = "opaque-details";

    /** Hostile ground (red diamond). 0.1 used a-u-G; those markers keep it. */
    public static final String MARKER_TYPE = "a-h-G";
    public static final String MAND_SUFFIX = ".mand";
    public static final String PREF_SUFFIX = ".pref";

    static final int RED = 0xFFD50000;
    static final int RED_FILL = 0x55D50000;
    static final int GREEN = 0xFF00C853;
    static final int AMBER_FILL = 0x33FFAB00;
    static final int GREY = 0xFF9E9E9E;
    static final int GREY_FILL = 0x149E9E9E;


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
        this.prefs = android.preference.PreferenceManager
                .getDefaultSharedPreferences(mapView.getContext());
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
        if (mi instanceof Marker && read(mi) != null)
            return (Marker) mi;
        return null;
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

    /** The record on a marker, adopting opaque data if needed; null if none. */
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

    /** True for one of our rings. */
    public static boolean isRing(MapItem item) {
        if (!(item instanceof DrawingCircle))
            return false;
        String uid = item.getUID();
        return uid != null
                && (uid.endsWith(MAND_SUFFIX) || uid.endsWith(PREF_SUFFIX))
                && item.hasMetaValue(META_PARENT);
    }

    /** The zone marker for a marker or one of its rings, else null. */
    public Marker zoneMarkerFor(MapItem item) {
        if (item == null)
            return null;
        if (item instanceof Marker && read(item) != null)
            return (Marker) item;
        if (item instanceof DrawingCircle) {
            String parent = item.getMetaString(META_PARENT, null);
            if (parent == null) {
                String uid = item.getUID();
                if (uid != null && uid.endsWith(MAND_SUFFIX))
                    parent = uid.substring(0, uid.length() - MAND_SUFFIX.length());
                else if (uid != null && uid.endsWith(PREF_SUFFIX))
                    parent = uid.substring(0, uid.length() - PREF_SUFFIX.length());
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
                        && (mi.hasMetaValue(META) || mi.hasMetaValue(OPAQUE))
                        && read(mi) != null
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
                                            || item.hasMetaValue(OPAQUE))
                                    && read(item) != null)
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
                                    && read(item) != null)
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
        BlastRecord r = read(m);
        int stored = r != null ? r.number : 0;
        return Math.max(stored, Names.numberIn(nameOf(m)));
    }

    public int nextNumber() {
        int max = 0;
        for (Marker m : all())
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

    /** Store a record on its marker and bring rings and labels up to date. */
    public void write(Marker m, BlastRecord r) {
        String xml = BlastCodec.toXml(r);
        m.setMetaString(META, xml);
        m.setMetaBoolean(META_FLAG, true);
        // Mirror into ATAK's opaque store so the detail survives (and is
        // re-sent) even while the plugin isn't loaded.
        try {
            AttributeSet opaque = m.getMetaAttributeSet(OPAQUE);
            if (opaque == null)
                opaque = new AttributeSet();
            opaque.setAttribute(BlastCodec.DETAIL, xml);
            m.setMetaAttributeSet(OPAQUE, opaque);
        } catch (RuntimeException e) {
            Log.w(TAG, "opaque mirror failed", e);
        }
        // The name is the callsign, which anyone can change (with or
        // without Blast). Only the CLEARED suffix follows the status here.
        String current = nameOf(m);
        String wanted = Names.withStatus(current.isEmpty()
                ? Names.initial(Math.max(1, r.number), r.rowLabel) : current,
                r.isCleared());
        if (!wanted.equals(current))
            setName(m, wanted);
        m.setMetaString("remarks", Labels.remarks(r));
        ensureRings(m, r);
        m.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
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
        BlastRecord r = read(m);
        if (clean == null || r == null)
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
        write(m, r);
        return true;
    }

    /** Create missing rings and restyle existing ones. */
    public void ensureRings(Marker m, BlastRecord r) {
        if (r == null)
            r = read(m);
        if (r == null)
            return;
        m.addOnGroupChangedListener(groupWatcher);
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
        c.addOnGroupChangedListener(groupWatcher);
    }

    /** The marker and its rings, marker first (send order). */
    public List<MapItem> items(Marker m) {
        List<MapItem> out = new ArrayList<>();
        out.add(m);
        MapItem pref = liveItem(m.getUID() + PREF_SUFFIX);
        if (pref != null)
            out.add(pref);
        MapItem mand = liveItem(m.getUID() + MAND_SUFFIX);
        if (mand != null)
            out.add(mand);
        return out;
    }

    /** Remove the rings that belong to a marker UID (local only). */
    public void removeRings(String markerUid) {
        for (String sfx : new String[] { MAND_SUFFIX, PREF_SUFFIX }) {
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
        if (uid.endsWith(MAND_SUFFIX))
            return uid.substring(0, uid.length() - MAND_SUFFIX.length());
        if (uid.endsWith(PREF_SUFFIX))
            return uid.substring(0, uid.length() - PREF_SUFFIX.length());
        return uid;
    }

    // ------------------------------------------------------------------
    // Deleting any part of an incident (ATAK delete, remote delete, the
    // pane) removes all of it. ATAK can fire "removed" when it moves an item
    // between groups, so the item must still be missing after a short wait.

    static final long GONE_CHECK_MS = 750;

    private final Handler main = new Handler(Looper.getMainLooper());

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
        for (Marker m : all()) {
            for (MapItem mi : items(m))
                mi.removeOnGroupChangedListener(groupWatcher);
        }
    }
}
