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
  anchored to a BLAST marker, so every TAK client sees them
- Shows your distance from the device, the zone you are in and what the
  card says to do, warns when GPS error could put you on the closer side
  of a boundary, and counts team contacts in each zone
- Every zone carries its provenance (source, edition, row, estimate, who
  placed it and when) in the marker remarks and in a <__blast> CoT detail
- Send to everyone, send to selected contacts, mark cleared (logged by
  callsign and Zulu time), delete locally or for everyone

Feet are the card's own unit; metres are conversions, rounded outward.
Blast is fully offline. It does not replace professional EOD, bomb squad
or department procedures.

_________________________________________________________________
STATUS

0.1 (Oct 2026): first test build, CISA/FBI card only. Planned: ERG2024
reference presets (IED table, BLEVE, Guides 112/114) as labelled secondary
layers.

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
   req_u="lb" by="CALLSIGN" t="..." st="ACTIVE|CLEARED" cby="" ct=""/>
  Unknown attributes are kept and re-sent.

_________________________________________________________________
ACKNOWLEDGMENTS

- DHS/DOJ Bomb Threat Stand-Off Card, CISA Office for Bombing Prevention
  and FBI Counter-IED Unit (public). Blast is not endorsed by CISA or the
  FBI.
- No third-party code or libraries are bundled in the plugin.
