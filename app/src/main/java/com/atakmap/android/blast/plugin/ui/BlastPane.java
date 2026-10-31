package com.atakmap.android.blast.plugin.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.atakmap.android.blast.plugin.R;
import com.atakmap.android.blast.plugin.atak.BlastStore;
import com.atakmap.android.blast.plugin.atak.Exporter;
import com.atakmap.android.blast.plugin.core.Names;
import com.atakmap.android.blast.plugin.atak.Sharing;
import com.atakmap.android.blast.plugin.core.BlastRecord;
import com.atakmap.android.blast.plugin.core.Formats;
import com.atakmap.android.blast.plugin.core.Guidance;
import com.atakmap.android.blast.plugin.core.Labels;
import com.atakmap.android.blast.plugin.core.PositionCheck;
import com.atakmap.android.blast.plugin.core.StandoffRow;
import com.atakmap.android.blast.plugin.core.StandoffTable;
import com.atakmap.android.blast.plugin.core.ZoneCalculator;
import com.atakmap.android.blast.plugin.core.ZoneCalculator.MassUnit;
import com.atakmap.android.blast.plugin.core.ZoneCalculator.Selection;
import com.atakmap.android.contact.Contact;
import com.atakmap.android.contact.Contacts;
import com.atakmap.android.contact.IndividualContact;
import com.atakmap.android.maps.MapEvent;
import com.atakmap.android.maps.MapEventDispatcher;
import com.atakmap.android.maps.MapItem;
import com.atakmap.android.maps.MapTouchController;
import com.atakmap.android.maps.MapView;
import com.atakmap.android.maps.Marker;
import com.atakmap.android.maps.PointMapItem;
import com.atakmap.android.preference.AtakPreferences;
import com.atakmap.android.util.ATAKUtilities;
import com.atakmap.android.toolbar.widgets.TextContainer;
import com.atakmap.coremap.log.Log;
import com.atakmap.android.blast.plugin.core.Geo;
import com.atakmap.coremap.maps.coords.GeoPoint;
import com.atakmap.coremap.maps.coords.GeoPointMetaData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * The Blast pane, built in code: the list of incidents on the map, the
 * selected incident's card (distances, where you are, team counts,
 * provenance, actions, caveats) and the new-incident picker. All text is
 * concatenated; no String.format anywhere. Runs on the main thread only.
 * Every action re-checks that its incident is still on the map, so a card
 * left open after a remote delete can't resurrect the incident.
 */
public final class BlastPane {

    private static final String TAG = "BlastPane";
    private static final String PREF_AUTO_SEND = "blast.autoSend";
    private static final String PREF_LAST_ROW = "blast.lastRow";
    private static final long TICK_MS = 3000;

    private static final int BG = 0xFF1E1E1E;
    private static final int CARD = 0xFF2A2A2A;
    private static final int TEXT = 0xFFEEEEEE;
    private static final int DIM = 0xFFAAAAAA;
    private static final int RED = 0xFFFF5252;
    private static final int AMBER = 0xFFFFC107;
    private static final int GREEN = 0xFF69F0AE;
    private static final int BLUE = 0xFF40C4FF;
    private static final int SEL = 0xFF455A64;
    private static final int BTN = 0xFF3C3C3C;
    private static final int SEND = 0xFF1565C0;

    private final Context plugin;
    private final MapView mapView;
    private final BlastStore store;
    private final Sharing sharing;
    private final Exporter exporter;
    private final SharedPreferences prefs;
    private final StandoffTable table = StandoffTable.current();
    private final Handler main = new Handler(Looper.getMainLooper());

    private final ScrollView root;
    private final List<Button> rowButtons = new ArrayList<>();
    private Button customButton;
    private LinearLayout customRow;
    private EditText qtyEdit;
    private Button lbButton, kgButton;
    private TextView selectionText;
    private Button pickBtn, selfBtn, centerBtn, targetBtn;
    private LinearLayout zoneCard;
    private LinearLayout zoneList;
    private TextView zoneListHeader;
    private Button deleteClearedBtn;
    /** Debug builds only: what the incident lookup found. */
    private TextView debugLine;

    private String selectedRowId;
    private boolean customMode;
    private MassUnit unit = MassUnit.LB;
    private Selection selection;
    private Marker placeTarget;
    private String selectedZoneUid;
    /**
     * What the card / list currently show; null means "redraw next time".
     * (0.4 reset these to "", which is also the signature of "no incident",
     * so a deleted incident's card was never cleared.)
     */
    private String cardSignature = null;
    private String listSignature = null;
    private TextView youText, teamText;

    private boolean picking;
    private MapEventDispatcher.MapEventDispatchListener pickListener;
    private boolean ticking;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!ticking)
                return;
            refresh();
            main.postDelayed(this, TICK_MS);
        }
    };

    public BlastPane(Context pluginContext, MapView mapView, BlastStore store,
            Sharing sharing) {
        this.plugin = pluginContext;
        this.mapView = mapView;
        this.store = store;
        this.sharing = sharing;
        this.exporter = new Exporter(store);
        this.prefs = AtakPreferences.getInstance(mapView.getContext())
                .getSharedPrefs();
        this.root = new ScrollView(plugin);
        root.setBackgroundColor(BG);
        root.addView(build());
        root.addOnAttachStateChangeListener(
                new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        startTicking();
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        stopTicking();
                        stopPick();
                    }
                });
        String last = prefs.getString(PREF_LAST_ROW, null);
        selectRow(table.row(last) != null ? last : null);
    }

    public View getView() {
        return root;
    }

    // ------------------------------------------------------------------
    // Entry points

    public void onShow() {
        cardSignature = null;
        listSignature = null;
        refresh();
    }

    /** Opened from the radial menu on a zone. */
    public void openZone(Marker zone) {
        placeTarget = null;
        selectedZoneUid = zone.getUID();
        updatePlacement();
        onShow();
        main.post(new Runnable() {
            @Override
            public void run() {
                if (zoneCard != null)
                    root.smoothScrollTo(0, 0);
            }
        });
    }

    /** Opened from the radial menu on another marker. */
    public void placeAt(Marker target) {
        placeTarget = target;
        updatePlacement();
        onShow();
        root.smoothScrollTo(0, 0);
    }

    public void dispose() {
        stopTicking();
        stopPick();
        main.removeCallbacksAndMessages(null);
    }

    private void startTicking() {
        if (ticking)
            return;
        ticking = true;
        main.post(tick);
    }

    private void stopTicking() {
        ticking = false;
        main.removeCallbacks(tick);
    }

    // ------------------------------------------------------------------
    // Layout

    private View build() {
        LinearLayout col = vertical();
        int pad = dp(8);
        col.setPadding(pad, pad, pad, pad);

        TextView title = text("BLAST STANDOFF", 18, TEXT, true);
        col.addView(title);
        col.addView(text(table.title + " (" + table.shortName.replace(
                "Bomb Threat Stand-Off Card", "").trim() + "), "
                + table.editionLabel(), 12, DIM, false));

        LinearLayout listHead = horizontal();
        zoneListHeader = header("INCIDENTS ON THE MAP");
        listHead.addView(zoneListHeader, weight());
        deleteClearedBtn = button("Delete all cleared", BTN);
        deleteClearedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDeleteCleared();
            }
        });
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        dlp.gravity = Gravity.BOTTOM;
        deleteClearedBtn.setVisibility(View.GONE);
        listHead.addView(deleteClearedBtn, dlp);
        col.addView(listHead);
        zoneList = vertical();
        col.addView(zoneList);

        zoneCard = vertical();
        col.addView(zoneCard);

        col.addView(header("NEW INCIDENT: 1. THREAT"));
        List<StandoffRow> rows = table.rows();
        for (int i = 0; i < rows.size(); i += 2) {
            LinearLayout line = horizontal();
            for (int j = i; j < Math.min(i + 2, rows.size()); j++) {
                final StandoffRow r = rows.get(j);
                Button b = button(r.label + "\n"
                        + Formats.thousands(r.capacityLb) + " lb", BTN);
                b.setTag(r.id);
                setIcon(b, threatIcon(r.id));
                b.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectRow(r.id);
                    }
                });
                rowButtons.add(b);
                line.addView(b, weight());
            }
            col.addView(line);
        }
        customButton = button("Custom quantity (lb or kg)", BTN);
        customButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectCustom();
            }
        });
        col.addView(customButton, full());

        customRow = horizontal();
        qtyEdit = new EditText(plugin);
        qtyEdit.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        qtyEdit.setHint("Estimate (TNT equivalent)");
        qtyEdit.setTextColor(TEXT);
        qtyEdit.setHintTextColor(DIM);
        qtyEdit.setSingleLine(true);
        qtyEdit.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(
                        ZoneCalculator.MAX_QUANTITY_CHARS)
        });
        qtyEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (customMode)
                    recomputeSelection();
            }
        });
        customRow.addView(qtyEdit, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 2f));
        lbButton = button("lb", BTN);
        kgButton = button("kg", BTN);
        lbButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnit(MassUnit.LB);
            }
        });
        kgButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnit(MassUnit.KG);
            }
        });
        customRow.addView(lbButton, weight());
        customRow.addView(kgButton, weight());
        customRow.setVisibility(View.GONE);
        col.addView(customRow);

        selectionText = text("", 13, TEXT, false);
        selectionText.setPadding(0, dp(4), 0, dp(4));
        col.addView(selectionText);

        col.addView(header("NEW INCIDENT: 2. PLACE"));
        targetBtn = button("", SEND);
        targetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeTarget != null && placeTarget.getPoint() != null)
                    place(placeTarget.getPoint());
            }
        });
        col.addView(targetBtn, full());
        LinearLayout place = horizontal();
        pickBtn = button("Tap map", BTN);
        selfBtn = button("My position", BTN);
        centerBtn = button("Map center", BTN);
        pickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (picking)
                    stopPick();
                else
                    startPick();
            }
        });
        selfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPoint gp = selfPoint();
                if (gp == null)
                    toast("No GPS position yet.");
                else
                    place(gp);
            }
        });
        centerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPointMetaData c = mapView.getCenterPoint();
                if (c != null && c.get() != null && c.get().isValid())
                    place(c.get());
            }
        });
        place.addView(pickBtn, weight());
        place.addView(selfBtn, weight());
        place.addView(centerBtn, weight());
        col.addView(place);

        CheckBox auto = new CheckBox(plugin);
        auto.setText("Send new incidents to everyone right away");
        auto.setTextColor(TEXT);
        auto.setChecked(prefs.getBoolean(PREF_AUTO_SEND, false));
        auto.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton b,
                            boolean on) {
                        prefs.edit().putBoolean(PREF_AUTO_SEND, on).apply();
                    }
                });
        col.addView(auto);

        if (com.atakmap.android.blast.plugin.BuildConfig.DEBUG) {
            debugLine = text("", 10, DIM, false);
            col.addView(debugLine);
        }
        TextView foot = text(Guidance.DISCLAIMER, 11, DIM, false);
        foot.setPadding(0, dp(12), 0, dp(4));
        col.addView(foot);
        updatePlacement();
        return col;
    }

    // ------------------------------------------------------------------
    // Threat selection

    private void selectRow(String rowId) {
        customMode = false;
        selectedRowId = rowId;
        if (rowId != null)
            prefs.edit().putString(PREF_LAST_ROW, rowId).apply();
        recomputeSelection();
    }

    private void selectCustom() {
        customMode = true;
        selectedRowId = null;
        recomputeSelection();
        qtyEdit.requestFocus();
    }

    private void setUnit(MassUnit u) {
        unit = u;
        recomputeSelection();
    }

    private void recomputeSelection() {
        if (customMode) {
            double q = ZoneCalculator.parseQuantity(qtyEdit.getText()
                    .toString());
            selection = Double.isNaN(q) ? null
                    : ZoneCalculator.forQuantity(table, q, unit);
        } else {
            selection = selectedRowId == null ? null
                    : ZoneCalculator.forRow(table, selectedRowId);
        }
        for (Button b : rowButtons)
            tint(b, !customMode && b.getTag().equals(selectedRowId) ? SEL
                    : BTN);
        tint(customButton, customMode ? SEL : BTN);
        customRow.setVisibility(customMode ? View.VISIBLE : View.GONE);
        tint(lbButton, unit == MassUnit.LB ? SEL : BTN);
        tint(kgButton, unit == MassUnit.KG ? SEL : BTN);

        if (selection == null) {
            selectionText.setText(customMode
                    ? "Enter your estimate. The card row at or above it is used."
                    : "Choose the threat that best matches what you see.");
            selectionText.setTextColor(DIM);
        } else if (!selection.isUsable()) {
            selectionText.setText(selection.explain());
            selectionText.setTextColor(RED);
        } else {
            StandoffRow r = selection.row;
            selectionText.setText(selection.explain() + "\nMandatory evac "
                    + Formats.ft(r.mandatoryFt) + " ("
                    + Formats.thousands(r.mandatoryMetres()) + " m)"
                    + " · Shelter-in-place to "
                    + Formats.ft(r.shelterToFt) + " · Preferred "
                    + Formats.ft(r.preferredFt) + "+ ("
                    + Formats.thousands(r.preferredMetres()) + " m)");
            selectionText.setTextColor(TEXT);
        }
        updatePlacement();
    }

    private void updatePlacement() {
        boolean ok = selection != null && selection.isUsable();
        for (Button b : new Button[] { pickBtn, selfBtn, centerBtn }) {
            if (b != null) {
                b.setEnabled(ok);
                b.setAlpha(ok ? 1f : 0.4f);
            }
        }
        if (targetBtn != null) {
            if (placeTarget != null) {
                targetBtn.setVisibility(View.VISIBLE);
                targetBtn.setText("Place at "
                        + ATAKUtilities.getDisplayName(placeTarget));
                targetBtn.setEnabled(ok);
                targetBtn.setAlpha(ok ? 1f : 0.4f);
            } else {
                targetBtn.setVisibility(View.GONE);
            }
        }
    }

    // ------------------------------------------------------------------
    // Placement

    private void place(GeoPoint at) {
        try {
            if (selection == null || !selection.isUsable()) {
                toast("Choose a threat first.");
                return;
            }
            BlastRecord r = BlastRecord.create(selection, callsign(),
                    System.currentTimeMillis());
            Marker m = store.create(at, r);
            if (m == null) {
                toast("Could not place the incident.");
                return;
            }
            placeTarget = null;
            selectedZoneUid = m.getUID();
            updatePlacement();
            if (prefs.getBoolean(PREF_AUTO_SEND, false)) {
                sharing.sendToAll(m);
                toast("Incident placed and sent to everyone.");
            } else {
                toast("Incident placed. Use SEND TO ALL to share it.");
            }
            onShow();
            main.post(new Runnable() {
                @Override
                public void run() {
                    root.smoothScrollTo(0, 0);
                }
            });
        } catch (RuntimeException e) {
            Log.e(TAG, "place failed", e);
            toast("Could not place the incident.");
        }
    }

    private void startPick() {
        if (selection == null || !selection.isUsable())
            return;
        final MapEventDispatcher d = mapView.getMapEventDispatcher();
        d.pushListeners();
        d.clearListeners(MapEvent.MAP_CLICK);
        d.clearListeners(MapEvent.ITEM_CLICK);
        d.clearListeners(MapEvent.MAP_LONG_PRESS);
        pickListener = new MapEventDispatcher.MapEventDispatchListener() {
            @Override
            public void onMapEvent(MapEvent event) {
                onPicked(event);
            }
        };
        d.addMapEventListener(MapEvent.MAP_CLICK, pickListener);
        d.addMapEventListener(MapEvent.ITEM_CLICK, pickListener);
        d.addMapEventListener(MapEvent.MAP_LONG_PRESS, pickListener);
        picking = true;
        pickBtn.setText("Cancel");
        TextContainer.getInstance().displayPrompt(
                "Tap the map or a marker where the device is. "
                        + "Press Cancel to stop.");
    }

    /**
     * Uses MapView.inverseWithElevation, deprecated but not slated for
     * removal (DeprecatedApi forRemoval=false); ATAK's own Tool.findPoint
     * converts taps the same way.
     */
    @SuppressWarnings("deprecation")
    private void onPicked(MapEvent event) {
        GeoPoint gp = null;
        MapItem item = event.getItem();
        if (MapEvent.ITEM_CLICK.equals(event.getType())
                && item instanceof PointMapItem) {
            gp = ((PointMapItem) item).getPoint();
        } else {
            PointF p = event.getPointF();
            if (p != null) {
                GeoPointMetaData m = mapView.inverseWithElevation(p.x, p.y);
                if (m != null)
                    gp = m.get();
            }
        }
        stopPick();
        if (gp != null && gp.isValid())
            place(gp);
    }

    private void stopPick() {
        if (!picking)
            return;
        picking = false;
        MapEventDispatcher d = mapView.getMapEventDispatcher();
        if (pickListener != null) {
            d.removeMapEventListener(MapEvent.MAP_CLICK, pickListener);
            d.removeMapEventListener(MapEvent.ITEM_CLICK, pickListener);
            d.removeMapEventListener(MapEvent.MAP_LONG_PRESS, pickListener);
            pickListener = null;
        }
        d.popListeners();
        TextContainer.getInstance().closePrompt();
        if (pickBtn != null)
            pickBtn.setText("Tap map");
    }

    // ------------------------------------------------------------------
    // Refresh

    private void refresh() {
        try {
            refreshCard();
            refreshList();
        } catch (RuntimeException e) {
            Log.e(TAG, "refresh failed", e);
        }
    }

    private Marker selectedZone() {
        if (selectedZoneUid == null)
            return null;
        // Not mapView.getMapItem alone: ATAK's UID index can still return
        // an incident after it was deleted.
        return store.liveMarker(selectedZoneUid);
    }

    private void refreshCard() {
        Marker m = selectedZone();
        BlastRecord r = m == null ? null : BlastStore.read(m);
        String sig = m == null ? "" : m.getUID() + "|"
                + m.getMetaString(BlastStore.META, "") + "|"
                + ATAKUtilities.getDisplayName(m);
        if (!sig.equals(cardSignature)) {
            cardSignature = sig;
            buildCard(m, r);
        }
        if (m != null && r != null)
            updateYou(m, r);
    }

    private void buildCard(final Marker m, final BlastRecord r) {
        zoneCard.removeAllViews();
        youText = null;
        teamText = null;
        if (m == null || r == null) {
            zoneCard.setVisibility(View.GONE);
            return;
        }
        zoneCard.setVisibility(View.VISIBLE);
        zoneCard.addView(header("SELECTED INCIDENT"));
        LinearLayout card = vertical();
        card.setBackgroundColor(CARD);
        int p = dp(8);
        card.setPadding(p, p, p, p);
        zoneCard.addView(card);

        TextView name = text(ATAKUtilities.getDisplayName(m), 17,
                r.isCleared() ? DIM : TEXT, true);
        setIcon(name, threatIcon(r.rowId));
        name.setCompoundDrawablePadding(dp(6));
        card.addView(name);
        card.addView(text(Labels.threat(r)
                + (r.isCleared() ? " · CLEARED" : ""), 13,
                r.isCleared() ? DIM : AMBER, true));

        String[][] rows = Labels.zoneRows(r);
        int[] colors = { RED, AMBER, GREEN };
        for (int i = 0; i < rows.length; i++) {
            LinearLayout line = horizontal();
            View sw = new View(plugin);
            sw.setBackgroundColor(r.isCleared() ? DIM : colors[i]);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(12),
                    dp(12));
            lp.setMargins(0, 0, dp(6), 0);
            lp.gravity = Gravity.CENTER_VERTICAL;
            line.addView(sw, lp);
            line.addView(text(rows[i][0], 14, TEXT, false),
                    new LinearLayout.LayoutParams(0,
                            ViewGroup.LayoutParams.WRAP_CONTENT, 1.3f));
            line.addView(text(rows[i][1], 14, TEXT, true), weight());
            line.addView(text(rows[i][2], 14, TEXT, true), weight());
            card.addView(line);
        }

        card.addView(text(Labels.metresNote(r), 11, DIM, false));

        youText = text("", 14, TEXT, false);
        youText.setPadding(0, dp(6), 0, 0);
        card.addView(youText);
        teamText = text("", 12, DIM, false);
        card.addView(teamText);

        if (r.table() == null)
            card.addView(text("This build doesn't have the " + r.sourceId
                    + " " + Formats.editionLabel(r.edition) + " table. "
                    + "Showing the distances the sender drew.", 12, AMBER,
                    false));
        else if (r.disagreesWithTable())
            card.addView(text("These distances differ from this build's "
                    + "copy of the same table. Check with the sender.", 12,
                    RED, false));

        TextView prov = text(Labels.provenance(r), 11, DIM, false);
        prov.setPadding(0, dp(4), 0, dp(4));
        card.addView(prov);

        LinearLayout a1 = horizontal();
        Button sendAll = button("SEND TO ALL", SEND);
        sendAll.setTypeface(Typeface.DEFAULT_BOLD);
        sendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Marker l = live(m);
                    if (l == null)
                        return;
                    int n = sharing.sendToAll(l);
                    toast("Sent to everyone (" + n + " items).");
                } catch (RuntimeException e) {
                    Log.e(TAG, "send failed", e);
                    toast("Send failed.");
                }
            }
        });
        Button sendTo = button("Send to…", BTN);
        sendTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Marker l = live(m);
                if (l != null)
                    sharing.sendTo(l);
            }
        });
        Button fly = button("Fly to", BTN);
        fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goTo(m);
            }
        });
        a1.addView(sendAll, new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1.4f));
        a1.addView(sendTo, weight());
        a1.addView(fly, weight());
        card.addView(a1);

        LinearLayout aRename = horizontal();
        Button ren = button("Rename…", BTN);
        ren.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptRename(m);
            }
        });
        Button exp = button("Export…", BTN);
        exp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptExport(m);
            }
        });
        aRename.addView(ren, weight());
        aRename.addView(exp, weight());
        card.addView(aRename);

        LinearLayout a2 = horizontal();
        Button clear = button(r.isCleared() ? "Reopen" : "Mark cleared", BTN);
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmStatus(m);
            }
        });
        Button del = button("Delete…", BTN);
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmDelete(m);
            }
        });
        a2.addView(clear, weight());
        a2.addView(del, weight());
        card.addView(a2);

        TextView actions = text(Guidance.CARD_ACTIONS, 13, TEXT, false);
        actions.setPadding(0, dp(6), 0, dp(2));
        card.addView(actions);
        for (String c : Guidance.CAVEATS)
            card.addView(text("• " + c, 12, DIM, false));
    }

    private void updateYou(Marker m, BlastRecord r) {
        if (youText == null)
            return;
        StandoffRow row = r.row();
        GeoPoint center = m.getPoint();
        Marker self = ATAKUtilities.findSelf(mapView);
        GeoPoint sp = self != null ? self.getPoint() : null;
        if (center == null || sp == null || !sp.isValid()
                || (sp.getLatitude() == 0 && sp.getLongitude() == 0)) {
            youText.setText("YOU: no GPS position yet.");
            youText.setTextColor(DIM);
        } else {
            // CE is NaN when unknown; CotPoint uses 9999999 for unknown.
            double ce = sp.getCE();
            boolean ceKnown = !Double.isNaN(ce) && ce > 0 && ce < 9999999;
            PositionCheck.Result res = PositionCheck.check(row,
                    distance(center, sp), ceKnown ? ce : Double.NaN);
            if (res == null) {
                youText.setText("YOU: position check unavailable.");
                youText.setTextColor(DIM);
            } else {
                String s = "YOU: " + res.headline() + "\n" + res.guidance();
                String w = res.boundaryWarning();
                if (w != null)
                    s += "\n" + w;
                if (ceKnown && ce > row.mandatoryRadiusM())
                    s += "\nGPS accuracy is poor (about "
                            + Formats.measured(ce)
                            + "): treat this check as approximate.";
                if (r.isCleared())
                    s = "Incident marked cleared. " + s;
                youText.setText(s);
                youText.setTextColor(r.isCleared() ? DIM
                        : res.zone == PositionCheck.Zone.MANDATORY ? RED
                                : res.zone == PositionCheck.Zone.SHELTER
                                        ? AMBER
                                        : GREEN);
            }
        }
        if (teamText != null && center != null) {
            List<Double> ds = new ArrayList<>();
            try {
                for (Contact c : Contacts.getInstance().getAllContacts()) {
                    if (!(c instanceof IndividualContact))
                        continue;
                    MapItem mi = ((IndividualContact) c).getMapItem();
                    if (!(mi instanceof PointMapItem) || mi == self)
                        continue;
                    GeoPoint gp = ((PointMapItem) mi).getPoint();
                    if (gp != null && gp.isValid())
                        ds.add(distance(center, gp));
                }
            } catch (RuntimeException e) {
                Log.w(TAG, "contact scan failed", e);
            }
            double[] arr = new double[ds.size()];
            for (int i = 0; i < arr.length; i++)
                arr[i] = ds.get(i);
            int[] n = PositionCheck.count(row, arr);
            teamText.setText(ds.isEmpty() ? "Team: no other contacts on the map."
                    : "Team on the map: " + n[0] + " in mandatory, " + n[1]
                            + " in shelter-in-place, " + n[2]
                            + " at or beyond preferred.");
            teamText.setTextColor(n[0] > 0 && !r.isCleared() ? RED : DIM);
        }
    }

    private void refreshList() {
        final GeoPoint ref = referencePoint();
        List<Marker> zones = store.all();
        if (debugLine != null)
            debugLine.setText("Debug: incidents " + store.lastScan());
        // Read each record once (parsing is not free) and sort on copies.
        final java.util.Map<String, BlastRecord> recs = new java.util.HashMap<>();
        final java.util.Map<String, Double> dist = new java.util.HashMap<>();
        List<Marker> usable = new ArrayList<>();
        for (Marker m : zones) {
            BlastRecord r = BlastStore.read(m);
            if (r == null)
                continue;
            recs.put(m.getUID(), r);
            dist.put(m.getUID(), distance(ref, m));
            usable.add(m);
        }
        zones = usable;
        Collections.sort(zones, new Comparator<Marker>() {
            @Override
            public int compare(Marker a, Marker b) {
                // Active incidents first, then by distance.
                boolean ca = recs.get(a.getUID()).isCleared();
                boolean cb = recs.get(b.getUID()).isCleared();
                if (ca != cb)
                    return ca ? 1 : -1;
                return Double.compare(dist.get(a.getUID()),
                        dist.get(b.getUID()));
            }
        });
        int cleared = 0;
        StringBuilder sig = new StringBuilder();
        for (Marker m : zones) {
            boolean c = recs.get(m.getUID()).isCleared();
            if (c)
                cleared++;
            sig.append(m.getUID()).append(c)
                    .append(ATAKUtilities.getDisplayName(m))
                    .append(Math.round(dist.get(m.getUID()) / 10)).append(';');
        }
        sig.append(selectedZoneUid);
        deleteClearedBtn.setVisibility(cleared > 0 ? View.VISIBLE : View.GONE);
        deleteClearedBtn.setText("Delete all cleared (" + cleared + ")");
        if (sig.toString().equals(listSignature))
            return;
        listSignature = sig.toString();
        zoneList.removeAllViews();
        int active = zones.size() - cleared;
        zoneListHeader.setText("INCIDENTS ON THE MAP: " + active + " active"
                + (cleared > 0 ? ", " + cleared + " cleared" : ""));
        if (zones.isEmpty()) {
            zoneList.addView(text("None. Choose a threat below and place it.",
                    13, DIM, false));
            return;
        }
        for (final Marker m : zones) {
            BlastRecord r = recs.get(m.getUID());
            boolean sel = m.getUID().equals(selectedZoneUid);
            double d = dist.get(m.getUID());
            String where = "";
            if (!Double.isNaN(d)) {
                where = "\n" + Formats.measured(d);
                if (!r.isCleared())
                    where += " · you: " + PositionCheck.zoneOf(r.row(), d).title;
            }
            LinearLayout row = horizontal();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(sel ? SEL : CARD);
            int p = dp(6);
            row.setPadding(p, p, p, p);
            View sw = new View(plugin);
            sw.setBackgroundColor(r.isCleared() ? DIM : RED);
            LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(dp(6),
                    dp(36));
            sl.setMargins(0, 0, dp(6), 0);
            row.addView(sw, sl);
            TextView t = text(ATAKUtilities.getDisplayName(m) + where, 14,
                    r.isCleared() ? DIM : TEXT, sel);
            setIcon(t, threatIcon(r.rowId));
            t.setCompoundDrawablePadding(dp(6));
            row.addView(t, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            Button go = button("Go to", BTN);
            go.setMinWidth(dp(56));
            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goTo(m);
                }
            });
            row.addView(go);
            Button del = button("Delete", BTN);
            del.setMinWidth(dp(56));
            del.setTextColor(RED);
            del.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    confirmDelete(m);
                }
            });
            row.addView(del);
            View.OnClickListener select = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedZoneUid = m.getUID();
                    listSignature = null;
                    cardSignature = null;
                    refresh();
                }
            };
            t.setOnClickListener(select);
            row.setOnClickListener(select);
            LinearLayout.LayoutParams rl = full();
            rl.setMargins(0, 0, 0, dp(3));
            zoneList.addView(row, rl);
        }
    }

    private static boolean isCleared(Marker m) {
        BlastRecord r = BlastStore.read(m);
        return r != null && r.isCleared();
    }

    /** Fly to the incident, framing the preferred ring. */
    private void goTo(Marker card) {
        Marker m = live(card);
        if (m == null)
            return;
        selectedZoneUid = m.getUID();
        listSignature = null;
        cardSignature = null;
        MapItem pref = store.liveItem(m.getUID() + BlastStore.PREF_SUFFIX);
        MapTouchController.goTo(pref != null ? pref : m, false);
        refresh();
    }

    private int threatIcon(String rowId) {
        if (rowId == null)
            return 0;
        switch (rowId) {
            case "PIPE_BOMB":
                return R.drawable.ic_threat_pipe_bomb;
            case "SUICIDE_BOMBER":
                return R.drawable.ic_threat_suicide_bomber;
            case "BRIEFCASE":
                return R.drawable.ic_threat_briefcase;
            case "CAR":
                return R.drawable.ic_threat_car;
            case "SUV_VAN":
                return R.drawable.ic_threat_suv_van;
            case "SMALL_TRUCK":
                return R.drawable.ic_threat_small_truck;
            case "CONTAINER_TRUCK":
                return R.drawable.ic_threat_container_truck;
            case "SEMI_TRAILER":
                return R.drawable.ic_threat_semi_trailer;
            default:
                return 0;
        }
    }

    private void setIcon(TextView v, int res) {
        if (res == 0)
            return;
        try {
            android.graphics.drawable.Drawable d = plugin.getResources()
                    .getDrawable(res, plugin.getTheme());
            int sz = dp(28);
            d.setBounds(0, 0, sz, sz);
            v.setCompoundDrawablesRelative(d, null, null, null);
        } catch (RuntimeException e) {
            Log.w(TAG, "icon " + res + " unavailable", e);
        }
    }

    /** Self position, or the map centre without a fix (for list sorting). */
    private GeoPoint referencePoint() {
        GeoPoint gp = selfPoint();
        if (gp != null)
            return gp;
        GeoPointMetaData c = mapView.getCenterPoint();
        return c != null ? c.get() : null;
    }

    private static double distance(GeoPoint ref, Marker m) {
        return distance(ref, m.getPoint());
    }

    /** WGS84 geodesic distance (core.Geo, tested against GeographicLib). */
    private static double distance(GeoPoint a, GeoPoint b) {
        if (a == null || b == null || !a.isValid() || !b.isValid())
            return Double.NaN;
        return Geo.distance(a.getLatitude(), a.getLongitude(),
                b.getLatitude(), b.getLongitude());
    }

    private GeoPoint selfPoint() {
        try {
            Marker self = ATAKUtilities.findSelf(mapView);
            GeoPoint gp = self != null ? self.getPoint() : null;
            if (gp != null && gp.isValid()
                    && !(gp.getLatitude() == 0 && gp.getLongitude() == 0))
                return gp;
        } catch (RuntimeException e) {
            Log.w(TAG, "no self position", e);
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Status and delete

    /**
     * The incident if it is still on the map; otherwise tells the user,
     * refreshes the pane and returns null.
     */
    private Marker live(Marker m) {
        Marker l = m == null ? null : store.liveMarker(m.getUID());
        if (l == null) {
            toast("This incident is no longer on the map.");
            afterDelete();
        }
        return l;
    }

    private void confirmStatus(Marker card) {
        final Marker m = live(card);
        if (m == null)
            return;
        BlastRecord r = BlastStore.read(m);
        if (r == null)
            return;
        final boolean clearing = !r.isCleared();
        new AlertDialog.Builder(mapView.getContext())
                .setTitle(clearing ? "Mark incident cleared?"
                        : "Reopen incident?")
                .setMessage(clearing
                        ? "Records your callsign and the time. The rings "
                                + "turn grey and stay on the map until deleted."
                        : "Sets the incident back to active with the "
                                + "original distances.")
                .setPositiveButton(clearing ? "Clear and send to all"
                        : "Reopen and send to all",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                setStatus(m, clearing, true);
                            }
                        })
                .setNeutralButton("This device only",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                setStatus(m, clearing, false);
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void setStatus(Marker card, boolean clearing, boolean send) {
        try {
            // Re-check and re-read now: the dialog may have been open while
            // the incident was deleted or updated by someone else.
            Marker m = live(card);
            BlastRecord r = m == null ? null : BlastStore.read(m);
            if (r == null)
                return;
            BlastRecord next = clearing
                    ? r.cleared(callsign(), System.currentTimeMillis())
                    : r.reopened();
            if (!store.write(m, next)) {
                live(null);
                return;
            }
            if (send)
                sharing.sendToAll(m);
            cardSignature = null;
            listSignature = null;
            refresh();
        } catch (RuntimeException e) {
            Log.e(TAG, "status change failed", e);
            toast("Could not update the incident.");
        }
    }

    private void confirmDelete(Marker card) {
        final Marker m = live(card);
        if (m == null)
            return;
        final String name = ATAKUtilities.getDisplayName(m);
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Delete " + name + "?")
                .setMessage("Removes the marker and both rings of this "
                        + "incident.\n\nDelete for everyone also sends a "
                        + "remove request to every connected TAK client. "
                        + "Clients that are offline keep their copy.")
                .setPositiveButton("Delete for everyone",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                try {
                                    if (store.liveMarker(m.getUID()) != null)
                                        sharing.deleteForEveryone(m);
                                } catch (RuntimeException e) {
                                    Log.e(TAG, "delete failed", e);
                                }
                                afterDelete();
                            }
                        })
                .setNeutralButton("This device only",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                store.delete(m);
                                afterDelete();
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void promptRename(Marker card) {
        final Marker m = live(card);
        if (m == null)
            return;
        final EditText input = new EditText(mapView.getContext());
        input.setSingleLine(true);
        input.setText(Names.base(BlastStore.nameOf(m)));
        input.setSelectAllOnFocus(true);
        input.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(Names.MAX_LENGTH)
        });
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Rename incident")
                .setMessage("The name is the marker's callsign, so every TAK "
                        + "client shows it. CLEARED is added automatically "
                        + "while the incident is cleared.")
                .setView(input)
                .setPositiveButton("Rename and send to all",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                doRename(m, input.getText().toString(), true);
                            }
                        })
                .setNeutralButton("This device only",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                doRename(m, input.getText().toString(), false);
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doRename(Marker card, String typed, boolean send) {
        try {
            Marker m = live(card);
            if (m == null)
                return;
            if (Names.sanitize(typed) == null) {
                toast("Enter a name.");
                return;
            }
            if (!store.rename(m, typed)) {
                toast("Could not rename the incident.");
                return;
            }
            if (send)
                sharing.sendToAll(m);
            cardSignature = null;
            listSignature = null;
            refresh();
        } catch (RuntimeException e) {
            Log.e(TAG, "rename failed", e);
            toast("Could not rename the incident.");
        }
    }

    private void promptExport(Marker card) {
        final Marker m = live(card);
        if (m == null)
            return;
        final Exporter.Format[] formats = Exporter.Format.values();
        final boolean[] checked = new boolean[formats.length];
        String[] labels = new String[formats.length];
        for (int i = 0; i < formats.length; i++) {
            labels[i] = formats[i].label;
            checked[i] = true;
        }
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Export " + BlastStore.nameOf(m))
                .setMultiChoiceItems(labels, checked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int i,
                                    boolean on) {
                                checked[i] = on;
                            }
                        })
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                doExport(m, formats, checked);
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void doExport(Marker card, Exporter.Format[] formats,
            boolean[] checked) {
        Marker m = live(card);
        if (m == null)
            return;
        StringBuilder done = new StringBuilder();
        StringBuilder failed = new StringBuilder();
        for (int i = 0; i < formats.length; i++) {
            if (!checked[i])
                continue;
            try {
                java.io.File f = exporter.export(m, formats[i]);
                done.append("\n• ").append(f.getName());
            } catch (Exception e) {
                Log.e(TAG, "export failed: " + formats[i], e);
                failed.append("\n• ").append(formats[i].label);
            }
        }
        if (done.length() == 0 && failed.length() == 0)
            return;
        String msg = "";
        if (done.length() > 0)
            msg += "Saved in atak/export/Blast:" + done
                    + "\n\nImport a data package with ATAK's Import "
                    + "Manager or send it as a file; copy the KMZ to Google "
                    + "Earth and the GPX to a Garmin's GPX folder.";
        if (failed.length() > 0)
            msg += (msg.isEmpty() ? "" : "\n\n") + "Could not save:" + failed;
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Export")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }

    private void confirmDeleteCleared() {
        final List<Marker> cleared = new ArrayList<>();
        for (Marker m : store.all())
            if (isCleared(m))
                cleared.add(m);
        if (cleared.isEmpty())
            return;
        StringBuilder names = new StringBuilder();
        for (Marker m : cleared)
            names.append("\n• ").append(ATAKUtilities.getDisplayName(m));
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Delete " + cleared.size() + " cleared incident"
                        + (cleared.size() == 1 ? "" : "s") + "?")
                .setMessage("Removes each marker and its rings:" + names
                        + "\n\nActive incidents are not touched.")
                .setPositiveButton("Delete for everyone",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                for (Marker m : cleared) {
                                    try {
                                        if (store.liveMarker(m.getUID()) != null)
                                            sharing.deleteForEveryone(m);
                                    } catch (RuntimeException e) {
                                        Log.e(TAG, "delete failed", e);
                                    }
                                }
                                afterDelete();
                            }
                        })
                .setNeutralButton("This device only",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                for (Marker m : cleared)
                                    store.delete(m);
                                afterDelete();
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void afterDelete() {
        // Keep the selection if a different incident was deleted.
        if (selectedZone() == null)
            selectedZoneUid = null;
        cardSignature = null;
        listSignature = null;
        refresh();
    }

    // ------------------------------------------------------------------
    // Helpers

    private String callsign() {
        try {
            String cs = mapView.getDeviceCallsign();
            return cs == null ? "" : cs;
        } catch (RuntimeException e) {
            return "";
        }
    }

    private void toast(String s) {
        Toast.makeText(mapView.getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private int dp(float v) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, v,
                plugin.getResources().getDisplayMetrics()));
    }

    private LinearLayout vertical() {
        LinearLayout l = new LinearLayout(plugin);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    private LinearLayout horizontal() {
        LinearLayout l = new LinearLayout(plugin);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private static LinearLayout.LayoutParams weight() {
        return new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
    }

    private static LinearLayout.LayoutParams full() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private TextView text(String s, float sp, int color, boolean bold) {
        TextView t = new TextView(plugin);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setTextColor(color);
        if (bold)
            t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    private TextView header(String s) {
        TextView t = text(s, 12, BLUE, true);
        t.setPadding(0, dp(10), 0, dp(4));
        return t;
    }

    private Button button(String label, int color) {
        Button b = new Button(plugin);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(Color.WHITE);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        b.setMinHeight(dp(44));
        tint(b, color);
        return b;
    }

    private void tint(Button b, int color) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(4));
        g.setStroke(dp(1), 0xFF000000);
        b.setBackground(g);
    }
}
