package com.atakmap.android.blast.plugin.atak;

import android.os.Handler;
import android.os.Looper;

import com.atakmap.android.blast.plugin.core.UxoCodec;
import com.atakmap.android.blast.plugin.core.UxoRecord;
import com.atakmap.android.cot.detail.CotDetailHandler;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Marker;
import com.atakmap.comms.CommsMapComponent;
import com.atakmap.coremap.cot.event.CotAttribute;
import com.atakmap.coremap.cot.event.CotDetail;
import com.atakmap.coremap.cot.event.CotEvent;
import com.atakmap.coremap.log.Log;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reads and writes the {@code <__blast_uxo/>} detail on UXO incident
 * markers. Same pattern as {@link BlastDetailHandler}.
 */
public final class UxoDetailHandler extends CotDetailHandler {

    private static final String TAG = "UxoDetailHandler";

    /** Wait for the sender's cordon before drawing a missing one. */
    private static final long RING_GRACE_MS = 4000;

    private final BlastStore store;
    private final Handler main = new Handler(Looper.getMainLooper());
    private Runnable onChange;

    public UxoDetailHandler(BlastStore store) {
        super(UxoCodec.DETAIL);
        this.store = store;
    }

    /** Called (on the main thread) after an incident arrives or changes. */
    public void setOnChange(Runnable r) {
        onChange = r;
    }

    @Override
    public boolean isSupported(MapItem item, CotEvent event, CotDetail detail) {
        return item instanceof Marker;
    }

    @Override
    public boolean toCotDetail(MapItem item, CotEvent event, CotDetail root) {
        try {
            UxoRecord r = BlastStore.readUxo(item);
            if (r == null)
                return false;
            CotDetail d = new CotDetail(UxoCodec.DETAIL);
            for (Map.Entry<String, String> e : UxoCodec.encode(r).entrySet())
                d.setAttribute(e.getKey(), e.getValue());
            root.addChild(d);
            return true;
        } catch (RuntimeException e) {
            Log.e(TAG, "toCotDetail failed", e);
            return false;
        }
    }

    @Override
    public CommsMapComponent.ImportResult toItemMetadata(MapItem item,
            CotEvent event, CotDetail detail) {
        try {
            Map<String, String> a = new LinkedHashMap<>();
            CotAttribute[] attrs = detail.getAttributes();
            if (attrs != null)
                for (CotAttribute ca : attrs)
                    a.put(ca.getName(), ca.getValue());
            UxoRecord r = UxoCodec.decode(a);
            if (r == null) {
                Log.w(TAG, "ignoring unusable __blast_uxo on " + item.getUID());
                return CommsMapComponent.ImportResult.IGNORE;
            }
            BlastStore.storeUxo(item, UxoCodec.toXml(r));
            final String uid = item.getUID();
            store.register(uid);
            main.postDelayed(new Runnable() {
                @Override
                public void run() {
                    afterImport(uid);
                }
            }, RING_GRACE_MS);
            return CommsMapComponent.ImportResult.SUCCESS;
        } catch (RuntimeException e) {
            Log.e(TAG, "toItemMetadata failed", e);
            return CommsMapComponent.ImportResult.FAILURE;
        }
    }

    private void afterImport(String uid) {
        try {
            Marker m = store.liveMarker(uid);
            if (m != null) {
                store.register(uid);
                store.ensureCordon(m, BlastStore.readUxo(m));
            }
            if (onChange != null)
                onChange.run();
        } catch (RuntimeException e) {
            Log.e(TAG, "post-import update failed", e);
        }
    }

    public void dispose() {
        main.removeCallbacksAndMessages(null);
        onChange = null;
    }
}
