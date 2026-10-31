package com.atakmap.android.blast.plugin.atak;

import android.content.Context;
import android.content.Intent;

import com.atakmap.android.ipc.AtakBroadcast;
import com.atakmap.android.maps.Base64MapDataRef;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.menu.MapMenuButtonWidget;
import com.atakmap.android.menu.MapMenuHandler;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.android.menu.MapMenuWidget;
import com.atakmap.coremap.log.Log;

import gov.tak.api.commons.graphics.IIcon;
import gov.tak.api.widgets.IMapMenuButtonWidget;
import gov.tak.api.widgets.IMapWidget;
import gov.tak.platform.commons.graphics.Icon;

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
    /**
     * The icon as base64 data. ATAK loads android.resource:// icons through
     * its own Resources, so a plugin resource ID draws nothing there.
     */
    private final String iconUri;
    private final BlastStore store;
    private final Listener listener;

    public BlastMenuHandler(Context atakContext, Context pluginContext,
            int iconRes, BlastStore store, Listener listener) {
        this.atakContext = atakContext;
        this.iconUri = loadIcon(pluginContext, iconRes);
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
            // Copy every layout parameter from an existing button, the way
            // ATAK builds its own submenu back button (MenuLayoutBase).
            // A new button otherwise keeps RadialButtonWidget defaults
            // (width 100), which draws oversized and dark when pressed.
            MapMenuButtonWidget f = null;
            for (int i = 0; i < menu.getChildWidgetCount(); i++) {
                IMapWidget w = menu.getChildWidgetAt(i);
                if (w instanceof MapMenuButtonWidget
                        && !((MapMenuButtonWidget) w).isBackButton()) {
                    f = (MapMenuButtonWidget) w;
                    break;
                }
            }
            int ax = 16, ay = 16, iw = 32, ih = 32;
            if (f != null) {
                b.setBackground(f.getWidgetBackground());
                b.setLayoutWeight(f.getLayoutWeight());
                b.setButtonSize(f.getButtonSpan(), f.getButtonWidth());
                b.setOrientation(f.getOrientationAngle(),
                        f.getOrientationRadius());
                IIcon fi = f.getWidgetIcon();
                if (fi != null && fi.getWidth() > 0 && fi.getHeight() > 0) {
                    ax = fi.getAnchorX();
                    ay = fi.getAnchorY();
                    iw = fi.getWidth();
                    ih = fi.getHeight();
                }
            }
            if (iconUri == null)
                return;
            b.setWidgetIcon(new Icon.Builder()
                    .setImageUri(IIcon.STATE_DEFAULT, iconUri)
                    .setAnchor(ax, ay)
                    .setSize(iw, ih)
                    .build());
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

    /** The icon as a base64:// URI, or null. */
    private static String loadIcon(Context ctx, int res) {
        try (java.io.InputStream in = ctx.getResources().openRawResource(res)) {
            java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = in.read(buf)) > 0)
                bo.write(buf, 0, n);
            String data = android.util.Base64.encodeToString(bo.toByteArray(),
                    android.util.Base64.URL_SAFE | android.util.Base64.NO_WRAP);
            return Base64MapDataRef.toUri(data);
        } catch (java.io.IOException | RuntimeException e) {
            Log.e(TAG, "radial icon unavailable", e);
            return null;
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
