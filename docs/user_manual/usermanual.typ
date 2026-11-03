
#import "@preview/polylux:0.4.0": *
#import "formatting.typ": *

#show: userguide.with(
   plugin-name: "Blast",
   plugin-version: "1.1",
   platform: "ATAK",
   platform-version: "5.6.0",
)


#tak-slide[
  = Overview
#toolbox.side-by-side(columns: (.75fr, 9fr))[
#image("plugin_icon.png", width: 70%)
][
Blast draws explosive-threat standoff zones from the public *DHS/DOJ Bomb Threat Stand-Off Card* (CISA Office for Bombing Prevention and FBI Counter-IED Unit, edition "As of Aug 2025"). The same table, with metric values, is printed in the *2024 Emergency Response Guidebook* (ERG2024) on p. 367. Pick the threat, tap where the item is, and Blast draws the card's mandatory evacuation, shelter-in-place and preferred evacuation zones. One more tap sends them to the whole team.

The zones are ordinary ATAK circles and a marker, so every TAK client sees them, with or without Blast. Blast works fully offline.
]
= Getting started
- Tap the *Blast* toolbar button. *Incidents on the map* is at the top: every incident with its distance, *Go to* and *Delete*.
- *New incident, 1. Threat:* tap the card category that matches what you see (Pipe Bomb … Semi-Trailer), or *Custom quantity* to enter an estimate in lb or kg (TNT equivalent). Nothing is pre-selected: you choose the threat for every incident, and the placement buttons stay greyed out until you do.
- *2. Place:* *Tap map* (then tap the map or a marker), *My position*, or *Map center*. From any marker's radial menu, the Blast button opens the pane ready to place a zone on that marker.
- Press *SEND TO ALL* on the zone card, or *Send to…* to choose contacts. Tick *Send new zones to everyone right away* to send automatically.
]

#tak-slide[
  = The zones
- *Red circle: mandatory evacuation.* Everyone inside and outside buildings evacuates and goes to the preferred distance.
- *Amber band: shelter-in-place zone.* Move to the preferred distance. If you can't, shelter inside a building away from windows and exterior walls.
- *Green line: preferred evacuation distance.* Stay at or beyond it.
Each incident also gets a hostile marker (red diamond) named *BLAST n* with the threat. Its remarks carry the full distances and the source, for clients without Blast.

= The incident card
Tap an incident in the list to open its card:
- The three distances in feet (the card) and metres (as published in ERG2024 p. 367).
- *YOU:* your distance from the device (in miles and km beyond 10,000 ft), the zone you are in, and what the card says to do. If your GPS error could put you on the closer side of a boundary, Blast says so: move farther. If your GPS accuracy is worse than the mandatory radius, Blast says the check is approximate.
- *Team:* how many contacts on the map are in each zone.
- *Provenance:* source, edition, row, your estimate (if any), who placed it and when (Zulu), and who cleared it.
- *Rename…*, *Export…*, *Mark cleared / Reopen*, *Delete…* (this device or everyone), *Fly to*.
]

#tak-slide[
  = Custom quantities
Blast never scales the card down. An estimate uses the *next row at or above it*, and the pane says which row it used: an 800 lb estimate uses the SUV/Van row (1,000 lb). Kilograms are converted to pounds first (300 kg = 661.4 lb → SUV/Van).

Above 60,000 lb (the Semi-Trailer row) the card publishes no distance, so Blast draws nothing and says so. Get expert guidance; distances will be greater than the Semi-Trailer row (9,300 ft+).

= Rename and export
- *Rename…* changes the incident's name, which is the marker's callsign, so every TAK client shows it. New incidents are named *BLAST n threat*; a renamed incident keeps its number for numbering. Choose *Rename and send to all* to update everyone.
- Anyone can also rename the marker in plain ATAK, with or without Blast; Blast keeps that name and only adds or removes *CLEARED*.
- Anyone can add notes to the marker's remarks, with or without Blast. Blast only replaces its own block (from *[BLAST]* to "Minimum distances, not guarantees.") and keeps everything else.
- *Export…* saves the incident to `atak/export/Blast/` as a *data package* (.zip: marker and both rings, for ATAK, WinTAK and iTAK), *KMZ* (Google Earth) and/or *GPX* (Garmin: a waypoint plus the two rings as tracks). In KMZ and GPX the rings are 72-sided outlines drawn just outside the true circle, so they are never smaller than the doctrine distance.

= Sharing, clearing and deleting
- *SEND TO ALL* sends the marker first and then both rings, so receivers can anchor the rings to it. Moving the marker moves the rings.
- Anyone can *Mark cleared*. Blast records the callsign and Zulu time; the rings turn grey and stay until deleted. Choose *Clear and send to all* to update everyone.
- *Delete* (in the list or on the card) removes the whole incident, marker and both rings, after you confirm: *This device only* or *Delete for everyone*. *Delete for everyone* sends ATAK's remote-delete request; clients that are offline keep their copy.
- *Delete all cleared* removes every greyed-out incident at once when the event is over (active incidents are not touched).
- Deleting the marker or either ring with ATAK's own delete also removes the rest of that incident.
]

#tak-slide[
  = Military ordnance (UXO), new in 1.1
For shells, bombs, mines, grenades, rockets and other military ordnance, choose *MILITARY ORDNANCE (UXO)* under New incident. Three steps on one card:
+ *What do you see?* Six types from US multi-service doctrine (ATP 4-32.2, March 2024, Fig. 31), each with its cordon: grenade, landmine or bomblet 300 m; artillery shell or mortar 375 m; rocket or missile 475 m; large or guided missile 600 m; aircraft bomb or dispenser 675 m; pile or cache 800 m. *Unknown* uses the 300 m minimum, or 675 m if it looks bomb or missile sized. *More than one item* adds 50 m to the cordon.
+ *Where are you?* On a known safe path, possibly in a minefield or bomblet area, or in a vehicle. This chooses the movement advice (leave by the same path; stop and don't move your feet; stay in the vehicle).
+ *Place it* without going closer: tap the map, *Range & bearing* from your GPS position (magnetic or true), map center, or a marker's radial menu.

Blast draws a hostile marker *UXO n* and one orange cordon circle. The card shows your distance (inside or outside the cordon), a *Do now* list, the movement drills, team counts and the source. UXO incidents are never sent automatically.

= Reporting
*EOD 9-LINE REPORT…* on the card (or *9-line* in the list) fills in the military EOD 9-line (ATP 4-32.2 Table 2) from the incident: lines 1 and 2 (time and grid) automatically; add a landmark, contact, how it got there (line 4 buttons), number of items, description, signs of chemicals, resources threatened, impact, measures taken and the priority (line 9 buttons). *Save* stores it on the incident (it travels with SEND); *Copy text* puts it on the clipboard for a call, text or chat. In conflict areas, reporting can put people at risk: choose who receives it.
]

#tak-slide[
  = The Reference tab
Offline: what to do if you find something (Recognize, Retreat, Report), the minefield and vehicle drills, radios and phones (only outside the cordon, never within 50 m), marking, what each type looks like, what the rings mean (with DoD fragment-distance comparisons), how responders work (5-Cs, 9-line priorities), IMAS terms and all sources. Set your *emergency number* (911, 112, 101/102, a mine action centre) at the top.

= UXO caveats
- The cordon is a *minimum* for people in the open while the item is left alone. ATP 4-32.2 warns that fragments may travel farther; DoD's maximum fragment ranges are larger (about 820 m for a 155 mm shell).
- Cover helps but never shrinks the cordon. Never touch, move or approach the item, not even for a photo.
- In a possible minefield or bomblet area, stop and wait for help; retracing your footsteps isn't safe unless you've been trained (UNMAS).
- Authorities set the real cordon and may use much larger distances. Blast doesn't replace EOD, police or mine action authorities.
]

#tak-slide[
  = Caveats
- The card's distances are *minimums* to prevent life-threatening injury, not guarantees. Other injuries (for example hearing damage) can occur farther out.
- Use larger distances in open or rural areas: there is less cover from fragments.
- Move people out of line of sight of the scene and away from windows (ERG2024 Guide 112).
- Watch for secondary devices.
- Metres are the values published in ERG2024 p. 367. Rings are drawn at the larger of the feet and metre values (for example the Car mandatory ring at 98 m rather than 320 ft = 97.5 m); the Semi-Trailer mandatory ring uses 1,570 ft (478.5 m) because ERG2024 prints 475 m.
- Blast doesn't replace professional EOD, bomb squad or department procedures.
- Card guidance: don't touch the item; notify authorities / call 911; keep witnesses available for first responders; use the distances together with your evacuation plan.

= Source
DHS/DOJ Bomb Threat Stand-Off Card, CISA Office for Bombing Prevention and FBI Counter-IED Unit, "As of Aug 2025": #link("https://www.cisa.gov/resources-tools/resources/dhs-doj-bomb-threat-stand-card")[cisa.gov]. The same table appears in the 2024 Emergency Response Guidebook (PHMSA, Transport Canada, SCT, CIQUIME), p. 367, headed "High Explosives (TNT Equivalent)". Blast is not endorsed by any of these agencies. Every overlay carries the source and edition it was drawn from.
]

#tak-slide[
  = Card values (Aug 2025; metres from ERG2024 p. 367)
#set text(size: 0.85em)
#table(
  columns: (1fr, auto, auto, auto, auto),
  stroke: 0.4pt + luma(180),
  inset: 4pt,
  [*Threat*], [*Explosives capacity (TNT eq.)*], [*Mandatory evacuation*], [*Shelter-in-place zone*], [*Preferred evacuation*],
  [Pipe Bomb], [5 lb / 2.3 kg], [70 ft / 21 m], [71–1,199 ft / 22–365 m], [1,200+ ft / 366+ m],
  [Suicide Bomber], [20 lb / 9 kg], [110 ft / 34 m], [111–1,699 ft / 35–518 m], [1,700+ ft / 519+ m],
  [Briefcase/Suitcase], [50 lb / 23 kg], [150 ft / 46 m], [151–1,849 ft / 47–563 m], [1,850+ ft / 564+ m],
  [Car], [500 lb / 227 kg], [320 ft / 98 m], [321–1,899 ft / 99–579 m], [1,900+ ft / 580+ m],
  [SUV/Van], [1,000 lb / 454 kg], [400 ft / 122 m], [401–2,399 ft / 123–731 m], [2,400+ ft / 732+ m],
  [Small Delivery Truck], [4,000 lb / 1,814 kg], [640 ft / 195 m], [641–3,799 ft / 196–1,158 m], [3,800+ ft / 1,159+ m],
  [Container/Water Truck], [10,000 lb / 4,536 kg], [860 ft / 263 m], [861–5,099 ft / 264–1,554 m], [5,100+ ft / 1,555+ m],
  [Semi-Trailer], [60,000 lb / 27,216 kg], [1,570 ft / 475 m], [1,571–9,299 ft / 476–2,834 m], [9,300+ ft / 2,835+ m],
)
]
