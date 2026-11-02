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
import com.atakmap.android.blast.plugin.core.UxoRecord;
import com.atakmap.android.blast.plugin.core.UxoReference;
import com.atakmap.android.blast.plugin.core.UxoTable;
import com.atakmap.android.blast.plugin.core.UxoText;
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
    private static final String PREF_EMERGENCY = "blast.emergencyNumber";
    private static final String DEFAULT_EMERGENCY = "911";
    /** 0.1-0.7 remembered the last threat; no longer used (removed on start). */
    private static final String PREF_LAST_ROW_OLD = "blast.lastRow";
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
    private static final int ORANGE = 0xFFFF9100;
    private static final int UXO_SEL = 0xFF8D4A00;

    /** Which kind of incident the New incident section is placing. */
    private enum Mode {
        NONE, IED, UXO
    }

    private final Context plugin;
    private final MapView mapView;
    private final BlastStore store;
    private final Sharing sharing;
    private final Exporter exporter;
    private final SharedPreferences prefs;
    private final StandoffTable table = StandoffTable.current();
    private final Handler main = new Handler(Looper.getMainLooper());

    private final ScrollView root;
    private LinearLayout incidentsView, referenceView;
    private Button tabIncidents, tabReference;
    private final java.util.Map<String, View> refAnchors = new java.util.HashMap<>();
    private TextView emergencyText;

    private Button modeIedBtn, modeUxoBtn;
    private LinearLayout iedSection, uxoSection;
    private final List<Button> uxoCatButtons = new ArrayList<>();
    private final List<Button> whereButtons = new ArrayList<>();
    private Button largeBtn;
    private CheckBox multiCheck;
    private TextView uxoSelText;
    private Button uxoPickBtn, uxoRbBtn, uxoCenterBtn, uxoTargetBtn;
    private String uxoCat;
    private boolean uxoLarge, uxoMulti;
    private UxoRecord.Where uxoWhere = UxoRecord.Where.UNSET;
    /** What the map tap places. */
    private Mode pickKind = Mode.NONE;
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
        root.addView(buildRoot());
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
        // No threat is pre-selected: the user must choose one for every
        // incident (a remembered choice could place the wrong size).
        prefs.edit().remove(PREF_LAST_ROW_OLD).apply();
        resetThreat();
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
        showTab(false, null);
        updatePlacement();
        updateUxo();
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
        showTab(false, null);
        updatePlacement();
        updateUxo();
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

    private View buildRoot() {
        LinearLayout outer = vertical();
        int pad = dp(8);
        outer.setPadding(pad, pad, pad, pad);
        outer.addView(text("BLAST STANDOFF", 18, TEXT, true));
        outer.addView(text("Bomb threats: " + table.title + " ("
                + table.editionLabel() + "). Military ordnance (UXO): "
                + UxoTable.SHORT_NAME + " (" + UxoTable.EDITION_LABEL + ").",
                12, DIM, false));
        LinearLayout tabs = horizontal();
        tabIncidents = button("INCIDENTS", SEL);
        tabReference = button("REFERENCE", BTN);
        tabIncidents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTab(false, null);
            }
        });
        tabReference.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTab(true, null);
            }
        });
        tabs.addView(tabIncidents, weight());
        tabs.addView(tabReference, weight());
        LinearLayout.LayoutParams tl = full();
        tl.setMargins(0, dp(6), 0, dp(2));
        outer.addView(tabs, tl);
        incidentsView = (LinearLayout) build();
        referenceView = buildReference();
        referenceView.setVisibility(View.GONE);
        outer.addView(incidentsView);
        outer.addView(referenceView);
        return outer;
    }

    /** Show the Reference tab (optionally at a section) or the Incidents tab. */
    private void showTab(boolean reference, final String sectionId) {
        stopPick();
        incidentsView.setVisibility(reference ? View.GONE : View.VISIBLE);
        referenceView.setVisibility(reference ? View.VISIBLE : View.GONE);
        tint(tabIncidents, reference ? BTN : SEL);
        tint(tabReference, reference ? SEL : BTN);
        main.post(new Runnable() {
            @Override
            public void run() {
                View a = sectionId == null ? null : refAnchors.get(sectionId);
                root.smoothScrollTo(0, a == null ? 0
                        : referenceView.getTop() + a.getTop());
            }
        });
    }

    private View build() {
        LinearLayout col = vertical();

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

        col.addView(header("NEW INCIDENT: WHAT KIND OF THREAT?"));
        LinearLayout modes = horizontal();
        modeIedBtn = button("SUSPICIOUS DEVICE\nbomb threat / IED\n(CISA/FBI card)",
                BTN);
        modeUxoBtn = button("MILITARY ORDNANCE (UXO)\nshells, bombs, mines,\n"
                + "rockets, grenades", BTN);
        modeIedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(Mode.IED);
            }
        });
        modeUxoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setMode(Mode.UXO);
            }
        });
        modes.addView(modeIedBtn, weight());
        modes.addView(modeUxoBtn, weight());
        col.addView(modes);
        col.addView(text("Not sure? Choose MILITARY ORDNANCE if it looks "
                + "factory-made, otherwise SUSPICIOUS DEVICE.", 12, DIM, false));

        uxoSection = buildUxoSection();
        uxoSection.setVisibility(View.GONE);
        col.addView(uxoSection);

        iedSection = vertical();
        iedSection.setVisibility(View.GONE);
        col.addView(iedSection);
        LinearLayout outerCol = col;
        col = iedSection;

        col.addView(header("1. THREAT"));
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

        col.addView(header("2. PLACE"));
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
                    startPick(Mode.IED);
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
        col = outerCol;

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
    // UXO: the three steps (one card, no pop-ups)

    private LinearLayout buildUxoSection() {
        LinearLayout col = vertical();
        col.addView(header("1. WHAT DO YOU SEE?   (do not go closer)"));
        for (final UxoTable.Category c : UxoTable.pickerCategories()) {
            Button b = button(c.plainLabel + "   "
                    + Formats.thousands(c.cordonM) + " m", BTN);
            b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            b.setTag(c.id);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uxoCat = c.id;
                    if (!c.isUnknown())
                        uxoLarge = false;
                    updateUxo();
                }
            });
            uxoCatButtons.add(b);
            LinearLayout.LayoutParams lp = full();
            lp.setMargins(0, 0, 0, dp(2));
            col.addView(b, lp);
        }
        largeBtn = button("It looks large (bomb or missile size): "
                + Formats.thousands(UxoTable.UNKNOWN_LARGE_M) + " m", BTN);
        largeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uxoLarge = !uxoLarge;
                updateUxo();
            }
        });
        col.addView(largeBtn, full());
        multiCheck = new CheckBox(plugin);
        multiCheck.setText("More than one item (+" + UxoTable.MULTI_ADD_M
                + " m)");
        multiCheck.setTextColor(TEXT);
        multiCheck.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton b, boolean on) {
                        uxoMulti = on;
                        updateUxo();
                    }
                });
        col.addView(multiCheck);

        col.addView(header("2. WHERE ARE YOU?"));
        Object[][] wheres = {
                { UxoRecord.Where.PATH, "On a road or path I know is safe" },
                { UxoRecord.Where.AREA,
                        "I may be in a minefield or an area with bomblets" },
                { UxoRecord.Where.VEHICLE, "In a vehicle" },
        };
        for (Object[] w : wheres) {
            final UxoRecord.Where where = (UxoRecord.Where) w[0];
            Button b = button((String) w[1], BTN);
            b.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
            b.setTag(where);
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    uxoWhere = where;
                    updateUxo();
                }
            });
            whereButtons.add(b);
            LinearLayout.LayoutParams lp = full();
            lp.setMargins(0, 0, 0, dp(2));
            col.addView(b, lp);
        }

        uxoSelText = text("", 13, TEXT, false);
        uxoSelText.setPadding(0, dp(4), 0, dp(4));
        col.addView(uxoSelText);

        col.addView(header("3. PLACE IT (never walk closer to get a fix)"));
        uxoTargetBtn = button("", SEND);
        uxoTargetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (placeTarget != null && placeTarget.getPoint() != null)
                    placeUxo(placeTarget.getPoint(),
                            UxoRecord.Placement.MARKER);
            }
        });
        col.addView(uxoTargetBtn, full());
        LinearLayout place = horizontal();
        uxoPickBtn = button("Tap map", BTN);
        uxoRbBtn = button("Range & bearing", BTN);
        uxoCenterBtn = button("Map center", BTN);
        uxoPickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (picking)
                    stopPick();
                else
                    startPick(Mode.UXO);
            }
        });
        uxoRbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptRangeBearing();
            }
        });
        uxoCenterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeoPointMetaData c = mapView.getCenterPoint();
                if (c != null && c.get() != null && c.get().isValid())
                    placeUxo(c.get(), UxoRecord.Placement.CENTER);
            }
        });
        place.addView(uxoPickBtn, weight());
        place.addView(uxoRbBtn, weight());
        place.addView(uxoCenterBtn, weight());
        col.addView(place);
        col.addView(text("UXO incidents are never sent automatically: "
                + "share them from the incident card when you choose.", 12,
                DIM, false));
        return col;
    }

    private void setMode(Mode m) {
        stopPick();
        tint(modeIedBtn, m == Mode.IED ? SEL : BTN);
        tint(modeUxoBtn, m == Mode.UXO ? UXO_SEL : BTN);
        iedSection.setVisibility(m == Mode.IED ? View.VISIBLE : View.GONE);
        uxoSection.setVisibility(m == Mode.UXO ? View.VISIBLE : View.GONE);
        updatePlacement();
        updateUxo();
    }

    private boolean uxoReady() {
        return uxoCat != null && uxoWhere != UxoRecord.Where.UNSET;
    }

    private void updateUxo() {
        if (uxoSection == null)
            return;
        for (Button b : uxoCatButtons)
            tint(b, b.getTag().equals(uxoCat) ? UXO_SEL : BTN);
        for (Button b : whereButtons)
            tint(b, b.getTag() == uxoWhere ? UXO_SEL : BTN);
        boolean unknown = UxoTable.UNKNOWN.equals(uxoCat);
        largeBtn.setVisibility(unknown ? View.VISIBLE : View.GONE);
        tint(largeBtn, uxoLarge ? UXO_SEL : BTN);
        if (multiCheck.isChecked() != uxoMulti)
            multiCheck.setChecked(uxoMulti);
        if (uxoCat == null) {
            uxoSelText.setText("Choose what you see, then where you are.");
            uxoSelText.setTextColor(DIM);
        } else {
            int m = UxoTable.cordonM(uxoCat, uxoMulti, unknown && uxoLarge);
            String s = "Cordon " + UxoText.cordon(m) + ", minimum. "
                    + "Fragments may travel farther.";
            if (uxoWhere == UxoRecord.Where.UNSET)
                s += "\nNow choose where you are.";
            else
                s += "\n" + UxoText.drill(uxoWhere);
            uxoSelText.setText(s);
            uxoSelText.setTextColor(uxoWhere == UxoRecord.Where.UNSET ? AMBER
                    : TEXT);
        }
        boolean ok = uxoReady();
        for (Button b : new Button[] { uxoPickBtn, uxoRbBtn, uxoCenterBtn }) {
            b.setEnabled(ok);
            b.setAlpha(ok ? 1f : 0.4f);
        }
        if (placeTarget != null) {
            uxoTargetBtn.setVisibility(View.VISIBLE);
            uxoTargetBtn.setText("Place at "
                    + ATAKUtilities.getDisplayName(placeTarget));
            uxoTargetBtn.setEnabled(ok);
            uxoTargetBtn.setAlpha(ok ? 1f : 0.4f);
        } else {
            uxoTargetBtn.setVisibility(View.GONE);
        }
    }

    private void placeUxo(GeoPoint at, UxoRecord.Placement how) {
        try {
            if (!uxoReady()) {
                toast("Choose what you see and where you are first.");
                return;
            }
            UxoRecord r = UxoRecord.create(uxoCat, uxoMulti,
                    UxoTable.UNKNOWN.equals(uxoCat) && uxoLarge, uxoWhere, how,
                    callsign(), System.currentTimeMillis());
            Marker m = store.createUxo(at, r);
            if (m == null) {
                toast("Could not place the incident.");
                return;
            }
            placeTarget = null;
            selectedZoneUid = m.getUID();
            resetThreat();
            toast("UXO incident placed (not sent). Use REPORT or SEND on its "
                    + "card when you are outside the cordon.");
            onShow();
            main.post(new Runnable() {
                @Override
                public void run() {
                    root.smoothScrollTo(0, 0);
                }
            });
        } catch (RuntimeException e) {
            Log.e(TAG, "UXO place failed", e);
            toast("Could not place the incident.");
        }
    }

    /** Place by distance and bearing from the user's own position. */
    private void promptRangeBearing() {
        final GeoPoint self = selfPoint();
        if (self == null) {
            toast("No GPS position yet: tap the map instead.");
            return;
        }
        Context ctx = mapView.getContext();
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        int p = dp(16);
        box.setPadding(p, dp(8), p, 0);
        final EditText dist = new EditText(ctx);
        dist.setHint("Distance to the item");
        dist.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        dist.setSingleLine(true);
        box.addView(dist);
        final CheckBox feet = new CheckBox(ctx);
        feet.setText("Distance in feet (otherwise metres)");
        box.addView(feet);
        final EditText brg = new EditText(ctx);
        brg.setHint("Bearing to the item, degrees (0-359)");
        brg.setInputType(InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        brg.setSingleLine(true);
        box.addView(brg);
        final CheckBox magnetic = new CheckBox(ctx);
        magnetic.setText("Magnetic bearing (from a compass)");
        magnetic.setChecked(true);
        box.addView(magnetic);
        TextView note = new TextView(ctx);
        note.setText("Measured from your GPS position. Estimate from where "
                + "you are now; never walk closer to measure.");
        box.addView(note);
        new AlertDialog.Builder(ctx)
                .setTitle("Place by range & bearing")
                .setView(box)
                .setPositiveButton("Place",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                double r = ZoneCalculator.parseQuantity(
                                        dist.getText().toString());
                                double b = ZoneCalculator.parseQuantity(
                                        brg.getText().toString());
                                if (feet.isChecked())
                                    r = Formats.ftToM(r);
                                if (!(r >= 1 && r <= 20000)) {
                                    toast("Enter a distance between 1 m and "
                                            + "20 km.");
                                    return;
                                }
                                if (!(b >= 0 && b <= 360)) {
                                    toast("Enter a bearing from 0 to 359.");
                                    return;
                                }
                                double trueB = b;
                                if (magnetic.isChecked()) {
                                    try {
                                        trueB = ATAKUtilities
                                                .convertFromMagneticToTrue(
                                                        self, b);
                                    } catch (RuntimeException e) {
                                        Log.w(TAG, "declination unavailable", e);
                                        toast("Magnetic declination "
                                                + "unavailable: enter a true "
                                                + "bearing.");
                                        return;
                                    }
                                }
                                double[] ll = Geo.destination(
                                        self.getLatitude(), self.getLongitude(),
                                        ((trueB % 360) + 360) % 360, r);
                                GeoPoint at = new GeoPoint(ll[0], ll[1]);
                                if (!at.isValid()) {
                                    toast("Could not compute the position.");
                                    return;
                                }
                                placeUxo(at, UxoRecord.Placement.RB);
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ------------------------------------------------------------------
    // UXO incident card

    private void buildUxoCard(final Marker m, final UxoRecord r) {
        zoneCard.setVisibility(View.VISIBLE);
        zoneCard.addView(header("SELECTED INCIDENT"));
        LinearLayout card = vertical();
        card.setBackgroundColor(CARD);
        int p = dp(8);
        card.setPadding(p, p, p, p);
        zoneCard.addView(card);

        card.addView(text(ATAKUtilities.getDisplayName(m), 17,
                r.isCleared() ? DIM : TEXT, true));
        card.addView(text(UxoText.threat(r) + (r.isCleared() ? " · CLEARED"
                : ""), 13, r.isCleared() ? DIM : ORANGE, true));
        TextView cordon = text("CORDON " + UxoText.cordon(r.cordonM)
                + "  (minimum)", 18, r.isCleared() ? DIM : ORANGE, true);
        cordon.setPadding(0, dp(4), 0, 0);
        card.addView(cordon);
        card.addView(text(UxoText.basis(r) + " Fragments may travel farther.",
                12, DIM, false));
        if (!r.isKnownEdition())
            card.addView(text("This build doesn't have " + r.sourceId + " "
                    + r.edition + ". Showing the distance the sender drew.",
                    12, AMBER, false));
        else if (r.receivedMismatch)
            card.addView(text("The sender's distance differs from this "
                    + "build's copy of the same table. This build's distance "
                    + "is drawn. Check with the sender.", 12, RED, false));

        youText = text("", 14, TEXT, false);
        youText.setPadding(0, dp(6), 0, 0);
        card.addView(youText);
        teamText = text("", 12, DIM, false);
        card.addView(teamText);

        Button report = button(r.report.isEmpty() ? "REPORT / 9-LINE…"
                : "REPORT / 9-LINE… (edit)", SEND);
        report.setTypeface(Typeface.DEFAULT_BOLD);
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptReport(m);
            }
        });
        LinearLayout.LayoutParams rl = full();
        rl.setMargins(0, dp(6), 0, 0);
        card.addView(report, rl);

        card.addView(sub("DO NOW"));
        int i = 1;
        for (String line : UxoText.doNow(r))
            card.addView(text((i++) + "  " + line, 13, TEXT, false));
        card.addView(text("Emergency number: " + emergencyNumber()
                + " (change it in the Reference tab)", 12, DIM, false));

        card.addView(sub("MOVING AWAY"));
        UxoRecord.Where first = r.where == UxoRecord.Where.UNSET
                ? UxoRecord.Where.PATH : r.where;
        if (r.where != UxoRecord.Where.UNSET)
            card.addView(text("Reported from " + UxoText.whereLabel(r.where)
                    + ".", 12, DIM, false));
        card.addView(text(UxoText.drill(first), 13, TEXT, true));
        for (UxoRecord.Where w : new UxoRecord.Where[] { UxoRecord.Where.PATH,
                UxoRecord.Where.AREA, UxoRecord.Where.VEHICLE })
            if (w != first)
                card.addView(text(UxoText.drill(w), 12, DIM, false));

        String rep = UxoText.nineLine(r, r.byCallsign, grid(m));
        if (!r.report.isEmpty()) {
            card.addView(sub("REPORT (saved on this incident)"));
            TextView rt = text(rep, 12, TEXT, false);
            rt.setTypeface(Typeface.MONOSPACE);
            card.addView(rt);
        }

        LinearLayout a1 = horizontal();
        Button sendAll = button("SEND TO ALL", SEND);
        sendAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSendUxo(m);
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
        LinearLayout.LayoutParams al = full();
        al.setMargins(0, dp(6), 0, 0);
        card.addView(a1, al);
        addCommonActions(card, m, r.isCleared());

        Button why = button("Why this distance?", BTN);
        why.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTab(true, UxoReference.RINGS);
            }
        });
        card.addView(why, full());
        for (String c : UxoText.CAVEATS)
            card.addView(text("• " + c, 12, DIM, false));
        TextView prov = text(UxoText.provenance(r), 11, DIM, false);
        prov.setPadding(0, dp(4), 0, dp(4));
        card.addView(prov);
    }

    /** Rename / Export / Clear / Delete, shared by both kinds of card. */
    private void addCommonActions(LinearLayout card, final Marker m,
            boolean cleared) {
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
        Button clear = button(cleared ? "Reopen" : "Mark cleared", BTN);
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
    }

    /** UXO sending is deliberate: confirm before broadcasting. */
    private void confirmSendUxo(Marker card) {
        final Marker m = live(card);
        if (m == null)
            return;
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Send to everyone?")
                .setMessage("Sends the marker, cordon and report to every "
                        + "connected TAK client.\n\n" + UxoText.REPORT_CAUTION
                        + "\n\nOnly transmit from outside the cordon.")
                .setPositiveButton("Send to all",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                try {
                                    Marker l = live(m);
                                    if (l == null)
                                        return;
                                    int n = sharing.sendToAll(l);
                                    toast("Sent to everyone (" + n
                                            + " items).");
                                } catch (RuntimeException e) {
                                    Log.e(TAG, "send failed", e);
                                    toast("Send failed.");
                                }
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateYouUxo(Marker m, UxoRecord r) {
        if (youText == null)
            return;
        GeoPoint center = m.getPoint();
        Marker self = ATAKUtilities.findSelf(mapView);
        GeoPoint sp = self != null ? self.getPoint() : null;
        if (center == null || sp == null || !sp.isValid()
                || (sp.getLatitude() == 0 && sp.getLongitude() == 0)) {
            youText.setText("YOU: no GPS position yet.");
            youText.setTextColor(DIM);
        } else {
            double ce = sp.getCE();
            boolean ceKnown = !Double.isNaN(ce) && ce > 0 && ce < 9999999;
            UxoText.You you = UxoText.check(r.cordonM, distance(center, sp),
                    ceKnown ? ce : Double.NaN);
            if (you == null) {
                youText.setText("YOU: position check unavailable.");
                youText.setTextColor(DIM);
            } else {
                String s = "YOU: " + you.headline(r.cordonM) + "\n"
                        + you.guidance(r.cordonM);
                String w = you.boundaryWarning();
                if (w != null)
                    s += "\n" + w;
                if (ceKnown && ce > 50)
                    s += "\nGPS accuracy is poor (about "
                            + Formats.measured(ce)
                            + "): treat this check as approximate.";
                if (r.placement == UxoRecord.Placement.RB)
                    s += "\nThe item's position is an estimate (range and "
                            + "bearing).";
                if (r.isCleared())
                    s = "Incident marked cleared. " + s;
                youText.setText(s);
                youText.setTextColor(r.isCleared() ? DIM
                        : you.inside ? RED : GREEN);
            }
        }
        if (teamText != null && center != null) {
            double[] arr = teamDistances(center, self);
            int[] n = UxoText.count(r.cordonM, arr);
            teamText.setText(arr.length == 0
                    ? "Team: no other contacts on the map."
                    : "Team on the map: " + n[0] + " inside the cordon, "
                            + n[1] + " outside.");
            teamText.setTextColor(n[0] > 0 && !r.isCleared() ? RED : DIM);
        }
    }

    private double[] teamDistances(GeoPoint center, Marker self) {
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
        return arr;
    }

    /** The item's position in MGRS (as ATAK formats it). */
    private static String grid(Marker m) {
        GeoPoint gp = m == null ? null : m.getPoint();
        if (gp == null || !gp.isValid())
            return "";
        try {
            return com.atakmap.coremap.conversions.CoordinateFormatUtilities
                    .formatToString(gp,
                            com.atakmap.coremap.conversions.CoordinateFormat.MGRS);
        } catch (RuntimeException e) {
            return (Math.round(gp.getLatitude() * 1e5) / 1e5) + ", "
                    + (Math.round(gp.getLongitude() * 1e5) / 1e5);
        }
    }

    // ------------------------------------------------------------------
    // Report (simple or EOD 9-line), saved on the incident

    private void promptReport(Marker card) {
        final Marker m = live(card);
        if (m == null)
            return;
        final UxoRecord r0 = BlastStore.readUxo(m);
        if (r0 == null)
            return;
        final UxoRecord.Report rp = r0.report;
        final Context ctx = mapView.getContext();
        ScrollView sv = new ScrollView(ctx);
        final LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.VERTICAL);
        int pd = dp(16);
        box.setPadding(pd, dp(8), pd, 0);
        sv.addView(box);

        final boolean[] nine = { false };
        final TextView preview = new TextView(ctx);
        preview.setTypeface(Typeface.MONOSPACE);
        preview.setTextIsSelectable(true);
        preview.setPadding(0, dp(8), 0, dp(8));

        final CheckBox nineBox = new CheckBox(ctx);
        nineBox.setText("Military EOD 9-line format (otherwise a plain "
                + "what / where / when report)");
        box.addView(nineBox);

        final EditText count = field(ctx, box, "Number of items seen",
                rp.count > 0 ? Integer.toString(rp.count) : "", true);
        final EditText desc = field(ctx, box,
                "Description: size, shape, colour, markings, condition",
                rp.description, false);
        final EditText land = field(ctx, box, "Landmark / how to find it",
                rp.landmark, false);
        final String[] emps = { "", "Dropped", "Projected", "Placed",
                "Thrown", "Possible IED", "Unknown" };
        final int[] emp = { 0 };
        for (int i = 0; i < emps.length; i++)
            if (emps[i].equals(rp.emplacement))
                emp[0] = i;
        final Button empBtn = new Button(ctx);
        empBtn.setAllCaps(false);
        box.addView(empBtn);
        final EditText cbrn = field(ctx, box,
                "Chemical or other signs (leaks, smell, sick people)", rp.cbrn,
                false);
        final EditText res = field(ctx, box,
                "Resources threatened (road, homes, school…)", rp.resources,
                false);
        final EditText imp = field(ctx, box, "Impact (road closed, …)",
                rp.impact, false);
        final EditText prot = field(ctx, box,
                "Protective measures taken (e.g. people kept out)",
                rp.protective, false);
        final UxoRecord.Priority[] pris = UxoRecord.Priority.values();
        final int[] pri = { rp.priority.ordinal() };
        final Button priBtn = new Button(ctx);
        priBtn.setAllCaps(false);
        box.addView(priBtn);
        final EditText poc = field(ctx, box,
                "Contact (callsign, phone, frequency)", rp.contact, false);
        box.addView(preview);
        TextView caution = new TextView(ctx);
        caution.setText(UxoText.REPORT_CAUTION + " " + UxoText.PRIORITY_NOTE
                + "\nEmergency number: " + emergencyNumber());
        box.addView(caution);

        final Runnable update = new Runnable() {
            @Override
            public void run() {
                empBtn.setText("Line 4, how it got there: "
                        + (emp[0] == 0 ? "(not given)" : emps[emp[0]]));
                priBtn.setText("Line 9, priority: "
                        + (pri[0] == 0 ? "(not given)" : pris[pri[0]].label));
                priBtn.setVisibility(nine[0] ? View.VISIBLE : View.GONE);
                empBtn.setVisibility(nine[0] ? View.VISIBLE : View.GONE);
                res.setVisibility(nine[0] ? View.VISIBLE : View.GONE);
                imp.setVisibility(nine[0] ? View.VISIBLE : View.GONE);
                UxoRecord r = r0.withReport(collect(count, desc, land,
                        emps[emp[0]], cbrn, res, imp, prot, pris[pri[0]], poc));
                preview.setText(nine[0]
                        ? UxoText.nineLine(r, callsign(), grid(m))
                        : UxoText.simpleReport(r, grid(m)));
            }
        };
        nineBox.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton b, boolean on) {
                        nine[0] = on;
                        update.run();
                    }
                });
        empBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emp[0] = (emp[0] + 1) % emps.length;
                update.run();
            }
        });
        priBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pri[0] = (pri[0] + 1) % pris.length;
                update.run();
            }
        });
        TextWatcher tw = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence s, int a, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                update.run();
            }
        };
        for (EditText e : new EditText[] { count, desc, land, cbrn, res, imp,
                prot, poc })
            e.addTextChangedListener(tw);
        update.run();

        new AlertDialog.Builder(ctx)
                .setTitle("Report: " + BlastStore.nameOf(m))
                .setView(sv)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                saveReport(m, collect(count, desc, land,
                                        emps[emp[0]], cbrn, res, imp, prot,
                                        pris[pri[0]], poc));
                            }
                        })
                .setNeutralButton("Copy text",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                saveReport(m, collect(count, desc, land,
                                        emps[emp[0]], cbrn, res, imp, prot,
                                        pris[pri[0]], poc));
                                copy(preview.getText().toString());
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private EditText field(Context ctx, LinearLayout box, String hint,
            String value, boolean number) {
        TextView label = new TextView(ctx);
        label.setText(hint);
        label.setPadding(0, dp(6), 0, 0);
        box.addView(label);
        EditText e = new EditText(ctx);
        e.setText(value);
        e.setInputType(number ? InputType.TYPE_CLASS_NUMBER
                : InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        e.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(
                        number ? 4 : com.atakmap.android.blast.plugin.core
                                .UxoCodec.MAX_REPORT_TEXT)
        });
        box.addView(e);
        return e;
    }

    private static UxoRecord.Report collect(EditText count, EditText desc,
            EditText land, String emp, EditText cbrn, EditText res,
            EditText imp, EditText prot, UxoRecord.Priority pri,
            EditText poc) {
        int n = 0;
        try {
            n = Integer.parseInt(count.getText().toString().trim());
        } catch (NumberFormatException ignored) {
        }
        return new UxoRecord.Report(n, clean(desc), clean(land), emp,
                clean(cbrn), clean(res), clean(imp), clean(prot), pri,
                clean(poc));
    }

    private static String clean(EditText e) {
        return com.atakmap.android.blast.plugin.core.UxoCodec.longText(
                e.getText().toString());
    }

    private void saveReport(Marker card, UxoRecord.Report rep) {
        try {
            Marker m = live(card);
            UxoRecord r = m == null ? null : BlastStore.readUxo(m);
            if (r == null)
                return;
            if (!store.writeUxo(m, r.withReport(rep))) {
                live(null);
                return;
            }
            toast("Report saved on the incident. Share it with SEND TO ALL or "
                    + "Send to… when you choose.");
            cardSignature = null;
            listSignature = null;
            refresh();
        } catch (RuntimeException e) {
            Log.e(TAG, "report save failed", e);
            toast("Could not save the report.");
        }
    }

    private void copy(String s) {
        try {
            android.content.ClipboardManager cm =
                    (android.content.ClipboardManager) mapView.getContext()
                            .getSystemService(Context.CLIPBOARD_SERVICE);
            if (cm != null) {
                cm.setPrimaryClip(android.content.ClipData.newPlainText(
                        "Blast report", s));
                toast("Report copied. Paste it into a text, chat or email.");
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "copy failed", e);
            toast("Could not copy the report.");
        }
    }

    private String emergencyNumber() {
        String n = prefs.getString(PREF_EMERGENCY, DEFAULT_EMERGENCY);
        return n == null || n.trim().isEmpty() ? DEFAULT_EMERGENCY : n.trim();
    }

    private void promptEmergencyNumber() {
        final EditText input = new EditText(mapView.getContext());
        input.setSingleLine(true);
        input.setText(emergencyNumber());
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        input.setFilters(new android.text.InputFilter[] {
                new android.text.InputFilter.LengthFilter(24)
        });
        new AlertDialog.Builder(mapView.getContext())
                .setTitle("Emergency number")
                .setMessage("Shown on UXO cards and reports, e.g. 911, 112, "
                        + "101 / 102 or a mine action centre.")
                .setView(input)
                .setPositiveButton("Save",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface d, int w) {
                                String v = Names.sanitize(
                                        input.getText().toString());
                                prefs.edit().putString(PREF_EMERGENCY,
                                        v == null ? DEFAULT_EMERGENCY : v)
                                        .apply();
                                if (emergencyText != null)
                                    emergencyText.setText("Emergency number: "
                                            + emergencyNumber());
                                cardSignature = null;
                                refresh();
                            }
                        })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ------------------------------------------------------------------
    // Reference tab

    private LinearLayout buildReference() {
        LinearLayout col = vertical();
        LinearLayout num = horizontal();
        num.setGravity(Gravity.CENTER_VERTICAL);
        emergencyText = text("Emergency number: " + emergencyNumber(), 14,
                TEXT, true);
        num.addView(emergencyText, weight());
        Button change = button("Change…", BTN);
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptEmergencyNumber();
            }
        });
        num.addView(change);
        col.addView(num);
        for (UxoReference.Section sec : UxoReference.sections()) {
            TextView h = header(sec.title.toUpperCase(java.util.Locale.US));
            refAnchors.put(sec.id, h);
            col.addView(h);
            for (String para : sec.body) {
                boolean bullet = para.startsWith("• ");
                TextView t = text(para, 13, bullet ? TEXT : DIM, false);
                t.setPadding(bullet ? dp(8) : 0, 0, 0, dp(3));
                t.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
                col.addView(t);
            }
        }
        TextView foot = text(Guidance.DISCLAIMER, 11, DIM, false);
        foot.setPadding(0, dp(12), 0, dp(4));
        col.addView(foot);
        return col;
    }

    private TextView sub(String s) {
        TextView t = text(s, 12, BLUE, true);
        t.setPadding(0, dp(8), 0, dp(2));
        return t;
    }

    // ------------------------------------------------------------------
    // Threat selection

    private void selectRow(String rowId) {
        customMode = false;
        selectedRowId = rowId;
        recomputeSelection();
    }

    /**
     * Clear the threat choice. Called when the pane is opened and after
     * each incident is placed, so every incident needs a fresh choice.
     */
    public void resetThreat() {
        customMode = false;
        selectedRowId = null;
        if (qtyEdit != null)
            qtyEdit.setText("");
        uxoCat = null;
        uxoLarge = false;
        uxoMulti = false;
        uxoWhere = UxoRecord.Where.UNSET;
        recomputeSelection();
        if (modeIedBtn != null)
            setMode(Mode.NONE);
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
            resetThreat();
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

    private void startPick(Mode kind) {
        if (kind == Mode.IED && (selection == null || !selection.isUsable()))
            return;
        if (kind == Mode.UXO && !uxoReady())
            return;
        pickKind = kind;
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
        (kind == Mode.UXO ? uxoPickBtn : pickBtn).setText("Cancel");
        TextContainer.getInstance().displayPrompt(kind == Mode.UXO
                ? "Tap the map where the item is (don't go closer). "
                        + "Press Cancel to stop."
                : "Tap the map or a marker where the device is. "
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
        Mode kind = pickKind;
        stopPick();
        if (gp == null || !gp.isValid())
            return;
        if (kind == Mode.UXO)
            placeUxo(gp, MapEvent.ITEM_CLICK.equals(event.getType())
                    ? UxoRecord.Placement.MARKER : UxoRecord.Placement.MAP);
        else
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
        pickKind = Mode.NONE;
        if (pickBtn != null)
            pickBtn.setText("Tap map");
        if (uxoPickBtn != null)
            uxoPickBtn.setText("Tap map");
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
        UxoRecord u = m == null ? null : BlastStore.readUxo(m);
        BlastRecord r = m == null || u != null ? null : BlastStore.read(m);
        String sig = m == null ? "" : m.getUID() + "|"
                + m.getMetaString(BlastStore.META, "") + "|"
                + m.getMetaString(BlastStore.META_UXO, "") + "|"
                + ATAKUtilities.getDisplayName(m) + "|" + emergencyNumber();
        if (!sig.equals(cardSignature)) {
            cardSignature = sig;
            if (u != null) {
                zoneCard.removeAllViews();
                youText = null;
                teamText = null;
                buildUxoCard(m, u);
            } else {
                buildCard(m, r);
            }
        }
        if (m != null && u != null)
            updateYouUxo(m, u);
        else if (m != null && r != null)
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
        final java.util.Map<String, UxoRecord> uxos = new java.util.HashMap<>();
        final java.util.Map<String, Boolean> clr = new java.util.HashMap<>();
        final java.util.Map<String, Double> dist = new java.util.HashMap<>();
        List<Marker> usable = new ArrayList<>();
        for (Marker m : zones) {
            UxoRecord u = BlastStore.readUxo(m);
            BlastRecord r = u == null ? BlastStore.read(m) : null;
            if (u == null && r == null)
                continue;
            if (u != null)
                uxos.put(m.getUID(), u);
            else
                recs.put(m.getUID(), r);
            clr.put(m.getUID(), u != null ? u.isCleared() : r.isCleared());
            dist.put(m.getUID(), distance(ref, m));
            usable.add(m);
        }
        zones = usable;
        Collections.sort(zones, new Comparator<Marker>() {
            @Override
            public int compare(Marker a, Marker b) {
                // Active incidents first, then by distance.
                boolean ca = clr.get(a.getUID());
                boolean cb = clr.get(b.getUID());
                if (ca != cb)
                    return ca ? 1 : -1;
                return Double.compare(dist.get(a.getUID()),
                        dist.get(b.getUID()));
            }
        });
        int cleared = 0;
        StringBuilder sig = new StringBuilder();
        for (Marker m : zones) {
            boolean c = clr.get(m.getUID());
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
            zoneList.addView(text("None. Choose the kind of threat below and "
                    + "place it.", 13, DIM, false));
            return;
        }
        for (final Marker m : zones) {
            final UxoRecord u = uxos.get(m.getUID());
            BlastRecord r = recs.get(m.getUID());
            boolean isClr = clr.get(m.getUID());
            boolean sel = m.getUID().equals(selectedZoneUid);
            double d = dist.get(m.getUID());
            String where = "";
            if (!Double.isNaN(d)) {
                where = "\n" + Formats.measured(d);
                if (!isClr) {
                    if (u != null)
                        where += " · you: " + (d < u.cordonM ? "INSIDE"
                                : "outside") + " cordon "
                                + Formats.thousands(u.cordonM) + " m";
                    else
                        where += " · you: "
                                + PositionCheck.zoneOf(r.row(), d).title;
                }
            }
            LinearLayout row = horizontal();
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(sel ? SEL : CARD);
            int p = dp(6);
            row.setPadding(p, p, p, p);
            View sw = new View(plugin);
            sw.setBackgroundColor(isClr ? DIM : u != null ? ORANGE : RED);
            LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(dp(6),
                    dp(36));
            sl.setMargins(0, 0, dp(6), 0);
            row.addView(sw, sl);
            TextView t = text(ATAKUtilities.getDisplayName(m) + where, 14,
                    isClr ? DIM : TEXT, sel);
            if (r != null) {
                setIcon(t, threatIcon(r.rowId));
                t.setCompoundDrawablePadding(dp(6));
            }
            row.addView(t, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            Button go = button("Go to", BTN);
            go.setMinWidth(dp(52));
            go.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    goTo(m);
                }
            });
            row.addView(go);
            if (u != null) {
                Button nl = button("9-line", BTN);
                nl.setMinWidth(dp(52));
                nl.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedZoneUid = m.getUID();
                        listSignature = null;
                        cardSignature = null;
                        refresh();
                        promptReport(m);
                    }
                });
                row.addView(nl);
            }
            Button del = button("Delete", BTN);
            del.setMinWidth(dp(52));
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
        UxoRecord u = BlastStore.readUxo(m);
        if (u != null)
            return u.isCleared();
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
        if (pref == null)
            pref = store.liveItem(m.getUID() + BlastStore.CORDON_SUFFIX);
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
        if (!BlastStore.isIncident(m))
            return;
        final boolean clearing = !isCleared(m);
        new AlertDialog.Builder(mapView.getContext())
                .setTitle(clearing ? "Mark incident cleared?"
                        : "Reopen incident?")
                .setMessage(clearing
                        ? "Records your callsign and the time. The rings "
                                + "turn grey and stay on the map until deleted."
                                + (BlastStore.isUxo(m) ? "\n\nOnly mark a UXO "
                                        + "incident cleared when authorities "
                                        + "say it has been removed." : "")
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
            if (m == null)
                return;
            UxoRecord u = BlastStore.readUxo(m);
            boolean ok;
            if (u != null) {
                ok = store.writeUxo(m, clearing
                        ? u.cleared(callsign(), System.currentTimeMillis())
                        : u.reopened());
            } else {
                BlastRecord r = BlastStore.read(m);
                if (r == null)
                    return;
                ok = store.write(m, clearing
                        ? r.cleared(callsign(), System.currentTimeMillis())
                        : r.reopened());
            }
            if (!ok) {
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
                .setMessage("Removes the marker and the rings of this "
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
