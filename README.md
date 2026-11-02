Blast


_________________________________________________________________
PURPOSE AND CAPABILITIES

Blast is an ATAK plugin that draws explosive-threat standoff zones from the
public DHS/DOJ Bomb Threat Stand-Off Card (CISA Office for Bombing
Prevention and FBI Counter-IED Unit, edition "As of Aug 2025"):

- Pick the threat (Pipe Bomb ... Semi-Trailer) or enter an estimated
  quantity in lb or kg; an estimate uses the next card row at or above it
  (the card is never scaled down; above 60,000 lb nothing is drawn)
- Place the zone by tapping the map, at your position, at the map center,
  or on any marker from its radial menu
- Draws the card's mandatory evacuation (red), shelter-in-place band
  (amber) and preferred evacuation line (green) as native ATAK circles
  anchored to a hostile BLAST marker, so every TAK client sees them
- Incident list at the top of the pane: Go to and Delete per incident,
  Delete all cleared; deleting any part of an incident removes all of it
- Shows your distance from the device, the zone you are in and what the
  card says to do, warns when GPS error could put you on the closer side
  of a boundary, and counts team contacts in each zone
- Every zone carries its provenance (source, edition, row, estimate, who
  placed it and when) in the marker remarks and in a <__blast> CoT detail
- Send to everyone, send to selected contacts, mark cleared (logged by
  callsign and Zulu time), delete locally or for everyone
- Rename an incident; export it as an ATAK data package, KMZ (Google
  Earth) or GPX (Garmin) to atak/export/Blast/
- Military ordnance (UXO, 1.1+): pick one of the six types of ATP 4-32.2
  (Mar 2024) Fig. 31 (300-800 m cordon, +50 m for more than one item,
  Unknown = 300 m or 675 m), say where you are (known path / possible
  minefield / vehicle) for the right movement drill, place it by map tap
  or range and bearing; one orange cordon, DO NOW list, plain report or
  EOD 9-line saved on the incident; never auto-sent
- Reference tab (offline): safety rules, minefield and vehicle drills,
  radio rule, marking, recognition, what the rings mean, 5-Cs, IMAS terms,
  sources; settable emergency number

Feet are the card's own unit; metres are those published for the same
table in ERG2024 p. 367 (High Explosives, TNT equivalent). Rings are drawn
at the larger of the two.
Blast is fully offline. It does not replace professional EOD, bomb squad
or department procedures.

_________________________________________________________________
STATUS

1.1 (Nov 2026, in testing): UXO mode and Reference tab (see above).
Sources verified: ATP 4-32.2 (01 Mar 2024, Distribution A) Fig. 31 and
Table 1; DESR 6055.09 Ed.1 Ch.2 (Nov 2025); UNMAS Safety Handbook (3rd
ed.); IMAS (as of 1 May 2026). Research notes: docs/UXO-RESEARCH.md.
1.0 (Oct 2026): first public release. Same code as 0.9; see
docs/AUDIT.md for the pre-release audit.
0.9 (Oct 2026): fixes an ATAK crash when opening the radial menu on a
marker (regression in 0.7/0.8: ATAK's radial renderer needs the legacy
WidgetIcon type for the Blast button).
0.8 (Oct 2026): no threat is pre-selected; the user chooses the threat
for every incident (on opening the pane and after each placement).
0.7 (Oct 2026): pre-release audit (see docs/AUDIT.md). Stale cards can no
longer resurrect deleted incidents; people's own remarks are kept; known
editions always draw the table's distances; listener leak fixed; poor-GPS
warning; deprecated ATAK APIs replaced; 61 unit tests incl. fuzzing.
0.6 (Oct 2026): Rename (name = callsign; survives renames by clients
without Blast; number stored in the detail), Export per incident as data
package, KMZ and GPX.
0.5 (Oct 2026): a deleted incident's card now clears from "Selected
incident".
0.4 (Oct 2026): a deleted incident no longer stays selected; incident list
and numbering no longer depend on a single ATAK lookup (own registry + UID
index + group walk, only items still on the map count).
0.3 (Oct 2026): fixes the empty incident list and repeated "BLAST 1"
numbering, the blank radial button icon, and shows far distances in
miles/km.
0.2 (Oct 2026): incident list with Go to / Delete, whole-incident delete,
hostile marker, threat icons, ERG2024 metres, radial button fix.
0.1 (Oct 2026): first test build, CISA/FBI card only.
Planned: ERG2024 reference presets (LPG with explosives p. 368, BLEVE
p. 359, explosives Guides 112/114) as labelled secondary layers; the data
is already in core/ErgData with golden tests.

_________________________________________________________________
POINT OF CONTACTS

S2Underground - s2underground@protonmail.com

_________________________________________________________________
PORTS REQUIRED

None. Blast makes no network connections of its own; zones are shared
through ATAK's normal CoT sending.

_________________________________________________________________
EQUIPMENT REQUIRED

None.

_________________________________________________________________
EQUIPMENT SUPPORTED

Any device running ATAK-CIV 5.6.0 or newer.

_________________________________________________________________
COMPILATION

Standard ATAK plugin build. `./gradlew assembleCivRelease` for release.
For the 5.5.1 developer build of ATAK, build with
`./gradlew assembleCivDebug -PatakVersion=5.5.0`.

Unit tests (`./gradlew testCivDebugUnitTest`) assert every value on the
card, the next-row-up rule, zone boundaries, the CoT detail format and the
text sent to clients without the plugin.

_________________________________________________________________
DEVELOPER NOTES

- core/ is pure Java: StandoffTable (versioned card data), ZoneCalculator
  (row selection), PositionCheck, BlastRecord, BlastCodec (<__blast/>
  detail), Labels, Guidance, Formats.
- atak/: BlastStore (marker + two DrawingCircles with UIDs <marker>.mand
  and <marker>.pref, anchored to the marker), BlastDetailHandler,
  BlastMenuHandler (radial button), Sharing.
- ui/: BlastPane, built in code.
- CoT detail:
  <__blast v="1" src="CISA-BTSOC" ed="2025-08" row="SUV_VAN" lbl="SUV/Van"
   cap_lb="1000" mand_ft="400" sip_ft="2399" pref_ft="2400" req="800"
   req_u="lb" by="CALLSIGN" t="..." st="ACTIVE|CLEARED" cby="" ct=""
   n="2"/>   (n = incident number, 0.6+)
  Unknown attributes are kept and re-sent.
- UXO incidents (1.1+) carry a separate detail that 1.0 clients ignore,
  plus one circle <marker>.cordon:
  <__blast_uxo v="1" src="ATP4-32.2" ed="2024-03-01" fig="31"
   cat="PROJ_MORTAR" lbl="Projectiles and Mortars" cord_m="375" multi="0"
   where="PATH|AREA|VEHICLE" plc="MAP|RB|CENTER|MARKER" by="..." t="..."
   st="ACTIVE|CLEARED" n="1" r_n="" r_desc="" r_land="" r_emp=""
   r_cbrn="" r_res="" r_imp="" r_prot="" r_pri="" r_poc=""/>
  For the known edition the cordon is recomputed from cat/multi/large.

_________________________________________________________________
ACKNOWLEDGMENTS

- DHS/DOJ Bomb Threat Stand-Off Card, CISA Office for Bombing Prevention
  and FBI Counter-IED Unit (public).
- ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / AFTTP 3-2.12, Multi-Service
  TTP for Explosive Ordnance, 01 Mar 2024 (Distribution A).
- DESR 6055.09, DoD Explosives Safety Standards, Ed. 1 Ch. 2 (Nov 2025).
- UNMAS Landmines, ERW and IED Safety Handbook, 3rd ed. (2015),
  paraphrased; IMAS 04.10 definitions quoted: "Used and reproduced with
  permission of the United Nations."
- USAF AFVA 32-4022 UXO Recognition and Reporting Chart (2015); US DoD 3Rs.
- 2024 Emergency Response Guidebook (PHMSA, Transport Canada, SCT,
  CIQUIME), pp. 154-155, 158-159, 359, 367-368.
- Blast is not endorsed by any of these agencies.
- No third-party code or libraries are bundled in the plugin.
