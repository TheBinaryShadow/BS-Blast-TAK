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
            "Move people out of line of sight of the scene and away from "
                    + "windows (ERG2024 Guide 112).",
            "Watch for secondary devices.",
            "Rings are drawn at the larger of the card's feet and the "
                    + "metres published in ERG2024 p. 367.",
            "This tool doesn't replace professional EOD, bomb squad or "
                    + "department procedures."
    };

    public static final String DISCLAIMER = "Built on the public DHS/DOJ "
            + "Bomb Threat Stand-Off Card (CISA Office for Bombing Prevention "
            + "and FBI Counter-IED Unit). Not endorsed by CISA or the FBI.";
}
