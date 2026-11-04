package com.atakmap.android.blast.plugin.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A nuclear detonation reference incident (immutable). The rings are
 * derived from yield and burst condition by {@link NucModel}; a record
 * received for a model edition this build lacks keeps the sender's radii.
 */
public final class NucRecord {

    public static final int VERSION = 1;

    /** The drawn distances, metres. */
    public static final class Radii {
        public final double sdzM, mdzM, ldzM, burnM, radM;

        public Radii(double sdzM, double mdzM, double ldzM, double burnM,
                double radM) {
            this.sdzM = sdzM;
            this.mdzM = mdzM;
            this.ldzM = ldzM;
            this.burnM = burnM;
            this.radM = radM;
        }

        public static Radii of(double kt, NucModel.Burst b) {
            return new Radii(NucModel.sdzM(kt, b), NucModel.mdzM(kt, b),
                    NucModel.ldzM(kt, b), NucModel.burnM(kt, b),
                    NucModel.radiationM(kt, b));
        }

        /** Same to the metre (what the CoT carries). */
        boolean sameAs(Radii o) {
            return Math.round(sdzM) == Math.round(o.sdzM)
                    && Math.round(mdzM) == Math.round(o.mdzM)
                    && Math.round(ldzM) == Math.round(o.ldzM)
                    && Math.round(burnM) == Math.round(o.burnM)
                    && Math.round(radM) == Math.round(o.radM);
        }
    }

    public final int version;
    public final String modelId;
    public final String modelEd;
    public final double yieldKt;
    public final NucModel.Burst burst;
    /** Preset id, or "" for a typed yield. */
    public final String presetId;
    /** Preset label (carried so other builds can show it), or "". */
    public final String presetLabel;
    /** Direction the wind blows FROM, degrees true; NaN when unknown. */
    public final double windFromDeg;
    /** Wind speed, mph; NaN when unknown. */
    public final double windMph;
    public final String byCallsign;
    public final long timeMillis;
    public final BlastRecord.Status status;
    public final String clearedBy;
    public final long clearedMillis;
    public final int number;
    public final Radii radii;
    public final Map<String, String> extra;
    /** The sender's radii differ from this build's for the same model. */
    public final boolean receivedMismatch;
    /** Military fallout picture (FM 3-3-1 / FM 3-11.3) settings. */
    public final Mil mil;
    /** NBC 1 NUC observer data. */
    public final Obs obs;

    /**
     * Military (simplified fallout prediction) options. {@code on} shows
     * Zones I/II instead of the civil contours. A wind from an EDM line is
     * the effective downwind; a 3-digit EDM gives the circular Zone I.
     */
    public static final class Mil {
        public static final Mil OFF = new Mil(false, Double.NaN, Double.NaN,
                false);
        public final boolean on;
        /** Expanded sector angle from the EDM, degrees; NaN = 40. */
        public final double angleDeg;
        /** Zone I distance from a 3-digit EDM line, km; NaN otherwise. */
        public final double edmZoneIKm;
        /** The wind on the record is an effective downwind from an EDM. */
        public final boolean edmWind;

        public Mil(boolean on, double angleDeg, double edmZoneIKm,
                boolean edmWind) {
            this.on = on;
            this.angleDeg = angleDeg >= NucMil.STD_ANGLE_DEG
                    && angleDeg <= NucMil.MAX_ANGLE_DEG ? angleDeg : Double.NaN;
            this.edmZoneIKm = edmZoneIKm >= 0.1 && edmZoneIKm <= 2000
                    ? edmZoneIKm : Double.NaN;
            this.edmWind = edmWind;
        }

        public Mil withOn(boolean v) {
            return new Mil(v, angleDeg, edmZoneIKm, edmWind);
        }
    }

    /** What an observer saw, for the NBC 1 NUC report (FM 3-11.3 Fig. G-3). */
    public static final class Obs {
        public static final Obs NONE = new Obs(Double.NaN, Double.NaN,
                Double.NaN, Double.NaN, Double.NaN, "", "SUS", "UNK", "UNK",
                "EE");
        /** Observer position (BRAVO). */
        public final double obsLat, obsLon;
        /** Direction of attack from the observer, degrees true (BRAVO). */
        public final double azDeg;
        /** Flash-to-bang, seconds (JULIET). */
        public final double flashToBangS;
        /** Angular cloud width at H+5 min, degrees (LIMA). */
        public final double cloudWidthDeg;
        /** KILO crater indicator: CRATER, NONE, UNK or "". */
        public final String crater;
        /** GOLF: SUS / OBS, delivery type, container type. */
        public final String golfSo, golfDelivery, golfContainer;
        /** FOXTROT qualifier: AA actual (GZ seen) or EE estimated. */
        public final String gzQualifier;

        public Obs(double obsLat, double obsLon, double azDeg,
                double flashToBangS, double cloudWidthDeg, String crater,
                String golfSo, String golfDelivery, String golfContainer,
                String gzQualifier) {
            boolean pos = obsLat >= -90 && obsLat <= 90 && obsLon >= -180
                    && obsLon <= 180;
            this.obsLat = pos ? obsLat : Double.NaN;
            this.obsLon = pos ? obsLon : Double.NaN;
            this.azDeg = azDeg >= 0 && azDeg <= 360 ? normDeg(azDeg)
                    : Double.NaN;
            this.flashToBangS = flashToBangS >= 0 && flashToBangS <= 300
                    ? flashToBangS : Double.NaN;
            this.cloudWidthDeg = cloudWidthDeg > 0 && cloudWidthDeg < 180
                    ? cloudWidthDeg : Double.NaN;
            this.crater = pick(crater, "", "CRATER", "NONE", "UNK");
            this.golfSo = pick(golfSo, "SUS", "OBS");
            this.golfDelivery = pick(golfDelivery, "UNK", "AIR", "MSL",
                    "CAN", "MLR", "SHP");
            this.golfContainer = pick(golfContainer, "UNK", "BOM", "MSL",
                    "SHL", "RKT", "TOR", "MNE");
            this.gzQualifier = pick(gzQualifier, "EE", "AA");
        }

        static String pick(String v, String... allowed) {
            for (String a : allowed)
                if (a.equals(v))
                    return a;
            return allowed[0];
        }

        /** Nothing entered (all defaults). */
        public boolean isEmpty() {
            return Double.isNaN(obsLat) && Double.isNaN(azDeg)
                    && Double.isNaN(flashToBangS) && Double.isNaN(cloudWidthDeg)
                    && crater.isEmpty() && "SUS".equals(golfSo)
                    && "UNK".equals(golfDelivery)
                    && "UNK".equals(golfContainer) && "EE".equals(gzQualifier);
        }

        public boolean hasObserver() {
            return !Double.isNaN(obsLat);
        }
    }

    NucRecord(int version, String modelId, String modelEd, double yieldKt,
            NucModel.Burst burst, String presetId, String presetLabel,
            double windFromDeg, double windMph, String byCallsign,
            long timeMillis, BlastRecord.Status status, String clearedBy,
            long clearedMillis, int number, Radii radii,
            Map<String, String> extra, boolean receivedMismatch, Mil mil,
            Obs obs) {
        this.version = version;
        this.modelId = modelId;
        this.modelEd = modelEd;
        this.yieldKt = yieldKt;
        this.burst = burst;
        this.presetId = presetId == null ? "" : presetId;
        this.presetLabel = presetLabel == null ? "" : presetLabel;
        this.windFromDeg = windFromDeg;
        this.windMph = windMph;
        this.byCallsign = byCallsign == null ? "" : byCallsign;
        this.timeMillis = timeMillis;
        this.status = status;
        this.clearedBy = clearedBy == null ? "" : clearedBy;
        this.clearedMillis = clearedMillis;
        this.number = number;
        this.radii = radii;
        this.extra = Collections.unmodifiableMap(
                new LinkedHashMap<>(extra == null
                        ? Collections.<String, String> emptyMap() : extra));
        this.receivedMismatch = receivedMismatch;
        this.mil = mil == null ? Mil.OFF : mil;
        this.obs = obs == null ? Obs.NONE : obs;
    }

    /** A new incident (yield must satisfy {@link NucModel#validYield}). */
    public static NucRecord create(double kt, NucModel.Burst burst,
            String presetId, double windFromDeg, double windMph,
            String byCallsign, long timeMillis) {
        if (!NucModel.validYield(kt))
            throw new IllegalArgumentException("yield " + kt);
        NucPresets.Preset p = NucPresets.byId(presetId);
        if (p != null && Math.abs(p.kt - kt) > 1e-9)
            p = null;
        boolean wind = validWind(windFromDeg, windMph);
        return new NucRecord(VERSION, NucModel.MODEL_ID, NucModel.MODEL_ED,
                kt, burst, p == null ? "" : p.id, p == null ? "" : p.label,
                wind ? normDeg(windFromDeg) : Double.NaN,
                wind ? windMph : Double.NaN, byCallsign, timeMillis,
                BlastRecord.Status.ACTIVE, "", 0, 0, Radii.of(kt, burst),
                null, false, Mil.OFF, Obs.NONE);
    }

    public static boolean validWind(double fromDeg, double mph) {
        return !Double.isNaN(fromDeg) && !Double.isInfinite(fromDeg)
                && fromDeg >= 0 && fromDeg <= 360 && !Double.isNaN(mph)
                && mph >= 0.5 && mph <= 200;
    }

    static double normDeg(double d) {
        double r = d % 360;
        return r < 0 ? r + 360 : r;
    }

    public boolean isKnownModel() {
        return NucModel.MODEL_ID.equals(modelId)
                && NucModel.MODEL_ED.equals(modelEd);
    }

    public boolean isCleared() {
        return status == BlastRecord.Status.CLEARED;
    }

    public boolean hasWind() {
        return validWind(windFromDeg, windMph);
    }

    /** Fallout is drawn for surface and unknown bursts with a known wind. */
    public boolean drawsFallout() {
        return hasWind() && burst != NucModel.Burst.AIR && isKnownModel()
                && !mil.on;
    }

    /** Direction fallout travels (downwind), degrees true. */
    public double downwindDeg() {
        return normDeg(windFromDeg + 180);
    }

    /** Military prediction for this record, or null if none applies. */
    public NucMil.Prediction militaryPrediction() {
        if (burst == NucModel.Burst.AIR || !isKnownModel())
            return null;
        if (!Double.isNaN(mil.edmZoneIKm))
            return NucMil.circular(yieldKt, mil.edmZoneIKm);
        if (!hasWind())
            return null;
        return NucMil.predict(yieldKt, downwindDeg(), windMph * KM_PER_MI,
                mil.angleDeg);
    }

    public static final double KM_PER_MI = 1.609344;

    /** Military picture shown and drawable. */
    public boolean drawsMilitary() {
        return mil.on && militaryPrediction() != null;
    }

    public NucRecord withMil(Mil m) {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, status, clearedBy, clearedMillis, number, radii,
                extra, receivedMismatch, m, obs);
    }

    public NucRecord withObs(Obs o) {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, status, clearedBy, clearedMillis, number, radii,
                extra, receivedMismatch, mil, o);
    }

    /** Apply an EDM line: effective downwind (toward) and speed, or circular. */
    public NucRecord withEdm(NucMil.Edm e) {
        if (e.isCircular())
            return withWind(Double.NaN, Double.NaN).withMil(new Mil(mil.on,
                    Double.NaN, e.zoneIKm, false));
        NucRecord w = withWind(normDeg(e.eddDeg + 180), e.ewsKph / KM_PER_MI);
        return w.withMil(new Mil(mil.on, e.angleDeg, Double.NaN, true));
    }

    public NucRecord withNumber(int n) {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, status, clearedBy, clearedMillis, n, radii, extra,
                receivedMismatch, mil, obs);
    }

    /** Same incident with a new wind (or none: NaN). */
    public NucRecord withWind(double fromDeg, double mph) {
        boolean ok = validWind(fromDeg, mph);
        Mil mil = new Mil(this.mil.on, Double.NaN, Double.NaN, false);
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, ok ? normDeg(fromDeg) : Double.NaN,
                ok ? mph : Double.NaN, byCallsign, timeMillis, status,
                clearedBy, clearedMillis, number, radii, extra,
                receivedMismatch, mil, obs);
    }

    public NucRecord cleared(String by, long when) {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, BlastRecord.Status.CLEARED, by, when, number,
                radii, extra, receivedMismatch, mil, obs);
    }

    public NucRecord reopened() {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, BlastRecord.Status.ACTIVE, "", 0, number, radii,
                extra, receivedMismatch, mil, obs);
    }

    /** "10 kt", "4.5 Mt". */
    public String yieldLabel() {
        return NucPresets.yieldLabel(yieldKt);
    }

    /** Short label for names: "10 kt". */
    public String plainLabel() {
        return yieldLabel();
    }
}
