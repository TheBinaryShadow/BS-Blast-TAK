# Blast 1.1: UXO mode, Phase 1 design spec (draft for review)

Status: draft, 2 November 2026. Nothing here is coded yet. The research and verified values behind it are in `claude/blast-uxo-research.md` (with three verification addenda).

## 1. Purpose and principles

**Who it's for.** Prepared citizens first, during a disaster or conflict when nothing is going right; professional responders second, when no other tool is available.

**What it does.** Recognise, keep distance, mark (on the map), report. Nothing about handling, fuzes, render-safe or disposal.

**Principles carried over from Blast 1.0:**
- Every number comes from a public document, with edition and page.
- Golden tests cover every value.
- Provenance travels in the CoT.
- Nothing is pre-selected.
- Rounding is conservative.

**New principles for UXO:**
1. **The quick path takes seconds.** It runs on big buttons and needs no reading before the ring is drawn.
2. **Rings never shrink.** Cover, terrain or "it looks small" change the advice, never the radius.
3. **Every ring says what it is and what it isn't**: "minimum cordon (US doctrine); fragments may travel farther".
4. **Movement advice depends on where the user is.** Backing away is right on a known safe path and wrong inside a minefield.
5. **Transmit after retreating.** The device itself is a radio.
6. **Sharing is deliberate.** Nothing is ever auto-sent (IMAS reprisal caution).

## 2. Sources pinned for 1.1

| ID (CoT `src`) | Document | Edition (CoT `ed`) | Used for |
|---|---|---|---|
| `ATP4-32.2` | ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / AFTTP 3-2.12, *Multi-Service TTP for Explosive Ordnance*, Distribution A | `2024-03-01` | Fig. 31 categories and cordons; 300 m minimum; +50 m for multiple items; 30 m radio; Ch. II "stop, leave by the same path"; Table 2 9-line; 5-Cs |
| `UNMAS-SH3` | UNMAS *Landmines, ERW and IED Safety Handbook*, 3rd ed. (© UN, paraphrase only) | `2015` | MINED drill on foot; vehicle procedure; retracing footsteps "not a safe option"; marking cautions; 50 m radio |
| `IMAS` | IMAS 12.10 Ed.2 Am.3 (2020); TNMA 12.10/01 (2022); TNMA 10.20/01 (2013); IMAS 04.10 Ed.2 Am.12 (2024) | per doc | Safety message wording; reporting/reprisal caution; shaped-charge jet warning; definitions (quoted with "Used and reproduced with permission of the United Nations.") |
| `AFVA32-4022` | USAF UXO Recognition and Reporting Chart (no releasability restrictions) | `2015-12-30` | Recognition classes and size cues for the Reference tab |
| `CISA-BTSOC` | DHS/DOJ Bomb Threat Stand-Off Card (existing) | `2025-08` | Unchanged IED mode |

Not used in 1.1 (planned for later):
- DESR 6055.09 Ed.1 Ch.2 (fragment ring, calculators);
- ERG 2024 Guide 112 (burning cache);
- ATP Fig. 11 (chemical downwind wedge).

## 3. The quick path (screens)

Wireframes are schematic. The pane keeps Blast's current layout: incident list on top, then the selected incident card, then New incident.

### 3.1 New incident: choose the kind of threat

```
┌───────────────────────────────────────────┐
│ NEW INCIDENT                              │
│ What kind of threat?                      │
│ ┌───────────────────┐ ┌─────────────────┐ │
│ │ [bag icon]        │ │ [shell icon]    │ │
│ │ SUSPICIOUS DEVICE │ │ MILITARY        │ │
│ │ bomb threat / IED │ │ ORDNANCE (UXO)  │ │
│ │ (CISA/FBI card)   │ │ shells, bombs,  │ │
│ │                   │ │ mines, rockets  │ │
│ └───────────────────┘ └─────────────────┘ │
│ Not sure? Choose MILITARY ORDNANCE if it  │
│ looks factory-made, otherwise SUSPICIOUS  │
│ DEVICE.                                   │
└───────────────────────────────────────────┘
```

Suspicious Device leads to the existing 1.0 threat picker, unchanged.

### 3.2 UXO, step 1: what do you see?

```
┌───────────────────────────────────────────┐
│ 1. WHAT DO YOU SEE?   (do not go closer)  │
│ [■] Grenade, landmine or bomblet   300 m  │
│ [■] Artillery shell or mortar      375 m  │
│ [■] Rocket or missile              475 m  │
│ [■] Large or guided missile        600 m  │
│ [■] Aircraft bomb or dispenser     675 m  │
│ [■] Pile / cache of ordnance       800 m  │
│ [■] Unknown object                 300 m  │
│     [ It looks large (bomb/missile size) ]│
│ How many?  (•) One   ( ) More than one    │
└───────────────────────────────────────────┘
```

- **Rows** are the six categories of ATP Fig. 31 in its order, plus Unknown. Button text is in plain words; the doctrine name is shown on the card and in the Reference tab.
- **Distances** are shown on the buttons so the user sees the consequence of the choice.
- **Unknown** defaults to the 300 m doctrinal minimum. "It looks large" escalates to 675 m, the largest single-item row (Bombs and Dispensers). *Open question Q2.*
- **More than one** adds 50 m, from ATP Fig. 31: "the CORDON should be extended by 50 meters or more". The card says "+50 m minimum; extend further if items are spread out".
- **Nothing is pre-selected**, and the placement buttons stay grey until a choice is made (as in 1.0).

### 3.3 UXO, step 2: where are you?

```
┌───────────────────────────────────────────┐
│ 2. WHERE ARE YOU?                         │
│ [ On a road or path I know is safe ]      │
│ [ I may be in a minefield / area with     │
│   bomblets ]                              │
│ [ In a vehicle ]                          │
└───────────────────────────────────────────┘
```

This selects the movement guidance on the card (section 5). It doesn't change the ring.

### 3.4 UXO, step 3: place it, without going closer

```
┌───────────────────────────────────────────┐
│ 3. PLACE IT                               │
│ [ Tap map ]  [ Range & bearing from me ]  │
│ [ Map center ]   [ On a marker (radial) ] │
│ ⚠ Never walk closer to get a GPS fix.     │
└───────────────────────────────────────────┘
```

- **"My position" is removed in UXO mode**, because the item is never where the user stands.
- **Range & bearing** has two fields:
  - distance, in m or ft;
  - bearing: magnetic or true (follows the ATAK setting), with a "use compass" button that reads the device heading.

  It computes the point from the user's position with the existing `core/Geo` (WGS84). The estimate is stored as approximate (`plc="rb"`).
- **The radial button on any marker** works as in 1.0.

### 3.5 The incident card (after placement)

```
┌───────────────────────────────────────────┐
│ UXO 3 · Artillery shell or mortar         │
│ CORDON 375 m (1,230 ft)          ● ACTIVE │
│ Minimum cordon, US doctrine (ATP 4-32.2,  │
│ Fig. 31). Fragments may travel farther.   │
├───────────────────────────────────────────┤
│ YOU: 210 m from the item — INSIDE the     │
│ cordon. Move out to at least 375 m.       │
├───────────────────────────────────────────┤
│ DO NOW                                    │
│ 1 Don't touch it or go closer.            │
│ 2 Leave the way you came, on the path you │
│   know is safe.                           │
│ 3 Keep everyone out of the cordon.        │
│ 4 Stay out of line of sight; get behind   │
│   hard cover.                             │
│ 5 Report it once you are outside the      │
│   cordon (not before: radios can set      │
│   some items off).                        │
├───────────────────────────────────────────┤
│ Team: 1 inside cordon · 6 outside         │
│ [ REPORT… ] [ SEND… ] [ Fly to ]          │
│ [ Rename… ] [ Export… ] [ Mark cleared ]  │
│ [ Delete… ]          [ Why this distance?]│
│ Provenance: ATP 4-32.2 (01 Mar 2024),     │
│ Fig. 31 · placed by ALPHA-1 1432Z ·       │
│ position: range & bearing (approx.)       │
└───────────────────────────────────────────┘
```

- **The "YOU" line** reuses 1.0's position check. In UXO mode there is one boundary, so the text is "INSIDE / OUTSIDE the cordon". The poor-GPS warning is kept.
- **Category-specific lines** are added to DO NOW:
  - Rocket or missile and large missile: "Keep to the side. Never stand in line with the nose or tail" (shaped-charge jets, TNMA 10.20/01).
  - Grenade / landmine / bomblet: "Where there is one, expect more."
  - Pile / cache: "If it is burning, move much farther away now". This is the 1.2 fire ring; in 1.1 it's text only.
- **"Why this distance?"** opens the Reference tab at "What the rings mean".

## 4. Rings and geometry (1.1)

- **One cordon ring per UXO incident:** a `DrawingCircle` with UID `<uid>.cordon`, anchored to the marker, like 1.0's rings.
  - Radius is the Fig. 31 value, plus 50 m if "More than one".
  - The value is stored and drawn in metres. The label shows ft converted and rounded up to the next 10 ft.
- **Style:** stroke dark orange `#E65100`, 4 px, with a light orange fill (about 25% opacity). It's distinct from the IED card's red/amber/green so the two modes can't be confused on a shared map. *Open question Q1.* Cleared incidents turn grey, as in 1.0.
- **Marker:** a hostile `a-h-G` point, like 1.0, but named `UXO n <category>` (shared numbering with BLAST n? *Q4*). It uses a distinct UXO icon.
- **Ring label (title):** `UXO CORDON 375 m (1,230 ft) MIN — fragments may go farther`.
- **Unchanged from 1.0:**
  - deleting any part deletes the whole incident;
  - send order is marker first;
  - the incident list, rename, export (KMZ/GPX: one 72-gon ring), clear and delete.

## 5. Guidance text (drafts, plain English)

**Movement (step 2 selects one).**
- **Known safe path:** "Stop. Don't go closer. Leave the way you came, on the path you know is safe, and get at least [R] away." *(ATP Ch. II)*
- **May be in a minefield or bomblet area:** "Stop. Don't move your feet. Call or shout for help and wait. Only move if you can reach a surface you know is safe (paved road, well-used path) without stepping on unknown ground. Don't try to retrace your footsteps unless you've been trained." *(UNMAS 4.1.1, paraphrased)*
- **In a vehicle:** "Stop. Don't reverse or turn the wheel. Stay in the vehicle unless it's on fire, and call for help." *(UNMAS 4.1.2, paraphrased)*

**Always shown (fixed caveats):**
- "This cordon is a minimum. Fragments may travel farther than this distance." *(ATP Table 1 warning)*
- "Cover helps, but doesn't make it safe to be closer."
- "Don't mark the item itself. Mark the area or the way in, from a safe place, and only if you can do it without going closer." *(3Rs; UNMAS 4.1.1.1)*
- "Never move it, touch it or take it as a souvenir."
- "Blast doesn't replace EOD, police or mine action authorities."

**Radio rule:** "Use radios and phones only from outside the cordon. Never within 50 m." This takes the larger of ATP's 30 m and UNMAS's 50 m; the cordon is at least 300 m.

## 6. Report (from the incident card)

```
┌───────────────────────────────────────────┐
│ REPORT                                    │
│ Format: (•) Simple  ( ) 9-line (military) │
│ ── Simple ──                              │
│ WHAT:  Artillery shell or mortar, 1 item, │
│        [description: size, colour…]       │
│ WHERE: 18S UJ 23371 06519 (approx., range │
│        & bearing) · [landmark…]           │
│ WHEN:  021432Z NOV 26                     │
│ MEASURES: cordon 375 m, people kept out   │
│ CALL: 911  (set in Settings)              │
│ [ Copy text ] [ Send as chat… ] [ Close ] │
│ ⚠ In conflict areas, reporting can be     │
│   risky. Choose who receives it.          │
└───────────────────────────────────────────┘
```

- **The 9-line** follows ATP Table 2, pre-filled where Blast knows the answer:
  - Line 1: DTG of discovery.
  - Line 2: grid (MGRS, 8-digit as in the example), plus the reporting callsign.
  - Line 3: contact.
  - Line 4: category, number of items and the user's description, with the note "without touching, disturbing, or approaching".
  - Line 5: CBRN (default "None seen"; user editable).
  - Line 8: protective measures (cordon radius, with "people kept out" ticked by the user).
  - Lines 6, 7 and 9: user choices.

  The doctrinal warning is shown: "MINOR or NO HAZARD does not mean the item is safe."
- **Sending is deliberate:**
  - "Copy text" for phone or SMS;
  - "Send as chat…" opens ATAK's contact picker and sends a GeoChat message;
  - there is no broadcast default.
- **Emergency number:** a setting with default 911. The user can change it to 112, 101/102, or a mine action centre.

## 7. Reference tab (1.1 subset)

A new tab in the pane: **Incidents | Reference**.

1. **Safety rules:** the 3Rs (Recognize, Retreat, Report), the minefield and vehicle drills, the radio rule, marking cautions, and "Where there is one, expect more".
2. **What am I looking at?** The six categories, in Blast's own words based on AFVA 32-4022's classes and size cues (e.g. "bombs and dispensers: 3–6 ft long, fins"). There are no copied images; a link points to the GICHD *Explosive Ordnance Guide for Ukraine* for photos.
3. **What the rings mean:**
   - blast versus fragments;
   - a comparison table for one example (155 mm shell): DoD accidental fragment distance 127 m, doctrinal cordon 375 m, maximum fragment range about 820 m;
   - typical authority cordons for WWII bombs (300 m–1.5 km);
   - why planned demolitions clear people to about 1–1.6 km.
4. **Glossary:** IMAS 04.10 definitions (EO, UXO, ERW, AXO, submunition), with the UN attribution line.
5. **Sources:** every document, edition and page.

The Reference tab is static text compiled into the APK, works offline, and is golden-tested for the numbers it quotes.

## 8. Data model and interoperability

- **The existing `<__blast …>` detail is unchanged for IED incidents.**
- UXO incidents use a **separate detail** that 1.0 clients keep as an opaque detail and ignore:

```xml
<__blast_uxo v="1" src="ATP4-32.2" ed="2024-03-01" fig="31"
  cat="PROJ_MORTAR" lbl="Projectiles and Mortars" cord_m="375"
  multi="0" where="PATH|AREA|VEHICLE" plc="MAP|RB|CENTER|MARKER"
  by="ALPHA-1" t="2026-11-02T14:32:05Z" st="ACTIVE|CLEARED"
  cby="" ct="" n="3"/>
```

  - Category IDs: `GREN_MINE_SUBMUN` 300, `PROJ_MORTAR` 375, `MISSILE_ROCKET` 475, `LARGE_GUIDED_MISSILE` 600, `BOMB_DISPENSER` 675, `STOCKPILE` 800, `UNKNOWN` 300 (or 675 when `large="1"`).
  - As in 1.0, the table in this build is authoritative for known `src`/`ed`. Received radii that don't match are flagged and never drawn. Unknown attributes are kept.
- **Clients without Blast** see a hostile marker, an orange circle, and remarks such as:
  ```
  [BLAST-UXO] Artillery shell or mortar (1 item). Minimum cordon
  375 m / 1,230 ft (ATP 4-32.2 Fig. 31, 01 Mar 2024). Fragments may
  travel farther. Don't approach; keep people out; report to
  authorities. Placed by ALPHA-1 021432Z. Minimum distances, not
  guarantees.
  ```
  Other remarks are kept, using the same block-merge rule as 1.0.
- **1.0 clients receiving a UXO incident** draw nothing extra, delete nothing wrongly, and show the circle and remarks. This must be confirmed in device testing (two devices, one on 1.0).

## 9. Tests

- **Golden (Fig. 31):**
  - every category radius;
  - +50 m for multiple items;
  - 300 m floor;
  - Unknown and large = 675;
  - the ft label rounds up;
  - Table 1 values present for the later calculator.
- **Codec:**
  - round trip;
  - fuzz;
  - tampered `cord_m` is ignored for a known edition;
  - unknown category is rejected;
  - 1.0's `__blast` decoder is unaffected by `__blast_uxo`.
- **Range & bearing:**
  - placed point matches GeographicLib for a set of cases;
  - magnetic/true handling;
  - invalid inputs are rejected.
- **Guidance:**
  - each "where are you" option shows exactly its drill;
  - the radio text is always present;
  - category-specific lines appear only for their categories.
- **Report:**
  - 9-line pre-fill is correct;
  - MGRS format;
  - nothing is sent without an explicit action.
- **Device checklist additions:**
  - placing by range and bearing;
  - two devices with one on 1.0;
  - GeoChat report;
  - Reference tab offline;
  - minified release build.

## 10. Out of scope for 1.1 (planned)

| Version | Features |
|---|---|
| 1.2 | Suspected-chemical overlay (381 m plus 2,000 m downwind wedge ±20°, user wind input); burning-cache ring (ERG 112, 1,600 m); hazard-area polygon for minefields and bomblet areas; responder layer (ICP marker, 5/25 m checks) |
| 1.3 | Optional maximum-fragment-range ring by calibre (DESR V5.E3.T1); calculators (ATP Table 1 by weight, DESR HFD); optional responder mode (5-Cs log); alert when entering an active cordon |
| Never | Handling, fuzing, render-safe or disposal content; copyrighted ID images; automatic sending |

## 11. Open questions for review

- **Q1. Colour:** orange cordon for UXO (distinct from the IED card's red/amber/green), or the same red as IED mandatory evacuation?
- **Q2. "It looks large" for unknown items:** 675 m (largest single-item row, Bombs and Dispensers) or 800 m (the stockpile row, also ERG Guide 112's evacuation figure)?
- **Q3. Mode names:** "Suspicious device" / "Military ordnance (UXO)". Or would you prefer "IED / bomb threat" and "UXO / ERW"?
- **Q4. Numbering:** one shared sequence (BLAST 4, UXO 5) or separate sequences (BLAST 4, UXO 1)?
- **Q5. Emergency number default:** 911, or none until the user sets one?
- **Q6. Chemical overlay:** keep it in 1.2, or pull it into 1.1? It needs a wind-direction input.
