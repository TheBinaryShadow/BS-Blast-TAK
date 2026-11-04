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

    NucRecord(int version, String modelId, String modelEd, double yieldKt,
            NucModel.Burst burst, String presetId, String presetLabel,
            double windFromDeg, double windMph, String byCallsign,
            long timeMillis, BlastRecord.Status status, String clearedBy,
            long clearedMillis, int number, Radii radii,
            Map<String, String> extra, boolean receivedMismatch) {
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
                null, false);
    }

    public static boolean validWind(double fromDeg, double mph) {
        return !Double.isNaN(fromDeg) && !Double.isInfinite(fromDeg)
                && fromDeg >= 0 && fromDeg <= 360 && !Double.isNaN(mph)
                && mph >= 1 && mph <= 200;
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
        return hasWind() && burst != NucModel.Burst.AIR && isKnownModel();
    }

    /** Direction fallout travels (downwind), degrees true. */
    public double downwindDeg() {
        return normDeg(windFromDeg + 180);
    }

    public NucRecord withNumber(int n) {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, status, clearedBy, clearedMillis, n, radii, extra,
                receivedMismatch);
    }

    /** Same incident with a new wind (or none: NaN). */
    public NucRecord withWind(double fromDeg, double mph) {
        boolean ok = validWind(fromDeg, mph);
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, ok ? normDeg(fromDeg) : Double.NaN,
                ok ? mph : Double.NaN, byCallsign, timeMillis, status,
                clearedBy, clearedMillis, number, radii, extra,
                receivedMismatch);
    }

    public NucRecord cleared(String by, long when) {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, BlastRecord.Status.CLEARED, by, when, number,
                radii, extra, receivedMismatch);
    }

    public NucRecord reopened() {
        return new NucRecord(version, modelId, modelEd, yieldKt, burst,
                presetId, presetLabel, windFromDeg, windMph, byCallsign,
                timeMillis, BlastRecord.Status.ACTIVE, "", 0, number, radii,
                extra, receivedMismatch);
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
