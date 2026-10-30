package com.atakmap.android.blast.plugin.atak;

import android.os.Handler;
import android.os.Looper;

import com.atakmap.android.blast.plugin.core.BlastCodec;
import com.atakmap.android.blast.plugin.core.BlastRecord;
import com.atakmap.android.blast.plugin.core.StandoffRow;
import com.atakmap.android.blast.plugin.core.Labels;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final Pattern NUMBER = Pattern.compile("^BLAST (\\d+)\\b");

    private final MapView mapView;

    public BlastStore(MapView mapView) {
        this.mapView = mapView;
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
            if (parent != null) {
                MapItem m = mapView.getMapItem(parent);
                if (m instanceof Marker && read(m) != null)
                    return (Marker) m;
            }
        }
        return null;
    }

    /** All zone markers on the map. */
    public List<Marker> all() {
        List<Marker> out = new ArrayList<>();
        try {
            for (MapItem mi : mapView.getRootGroup().deepFindItems(META_FLAG,
                    "true"))
                if (mi instanceof Marker && read(mi) != null)
                    out.add((Marker) mi);
        } catch (RuntimeException e) {
            Log.w(TAG, "zone listing failed", e);
        }
        return out;
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

    /** "BLAST 3 ..." → 3, else 0. */
    public static int numberOf(Marker m) {
        String cs = m.getMetaString("callsign", m.getTitle());
        if (cs == null)
            return 0;
        Matcher mt = NUMBER.matcher(cs);
        if (mt.find()) {
            try {
                return Integer.parseInt(mt.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
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
        String name = Labels.markerName(number, r);
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
        int number = numberOf(m);
        if (number > 0) {
            String name = Labels.markerName(number, r);
            m.setMetaString("callsign", name);
            m.setTitle(name);
        }
        m.setMetaString("remarks", Labels.remarks(r));
        ensureRings(m, r);
        m.persist(mapView.getMapEventDispatcher(), null, BlastStore.class);
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
        MapItem existing = mapView.getMapItem(uid);
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
        MapItem pref = mapView.getMapItem(m.getUID() + PREF_SUFFIX);
        if (pref != null)
            out.add(pref);
        MapItem mand = mapView.getMapItem(m.getUID() + MAND_SUFFIX);
        if (mand != null)
            out.add(mand);
        return out;
    }

    /** Remove the rings that belong to a marker UID (local only). */
    public void removeRings(String markerUid) {
        for (String sfx : new String[] { MAND_SUFFIX, PREF_SUFFIX }) {
            MapItem c = mapView.getMapItem(markerUid + sfx);
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
        MapItem m = mapView.getMapItem(markerUid);
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
                    if (mapView.getMapItem(uid) == null)
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
