package com.atakmap.android.blast.plugin.atak;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.MapDataRef;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.menu.MapMenuButtonWidget;
import com.atakmap.android.menu.MapMenuHandler;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.android.widgets.MapWidget;
import com.atakmap.android.widgets.WidgetIcon;
import com.atakmap.coremap.log.Log;

import gov.tak.api.widgets.IMapMenuButtonWidget;

/**
 * Adds a "Blast" button to the radial menu:
 * on a zone marker or ring it opens that zone; on any other marker it opens
 * the threat picker to place a zone on that marker's position.
 * Added by a handler (not a menu factory) so ATAK's own buttons stay.
 */
public final class BlastMenuHandler implements MapMenuHandler {

    private static final String TAG = "BlastMenuHandler";

    public interface Listener {
        /** Open the pane on an existing zone. */
        void openZone(Marker zone);

        /** Open the pane ready to place a zone at this marker. */
        void placeAt(Marker target);
    }

    private final Context atakContext;
    private final String iconUri;
    private final BlastStore store;
    private final Listener listener;

    public BlastMenuHandler(Context atakContext, String pluginPackage,
            int iconRes, BlastStore store, Listener listener) {
        this.atakContext = atakContext;
        this.iconUri = "android.resource://" + pluginPackage + "/" + iconRes;
        this.store = store;
        this.listener = listener;
    }

    @Override
    public void updateMenu(MapItem item, MapMenuWidget menu) {
        try {
            if (item == null || menu == null)
                return;
            final Marker zone = store.zoneMarkerFor(item);
            final Marker target;
            if (zone != null) {
                target = null;
            } else if (item instanceof Marker && eligible((Marker) item)) {
                target = (Marker) item;
            } else {
                return;
            }
            MapMenuButtonWidget b = new MapMenuButtonWidget(atakContext);
            MapWidget first = menu.getChildCount() > 0 ? menu.getChildAt(0)
                    : null;
            if (first instanceof MapMenuButtonWidget) {
                MapMenuButtonWidget f = (MapMenuButtonWidget) first;
                b.setWidgetBackground(f.getWidgetBackground());
                b.setLayoutWeight(f.getLayoutWeight());
            }
            b.setWidgetIcon(new WidgetIcon(MapDataRef.parseUri(iconUri),
                    new Point(16, 16), 32, 32));
            b.setOnButtonClickHandler(
                    new IMapMenuButtonWidget.OnButtonClickHandler() {
                        @Override
                        public boolean isSupported(Object o) {
                            return true;
                        }

                        @Override
                        public void performAction(Object o) {
                            AtakBroadcast.getInstance().sendBroadcast(
                                    new Intent(MapMenuReceiver.HIDE_MENU));
                            if (zone != null)
                                listener.openZone(zone);
                            else
                                listener.placeAt(target);
                        }
                    });
            menu.addWidget(b);
        } catch (RuntimeException e) {
            Log.e(TAG, "could not add radial button", e);
        }
    }

    /** Ordinary placed markers; not the self marker or shape helpers. */
    static boolean eligible(Marker m) {
        String type = m.getType();
        if (type == null || type.equals("self")
                || type.equals("shape_marker")
                || type.startsWith("b-m-r")
                || m.getMetaBoolean("nevercot", false)
                || m.getUID().equals(
                        com.atakmap.android.maps.MapView.getDeviceUid()))
            return false;
        return m.getPoint() != null && m.getPoint().isValid();
    }
}
