
#import "@preview/polylux:0.4.0": *
#import "formatting.typ": *

#show: userguide.with(
   plugin-name: "Blast",
   plugin-version: "0.1",
   platform: "ATAK",
   platform-version: "5.6.0",
)


#tak-slide[
  = Overview
#toolbox.side-by-side(columns: (.75fr, 9fr))[
#image("plugin_icon.png", width: 70%)
][
Blast draws explosive-threat standoff zones from the public *DHS/DOJ Bomb Threat Stand-Off Card* (CISA Office for Bombing Prevention and FBI Counter-IED Unit, edition "As of Aug 2025"). Pick the threat, tap where the item is, and Blast draws the card's mandatory evacuation, shelter-in-place and preferred evacuation zones. One more tap sends them to the whole team.

The zones are ordinary ATAK circles and a marker, so every TAK client sees them, with or without Blast. Blast works fully offline.
]
= Getting started
- Tap the *Blast* toolbar button.
- *1. Threat:* tap the card category that matches what you see (Pipe Bomb … Semi-Trailer), or *Custom quantity* to enter an estimate in lb or kg.
- *2. Place:* *Tap map* (then tap the map or a marker), *My position*, or *Map center*. From any marker's radial menu, the Blast button opens the pane ready to place a zone on that marker.
- Press *SEND TO ALL* on the zone card, or *Send to…* to choose contacts. Tick *Send new zones to everyone right away* to send automatically.
]

#tak-slide[
  = The zones
- *Red circle: mandatory evacuation.* Everyone inside and outside buildings evacuates and goes to the preferred distance.
- *Amber band: shelter-in-place zone.* Move to the preferred distance. If you can't, shelter inside a building away from windows and exterior walls.
- *Green line: preferred evacuation distance.* Stay at or beyond it.
Each zone also gets a marker named *BLAST n* with the threat. Its remarks carry the full distances and the source, for clients without Blast.

= The zone card
- The three distances in feet, as printed on the card, with converted metres.
- *YOU:* your distance from the device, the zone you are in, and what the card says to do. If your GPS error could put you on the closer side of a boundary, Blast says so: move farther.
- *Team:* how many contacts on the map are in each zone.
- *Provenance:* source, edition, row, your estimate (if any), who placed it and when (Zulu), and who cleared it.
- *Mark cleared / Reopen*, *Delete…* (this device or everyone), *Fly to*.
]

#tak-slide[
  = Custom quantities
Blast never scales the card down. An estimate uses the *next row at or above it*, and the pane says which row it used: an 800 lb estimate uses the SUV/Van row (1,000 lb). Kilograms are converted to pounds first (300 kg = 661.4 lb → SUV/Van).

Above 60,000 lb (the Semi-Trailer row) the card publishes no distance, so Blast draws nothing and says so. Get expert guidance; distances will be greater than the Semi-Trailer row (9,300 ft+).

= Sharing, clearing and deleting
- *SEND TO ALL* sends the marker first and then both rings, so receivers can anchor the rings to it. Moving the marker moves the rings.
- Anyone can *Mark cleared*. Blast records the callsign and Zulu time; the rings turn grey and stay until deleted. Choose *Clear and send to all* to update everyone.
- *Delete for everyone* sends ATAK's remote-delete request for the marker and rings. Clients that are offline keep their copy.
]

#tak-slide[
  = Caveats
- The card's distances are *minimums* to prevent life-threatening injury, not guarantees. Other injuries (for example hearing damage) can occur farther out.
- Use larger distances in open or rural areas: there is less cover from fragments.
- Watch for secondary devices.
- Metres are conversions of the card's feet, rounded outward (never closer to the device).
- Blast doesn't replace professional EOD, bomb squad or department procedures.
- Card guidance: don't touch the item; notify authorities / call 911; keep witnesses available for first responders; use the distances together with your evacuation plan.

= Source
DHS/DOJ Bomb Threat Stand-Off Card, CISA Office for Bombing Prevention and FBI Counter-IED Unit, "As of Aug 2025": #link("https://www.cisa.gov/resources-tools/resources/dhs-doj-bomb-threat-stand-card")[cisa.gov]. Blast is not endorsed by CISA or the FBI. Every overlay carries the source and edition it was drawn from.
]

#tak-slide[
  = Card values (Aug 2025)
#set text(size: 0.85em)
#table(
  columns: (1fr, auto, auto, auto, auto),
  stroke: 0.4pt + luma(180),
  inset: 4pt,
  [*Threat*], [*Explosives capacity*], [*Mandatory evacuation*], [*Shelter-in-place zone*], [*Preferred evacuation*],
  [Pipe Bomb], [5 lb], [70 ft], [71–1,199 ft], [1,200+ ft],
  [Suicide Bomber], [20 lb], [110 ft], [111–1,699 ft], [1,700+ ft],
  [Briefcase/Suitcase], [50 lb], [150 ft], [151–1,849 ft], [1,850+ ft],
  [Car], [500 lb], [320 ft], [321–1,899 ft], [1,900+ ft],
  [SUV/Van], [1,000 lb], [400 ft], [401–2,399 ft], [2,400+ ft],
  [Small Delivery Truck], [4,000 lb], [640 ft], [641–3,799 ft], [3,800+ ft],
  [Container/Water Truck], [10,000 lb], [860 ft], [861–5,099 ft], [5,100+ ft],
  [Semi-Trailer], [60,000 lb], [1,570 ft], [1,571–9,299 ft], [9,300+ ft],
)
]
