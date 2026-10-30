package com.atakmap.android.blast.plugin;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.atak.plugins.impl.PluginContextProvider;
import com.atakmap.android.blast.plugin.atak.BlastDetailHandler;
import com.atakmap.android.blast.plugin.atak.BlastMenuHandler;
import com.atakmap.android.blast.plugin.atak.BlastStore;
import com.atakmap.android.blast.plugin.atak.Sharing;
import com.atakmap.android.blast.plugin.core.BlastRecord;
import com.atakmap.android.blast.plugin.ui.BlastPane;
import com.atakmap.android.cot.detail.CotDetailManager;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.menu.MapMenuReceiver;
import com.atakmap.coremap.log.Log;

import gov.tak.api.plugin.IPlugin;
import gov.tak.api.plugin.IServiceController;
import gov.tak.api.ui.IHostUIService;
import gov.tak.api.ui.Pane;
import gov.tak.api.ui.PaneBuilder;
import gov.tak.api.ui.ToolbarItem;
import gov.tak.api.ui.ToolbarItemAdapter;
import gov.tak.platform.marshal.MarshalManager;

/**
 * Blast: explosive-threat standoff zones from the DHS/DOJ Bomb Threat
 * Stand-Off Card, drawn as native ATAK shapes any TAK client can see.
 * Offline; no network code, no permissions.
 */
public class BlastPlugin implements IPlugin {

    private static final String TAG = "BlastPlugin";

    private Context pluginContext;
    private final IHostUIService uiService;
    private final ToolbarItem toolbarItem;
    private Pane pane;
    private BlastPane controller;

    private MapView mapView;
    private BlastStore store;
    private Sharing sharing;
    private BlastDetailHandler detailHandler;
    private BlastMenuHandler menuHandler;
    private MapEventDispatcher.MapEventDispatchListener removeListener;

    public BlastPlugin(IServiceController serviceController) {
        final PluginContextProvider ctxProvider = serviceController
                .getService(PluginContextProvider.class);
        if (ctxProvider != null) {
            pluginContext = ctxProvider.getPluginContext();
            pluginContext.setTheme(R.style.ATAKPluginTheme);
        }
        uiService = serviceController.getService(IHostUIService.class);

        toolbarItem = new ToolbarItem.Builder(
                pluginContext.getString(R.string.app_name),
                MarshalManager.marshal(
                        icon(),
                        android.graphics.drawable.Drawable.class,
                        gov.tak.api.commons.graphics.Bitmap.class))
                .setListener(new ToolbarItemAdapter() {
                    @Override
                    public void onClick(ToolbarItem item) {
                        showPane();
                    }
                }).setIdentifier(pluginContext.getPackageName())
                .build();
    }

    private Drawable icon() {
        return pluginContext.getResources().getDrawable(R.drawable.ic_launcher,
                pluginContext.getTheme());
    }

    @Override
    public void onStart() {
        mapView = MapView.getMapView();
        store = new BlastStore(mapView);
        sharing = new Sharing(store);
        if (uiService != null)
            uiService.addToolbarItem(toolbarItem);

        try {
            detailHandler = new BlastDetailHandler(mapView, store);
            detailHandler.setOnChange(new Runnable() {
                @Override
                public void run() {
                    if (controller != null)
                        controller.onShow();
                }
            });
            CotDetailManager.getInstance().registerHandler(detailHandler);
        } catch (RuntimeException e) {
            Log.e(TAG, "detail handler registration failed", e);
        }

        try {
            menuHandler = new BlastMenuHandler(mapView.getContext(),
                    pluginContext.getPackageName(), R.drawable.ic_blast_radial,
                    store, new BlastMenuHandler.Listener() {
                        @Override
                        public void openZone(Marker zone) {
                            showPane();
                            controller.openZone(zone);
                        }

                        @Override
                        public void placeAt(Marker target) {
                            showPane();
                            controller.placeAt(target);
                        }
                    });
            MapMenuReceiver.getInstance().registerMapMenuHandler(menuHandler);
        } catch (RuntimeException e) {
            Log.e(TAG, "radial menu registration failed", e);
        }

        // Deleting any part of an incident (marker or either ring, by ATAK
        // delete, remote delete or the pane) removes the whole incident.
        // Backs up the per-item group listeners in BlastStore.
        removeListener = new MapEventDispatcher.MapEventDispatchListener() {
            @Override
            public void onMapEvent(MapEvent event) {
                try {
                    MapItem item = event.getItem();
                    if (item == null)
                        return;
                    if ((item instanceof Marker && item.getMetaBoolean(
                            BlastStore.META_FLAG, false))
                            || BlastStore.isRing(item))
                        store.partRemoved(item.getUID());
                } catch (RuntimeException e) {
                    Log.e(TAG, "incident cleanup failed", e);
                }
            }
        };
        mapView.getMapEventDispatcher().addMapEventListener(
                MapEvent.ITEM_REMOVED, removeListener);

        // Zones loaded before the plugin: adopt their stored detail and
        // make sure the rings exist.
        try {
            store.adoptAll();
            for (Marker m : store.all()) {
                BlastRecord r = BlastStore.read(m);
                if (r != null)
                    store.ensureRings(m, r);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "startup adoption failed", e);
        }
    }

    @Override
    public void onStop() {
        if (uiService != null) {
            if (pane != null && uiService.isPaneVisible(pane))
                uiService.closePane(pane);
            uiService.removeToolbarItem(toolbarItem);
        }
        if (menuHandler != null) {
            try {
                MapMenuReceiver.getInstance()
                        .unregisterMapMenuHandler(menuHandler);
            } catch (RuntimeException e) {
                Log.e(TAG, "radial menu unregister failed", e);
            }
            menuHandler = null;
        }
        if (detailHandler != null) {
            try {
                CotDetailManager.getInstance().unregisterHandler(detailHandler);
            } catch (RuntimeException e) {
                Log.e(TAG, "detail handler unregister failed", e);
            }
            detailHandler.dispose();
            detailHandler = null;
        }
        if (removeListener != null && mapView != null) {
            mapView.getMapEventDispatcher().removeMapEventListener(
                    MapEvent.ITEM_REMOVED, removeListener);
            removeListener = null;
        }
        if (controller != null) {
            controller.dispose();
            controller = null;
            pane = null;
        }
        if (store != null)
            store.dispose();
    }

    private void showPane() {
        if (uiService == null)
            return;
        if (pane == null) {
            controller = new BlastPane(pluginContext, mapView, store, sharing);
            pane = new PaneBuilder(controller.getView())
                    .setMetaValue(Pane.RELATIVE_LOCATION, Pane.Location.Default)
                    .setMetaValue(Pane.PREFERRED_WIDTH_RATIO, 0.45D)
                    .setMetaValue(Pane.PREFERRED_HEIGHT_RATIO, 0.6D)
                    .build();
        }
        controller.onShow();
        if (!uiService.isPaneVisible(pane))
            uiService.showPane(pane, null);
    }
}
