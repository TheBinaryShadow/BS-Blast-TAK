package com.atakmap.android.blast.plugin.core;

/** Fixed caveat and card-guidance text shown in the pane. */
public final class Guidance {

    private Guidance() {
    }

    /** Card guidance, paraphrased. */
    public static final String CARD_ACTIONS = "Don't touch the item. Notify "
            + "authorities / call 911. Keep witnesses available for first "
            + "responders. Use these distances together with your own "
            + "evacuation plan.";

    /** Caveats, always visible under the zone card. */
    public static final String[] CAVEATS = {
            "Minimum distances to prevent life-threatening injury, not "
                    + "guarantees. Other injuries (e.g. hearing damage) can "
                    + "occur farther out.",
            "Use larger distances in open or rural areas: less cover from "
                    + "fragments.",
            "Watch for secondary devices; vary rally points and stay out of "
                    + "line of sight of the item.",
            "Metres are conversions of the card's feet, rounded outward.",
            "This tool doesn't replace professional EOD, bomb squad or "
                    + "department procedures."
    };

    public static final String DISCLAIMER = "Built on the public DHS/DOJ "
            + "Bomb Threat Stand-Off Card (CISA Office for Bombing Prevention "
            + "and FBI Counter-IED Unit). Not endorsed by CISA or the FBI.";
}
