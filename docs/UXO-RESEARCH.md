# A 300-metre floor anchors Blast's UXO mode

Only one public, current, doctrinal source ties a found ordnance item to a keep-back distance. It is the US multi-service Explosive Ordnance MTTP (**ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / AFTTP 3-2.12**, a 2022-or-later edition that supersedes the 12 March 2020 one). Its Table 1 sets an evacuation distance of **300 m for net explosive weight (NEW) of 27 lb or less, rising to 800 m at 500 lb**. It also sets a **300 m minimum for exposed personnel** around any explosive ordnance, and a rule to **assume NEW is 50% of the item's total weight** ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)). The table has not changed since FM 4-30.51 (2006). It is a US federal work and fits the table-plus-provenance design Blast already uses, so it should be the anchor. Every other number found answers a different question:

- **DoD hazardous-fragment and maximum-fragment distances** are engineering values for planned munitions-response sites.
- **The Cambodian CMAS 10.20 figures** are demolition-planning distances from a draft national standard.
- **The UNMAS handbook figures** are lethal and injury radii, not stand-offs.
- **ERG Guide 112** covers explosives in transport.
- **The UNMAS evacuation poster** is the same IED card logic Blast already draws.
- **European and Japanese WWII-bomb cordons** are one-off decisions by EOD teams, varying tenfold for the same bomb weight.

Public guidance from the US (3Rs), UNMAS, Ukraine, Canada, Lebanon and UK police deliberately gives behaviour rather than metres: don't touch, retreat the way you came, report.

UXO mode should therefore work like this:

- **Recognise** using the public-domain AFVA 32-4022 class scheme.
- **Retreat** to at least 300 m, escalating by estimated weight.
- **Mark** digitally from where the observer stands, never by walking closer.
- **Report** in a 9-line or "what, where, when" format.
- Treat mines and submunitions as area hazards, and treat cover as advice, not a reduction.
- Bundle only US federal material; link to GICHD, UNMAS and CORD.

The most important unknown is the doctrine's own "Cordon Distances by Type" figure (Figure 31), which could not be read and may already be the category table Blast needs.

## ATP 4-32.2 Table 1 is the only doctrinal found-item table

The current EO MTTP is written for all Services' non-EOD forces. It covers reporting and protective measures, not render-safe procedures, and its executive summary says it does not address IEDs ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)). That audience is close to Blast's: someone who has spotted an item and must protect people until specialists arrive. Chapter III, Table 1 (p. 18) lists the evacuation distance against explosive weight. Paragraph 3.a.(2) on p. 19 states that **"the minimum safe distance for exposed personnel is 300 meters from EO or in accordance with table 1"**. The cordon should close the danger area "to all vehicles and foot traffic", sized by METT-T ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)).

| NEW (lb) | ≤27 | 30 | 35 | 40 | 45 | 50 | 100 | 150 | 200 | 250* | 300 | 400 | 500 |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| Evacuation (m) | 300 | 310 | 330 | 350 | 360 | 375 | 475 | 550 | 600 | 625* | 675 | 725 | 800 |
| Evacuation (ft, converted) | 984 | 1,017 | 1,083 | 1,148 | 1,181 | 1,230 | 1,558 | 1,804 | 1,969 | 2,051* | 2,215 | 2,379 | 2,625 |

*The 250 lb row appears in the superseded FM 4-30.51 Figure 3-2 but not in the extracted text of the current table. It may have been lost in extraction ([FM 4-30.51](https://info.publicintelligence.net/USArmy-UXO.pdf)).

Three caveats come with the table:

- It assumes an accurate estimate of the explosive filler.
- Protective barricades can reduce the distances, but no reduction factor is given.
- **"Fragmentation may travel farther than the distance written in this table."**

For items whose filler is unknown, the doctrine says to assume 50% of total ordnance weight is NEW. Its worked example treats a 500 lb bomb as 250 lb NEW and interpolates **637.5 m**. The 2006 manual used the same example and read **625 m** straight off its 250 lb row ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf); [FM 4-30.51](https://info.publicintelligence.net/USArmy-UXO.pdf)). Blast's existing rule for custom quantities is "use the next card row at or above". Applied here it gives 675 m (the 300 lb row). That is more conservative than either published answer, it keeps the plugin's behaviour consistent, and it should be labelled "rounded up to the next table row".

The values are not arbitrary. They match the blast-scaling curve D = 328·W^(1/3) ft (W = NEW in lb), with a 300 m floor, rounded up. For example, 27 lb gives 300 m, 100 lb gives 464 m (table: 475) and 500 lb gives 793 m (table: 800). K328 is the 0.065 psi temporary-threshold-shift (hearing) distance that US munitions-response practice uses for public withdrawal ([Fort Bliss Castner Range ESS](https://home.army.mil/bliss/download_file/view/549/366)). Neither manual states this link, so it is an inference. If correct, it explains the fragment warning: **Table 1 is a blast and hearing criterion with a floor, not a fragment-range table.** For Blast this means the 300 m ring is a doctrinal evacuation distance, not a promise that no fragment lands beyond it.

The same publication supplies three other values in scope:

- **CBRN-suspected item:** a 381 m isolation zone plus a **2,000 m downwind** isolation and evacuation zone (Fig. 11).
- **Radio transmission:** a 30 m (100 ft) minimum for emitters.
- **Marking:** place markers "no closer to the UXO than the point where UXO is first recognized"; mark large concentrations such as submunitions like a minefield, with markers about every 15 m; never approach a dispenser.

Source for all three: [ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf). The 381 m figure is exactly 1,250 ft, which is also the US default hazardous fragment distance for NEW over 450 lb ([Ross, NDIA paper](https://ndia.dtic.mil/wp-content/uploads/2018/intexpsafety/Ross2Paper.pdf)). That suggests a common origin, though no source states it.

The table has two structural limits:

- **It stops at 500 lb NEW.** Under the 50% rule that is roughly a 1,000 lb (about 450 kg) item, so any heavier aircraft bomb, including the common German SC500 and SC1000, is off the table.
- **The 50% rule can underestimate thin-cased bombs.** The Pforzheim bomb found in 2026 weighed about 1,800 kg with **1,350 kg net explosive mass, about 75%** ([Stadt Pforzheim FAQ](https://www.pforzheim.de/stadt/aktuelles/bombenfund-haeufig-gestellte-fragen.html)).

Both limits shape the design recommendations below. The list of figures also names **Figure 31, "Cordon Distances by Type Quick Reference Guide" (p. 59)**, and Figure 36, "Barricade Placement Distance" (p. 63). Their contents are images that text extraction could not capture ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)). Figure 31 is probably the closest thing in current US doctrine to a by-category distance table, and it must be read before Blast codes any category defaults.

## Fragment, demolition and transport distances measure different hazards

The rest of the published distances are real and doctrinal, but each answers a different scenario. They cannot be swapped for Table 1 without changing what the ring means.

**DoD siting and munitions-response distances.** The current DoD standard is **DESR 6055.09, Edition 1, Change 2 (25 November 2025)**, which replaces Change 1 of 23 February 2024 ([DESR 6055.09 Ed.1 Ch.2](https://www.denix.osd.mil/ddes/denix-files/sites/32/2022/08/DESR-6055.09-Edition1-Change-2-251208.pdf)). Its contents locate the relevant tables:

- HD 1.1 hazardous fragment distance (HFD) tables in Volume 3, Enclosure 3 (p. 123).
- **V5.E3.T1, "Default maximum case fragment distances for intentional detonations" by munition diameter** (p. 226).
- Munitions-response minimum safety distances in V7.E6.1 (p. 324).

None of these values could be read, because the fetch stopped at the table of contents. A secondary source confirms the headline rule that **HFD is 1,250 ft for any NEW above 450 lb**. It defines HFD as the public stand-off at which the density of hazardous fragments is "acceptably low". It defines maximum fragment distance (MFD) as the farthest any single fragment is predicted to travel ([Ross, NDIA paper](https://ndia.dtic.mil/wp-content/uploads/2018/intexpsafety/Ross2Paper.pdf)). "Hazardous" means at least **58 ft-lb (79 J)** of impact energy, a threshold the US and NATO share. NATO's AASTP-1 Edition C Version 1 (March 2023) sets the fragment-limited distance at no more than 1 hazardous projectile per 56 m², and has called it the "Debris and Fragment Distance" since 2023 ([van der Voort et al., TNO](https://www.denix.osd.mil/ddes/denix-files/sites/32/2026/04/25IESSE-0029-Comparison-of-79-J-hazardous-fragment_REV1.pdf)). That density is the metric equivalent of the US 1 per 600 sq ft criterion ([Fort Bliss ESS](https://home.army.mil/bliss/download_file/view/549/366)). These are storage and siting standards, not field guides.

US munitions-response documents use these terms consistently:

- **Team separation distance:** about K40, tens of feet. It separates EOD teams from each other and is irrelevant to a bystander.
- **HFD:** governs non-essential personnel for an *unintentional* detonation, with a 200 ft floor.
- **MFD:** governs everyone for an *intentional* detonation.

Sources: [Fort Ord Appendix I ESP](https://docs.fortordcleanup.com/ar_pdfs/AR-ESCA-0124/Draft-Final_Volume_2/Appendices/Appendix_I.pdf); [Becker, USACE M2S2 slides](https://clu-in.org/conf/tio/m2s2fy14-6/slides/M2S2-Explosives-Safety-Becker.pdf); [Fort Bliss ESS](https://home.army.mil/bliss/download_file/view/549/366). Site documents give values for specific items:

| Item (legacy) | HFD | MFD | Source |
|---|---|---|---|
| 40 mm M406 | n/a | 345 ft (105 m) | [Fort Bliss ESS](https://home.army.mil/bliss/download_file/view/549/366) |
| 37 mm Mk II | 200 ft (61 m) | 980 ft (299 m) | [Fort Bliss ESS](https://home.army.mil/bliss/download_file/view/549/366) |
| 75 mm HE M48 | 234 ft (71 m) | 1,701 ft (518 m) | [Fort Bliss ESS](https://home.army.mil/bliss/download_file/view/549/366) |
| 81 mm (four models) | 209–247 ft (64–75 m)† | 1,199–1,579 ft (365–481 m)† | [Becker slides](https://clu-in.org/conf/tio/m2s2fy14-6/slides/M2S2-Explosives-Safety-Becker.pdf) |

†Column headings on the slide were not explicitly mapped to values; the mapping is inferred from the slide's header order.

The comparison is the most useful single result for calibrating Blast. A person waiting for EOD beside an undisturbed item faces the "unintentional detonation" scenario, which DoD governs with HFD. **The 300 m doctrinal floor is roughly four times the HFD for these items, but below the MFD of 75–81 mm rounds.** So the floor is conservative for the scenario it covers. It is not a worst-case fragment envelope, and the UI should say so. A diameter-based MFD ring from DESR V5.E3.T1 would be the natural conservative outer ring, because users can judge calibre by eye far more easily than NEW. The values must be read first. The legacy items also do not transfer to modern munitions of the same calibre; the 81 mm MFDs alone vary by 380 ft across models.

**Humanitarian mine-action distances.** The official IMAS texts (10.10, 10.20, 09.30, 12.10, 04.10 and the technical notes) could not be retrieved, so no IMAS clause or value is cited here. The best available proxy is Cambodia's **CMAS 10.20, Draft Version 5.0**, which itself warns that it "is a working draft and is therefore not a Cambodia Mine Action Standard" ([CMAA CMAS 10.20](https://www.cmaa.gov.kh/assets/cmas/cmas-10-worksite-safety-en.pdf)). It sets:

- **A 25 m minimum "public inner safety distance"** around active demining.
- Default worksite separations of 25–100 m.
- An open-area demolition table by item type:

| Item | Demolition distance |
|---|---|
| AP blast mine | 100 m |
| AP fragmentation, bounding or directional mine | 200 / 500 m |
| AT blast mine | 300 / 1,000 m |
| AT shaped-charge mine | 1,800 m |
| Mortar up to 82 mm | 500 m |
| Shell up to 160 mm | 800 m |
| Shell above 160 mm | 1,000 m |
| Hand or rifle grenade | 300 m |

The table is tempting because it is by category. But it is a draft national standard for *intentional* detonation planning, its paired values were not attributed to conditions, and it does not say whether they apply to staff or the public. It belongs in Blast, if at all, only as a labelled "authorities may cordon to roughly this" reference, never as the default ring.

**UNMAS figures.** The UNMAS *Landmines, ERW and IED Safety Handbook* (3rd edition, 2015) gives danger radii, not stand-offs ([UNMAS Handbook](https://unmas.org/sites/default/files/handbook_english.pdf)):

| Item | Danger radius |
|---|---|
| Stake mine | Lethal within an unobstructed 4 m |
| POMZ-2M | About 10 m |
| Tripwires | Up to 30 m from the mine |
| Directional mine | Effective range over 50 m |
| Bounding mine | Lethal within 25 m, injury to 100 m |
| Fragmentation grenade | Lethal within 10 m, danger area 50 m or more |

The handbook also notes that bombs can penetrate up to 35 m into the ground. These figures help explain *why* the rings are large; they do not define them.

**UNMAS evacuation poster.** The poster's threat rows (pipe bomb 5 lb up to semitrailer 60,000 lb) and "behind wall" versus "in the open" columns reproduce IED card logic. Its extracted values (for example 5,100 ft and 9,300 ft) match the largest rows of the stand-off card Blast already draws, so it adds nothing for UXO ([UNMAS poster](https://unmas.org/sites/default/files/documents/evacuation_distance_guide_poster.pdf)).

**ERG 2024.** Guide 112 (Division 1.1/1.2/1.3/1.5) says isolate **500 m**, consider **800 m** evacuation for a large spill, and **1,600 m** when a rail car or trailer is on fire. Guide 114 (Division 1.4/1.6) gives 100, 250 and 800 m respectively ([ERG 2024 Guides 112/114](https://www.quantico.marines.mil/Portals/147/Offices_and_Staff/G3_Ops/Ammo_Supply_Point/Other_Pubs/2024_Emergency_Response_Guides_%28ERG%29_112_and_114.pdf)). No US or NATO authority was found recommending the ERG for found military ordnance. Its 1,600 m fire value is still the most defensible public figure for a *burning cache or vehicle load* of munitions, a case Table 1 does not cover.

## Authorities set WWII-bomb cordons case by case, and they disagree tenfold

No national authority found publishes a binding public table for legacy bombs. In Germany the state ordnance-disposal service (KMBD/KMRD) decides the radius and the city enforces it. Essen's official FAQ says: "Die abschließende Entscheidung über den tatsächlichen Evakuierungsradius hat der Kampfmittelbeseitigungsdienst" ("the ordnance-disposal service makes the final decision on the actual evacuation radius"). Its rules of thumb are **300 m for a "Fünf-Zentner" bomb and 600 m for a "Zehn-Zentner" bomb**. Since a Zentner is 50 kg, those mean about 250 kg and 500 kg; the automated summary rendered them as 500 kg and 1,000 kg, which needs a direct check. Essen also dropped its two-zone model (an evacuation circle plus an outer stay-indoors circle) in February 2024 ([Stadt Essen](https://www.essen.de/essenaktuell/bombenfund_faq.de.html)). Practice elsewhere:

| Item | Cordon | Place, date | Source |
|---|---|---|---|
| ~50 kg (damaged) | ~150 m | Stuttgart, Jul 2026 | [Stadt Stuttgart](https://www.stuttgart.de/it/pressemitteilungen/2026/juli/50-kilo-weltkriegsbombe-auf-baustelle-in-bad-cannstatt-entschaerft-7091104) |
| 250 kg | ~300 m | Plattling station, Sep 2026 | [diebayern.de](https://www.diebayern.de/wirtschaft/250-kilo-fliegerbombe-in-plattling-bahnhof-gesperrt-300-meter-evakuierung-3181462) |
| 250 kg | 500 m | Berlin-Gesundbrunnen, Dec 2021 | [Bezirksamt Mitte](https://www.berlin.de/ba-mitte/aktuelles/artikel.1155160.php) |
| ~250 kg (551 lb) | 283 m ground; airspace to ~780 m | Naha, Okinawa, Sep 2024 | [Stars and Stripes](https://stripes.com/theaters/asia_pacific/2024-08-20/okinawa-uxo-ww2-evacuation-14920138.html) |
| 500 kg SC500 | ~300 m | Plymouth, Feb 2024 | [GOV.UK](https://www.gov.uk/government/news/unexploded-bomb-in-plymouth-safely-removed-during-complex-disposal-operation-and-major-evacuation) |
| ~495 kg | 591 m red zone + 755 m shelter belt | Rovereto, Jan 2026 | [il Dolomiti](https://www.ildolomiti.it/cronaca/2026/scatta-la-zona-rossa-a-rovereto-per-loperazione-bomba-21-varchi-attivi-allalba-evacuati-oltre-6mila-residenti-e-droni-dei-vigili-del-fuoco-in-volo-per-la-sicurezza) |
| 1,000 kg "Hermann" | 100 m, extended to 400 m | Exeter, Feb 2021 | [LBC](https://www.lbc.co.uk/article/2-000-homes-evacuated-unexploded-world-war-ii-bomb-discovered-DWyrFS_2/) |
| Suspected bomb | up to 1,000 m | Gießen, Oct 2026 | [Gießener Anzeiger](https://www.giessener-anzeiger.de/stadt-giessen/weltkriegsbombe-in-giessen-bis-zu-1000-meter-umkreis-koennten-evakuiert-werden-94539572.html) |
| ~1,800 kg | ~1.5 km | Pforzheim, May 2026 | [Stadt Pforzheim](https://www.pforzheim.de/stadt/aktuelles/bombenfund-haeufig-gestellte-fragen.html) |

For bombs of about 250 kg the spread runs from **100 m (Nova Gorica, 2023) to 1 km (Vurberk, 2017)** ([Wikipedia aggregator](https://en.wikipedia.org/wiki/World_War_II_bomb_disposal_in_Europe)). At Exeter, houses within 100 m were damaged after the controlled detonation even with mitigation in place (same source). Officials cite the same few drivers each time:

- **Weight** (Essen).
- **Uncertainty about the item.** The Dutch EOD planned for a 1,000 lb bomb when 500 lb was likely ([NOS](https://nos.nl/l/2522430)).
- **Degraded condition.** A degraded fuze or payload drove the Plymouth assessment ([GOV.UK](https://www.gov.uk/government/news/unexploded-bomb-in-plymouth-safely-removed-during-complex-disposal-operation-and-major-evacuation)).
- **Mitigation:** sand mounds, walls and earth-filled containers.

Two lessons follow. First, **Table 1 is more conservative than most authority cordons for mid-size bombs.** A 250 kg bomb has about 125 kg (276 lb) NEW by the 50% rule, which falls on the 300 lb row: 675 m, against the 283–500 m that authorities typically imposed. That is appropriate. Authority cordons are drawn for controlled operations, often around buried items and with mitigation in place; Blast's user has neither. Second, **above the table, authority practice climbs toward 1–1.5 km**, and the doctrine gives no number. Extending the K328 curve that Table 1 appears to follow gives these values (my derivation, not doctrine):

| Bomb (NEW assumption) | NEW | Extrapolated distance | Authority practice |
|---|---|---|---|
| 1,000 kg, 50% rule | ~1,100 lb | ~1,030 m | Exeter 400 m; Gießen up to 1,000 m |
| Pforzheim, stated 1,350 kg NEM | ~2,980 lb | ~1,440 m | ~1.5 km actual |

The match is close enough to make the extrapolation a plausible option. It is not a published rule, so it needs explicit sign-off before Blast uses it. Okinawa's airspace restriction, about 780 m above a 283 m ground radius, is also a reminder for TAK teams that often fly small drones: the hazard extends upward as well as outward ([Stars and Stripes](https://stripes.com/theaters/asia_pacific/2024-08-20/okinawa-uxo-ww2-evacuation-14920138.html)).

## Public guidance converges on behaviour, not metres

Public-facing sources deliberately avoid numbers.

- **US DoD 3Rs (Recognize, Retreat, Report).** "Do not touch, move or disturb it, but immediately and carefully – do not run – leave the area." Leave by the same path, mark the general area rather than the munition (for example with clothing), and "call 911" ([DENIX UXO Explosives Safety](https://denix.osd.mil/uxo/explosives-safety); [DENIX FAQs](https://www.denix.osd.mil/uxo/explosives-safety/faqs/)). The outdoor-recreation guide adds: retrace your steps, "if possible, get a fix on your position", and at sea call the Coast Guard on Channel 16 ([3Rs Outdoor Recreation Guide](https://www.denix.osd.mil/uxo/denix-files/sites/72/2023/02/3Rs_Guide_Outdoor_Recreation-2022.11.10.pdf)).
- **Canada DND.** "Go back the way that you came"; in a vehicle, stay inside and reverse away; no distance given ([Canada DND](https://www.canada.ca/en/department-national-defence/services/uxo/uxo-safety-tips.html)).
- **UNMAS handbook.** "You cannot outrun the fuse of a mine/UXO or the blast wave", "If you are in any doubt, turn back!", and **"Do not attempt marking if you have not received proper training"** ([UNMAS Handbook](https://unmas.org/sites/default/files/handbook_english.pdf)).
- **Ukraine, National Police (2023).** Call 102 or DSNS on 101. Do not approach, touch or move the item, keep others away, stop all work, and do not use radios or mobile phones. Wait for specialists and give the location and time of the find ([KMU / National Police](https://www.kmu.gov.ua/news/natsionalna-politsiia-ukrainy-nahaduie-pravyla-bezpeky-iaki-dopomozhut-vriatuvaty-zhyttia)).
- **Ukraine, DSNS.** "Only EOD technicians can tell whether a munition is still dangerous" ([ArmyInform](https://armyinform.com.ua/2023/03/22/vybuhonebezpechni-predmety-yak-zahystyty-sebe-ta-svoyih-blyzkyh-vid-nebezpeky/)).
- **Lebanon, NMAS 12.10 (Edition 3, 2023).** The core message is "recognize a potential threat, don't approach it, don't touch it, report it immediately" ([LMAC NMAS 12.10](https://lebmac.org/wp-content/uploads/2025/01/LEBANON_NMAS_12.10_Explosive_Ordnance_Risk_Education_E3.pdf)).
- **Lebanese Army.** "the absence of clues or signs does not mean the absence of dangerous ordnances" ([Lebanese Army](https://www.lebarmy.gov.lb/en/content/generalization-about-awareness-dangers-mines-cluster-bombs-and-suspicious-ordnances)).
- **Devon and Cornwall Police.** Call 999 or 101 and describe the item without handling it ([Devon & Cornwall Police](https://firearms.devon-cornwall.police.uk/explosives/lost-and-found/)).

The only public number found is a Kharkiv-oblast civil-protection pamphlet hosted by Ukraine's Pension Fund. For an anti-personnel mine it says to withdraw "not less than 100 metres", retracing your own steps, and to stay behind cover until police or DSNS arrive ([PFU pamphlet](https://www.pfu.gov.ua/content/uploads/sites/24/2024/09/Pamyatka-vybuhonebezpechni-predmety.pdf)). That is regional, not national DSNS guidance, and it conflicts with the US 300 m floor. Because the public sources withhold numbers, **any ring Blast draws goes beyond public guidance and must cite its doctrinal source.** A UI localised for Ukraine must not imply that the ring comes from Ukrainian government guidance.

**Radio stand-off.** Published values disagree:

- 30 m (100 ft) in the current MTTP ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)).
- 50 m in the UNMAS handbook ([UNMAS Handbook](https://unmas.org/sites/default/files/handbook_english.pdf)).
- 100 m in the superseded FM 4-30.51 ([FM 4-30.51](https://info.publicintelligence.net/USArmy-UXO.pdf)).
- No distance at all, simply "don't use", from the Ukrainian police.

This matters more for Blast than for a paper card, because the ATAK device is itself a transmitter. The conflict resolves through sequence rather than a choice of number. If the flow says **retreat to the ring first, then mark and transmit**, the user is at 300 m or more, beyond every published radio stand-off, before sending anything.

**Mines and submunitions are area hazards.** FM 4-30.51 says to consider all mines booby-trapped or fitted with anti-disturbance devices, that submunitions may be widely scattered, and that small size does not reduce the danger ([FM 4-30.51](https://info.publicintelligence.net/USArmy-UXO.pdf)). The current MTTP marks submunition concentrations like a minefield, with perimeter markers about every 15 m ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)). UNMAS warns that tripwires run up to 30 m from a mine and bounding mines injure to 100 m ([UNMAS Handbook](https://unmas.org/sites/default/files/handbook_english.pdf)). No submunition-specific stand-off was found in any CCM, GICHD, HALO, NPA or Lao or Cambodian source reached. The behavioural rule is the same across sources: stop, don't advance, leave by the known route, and assume more items nearby.

**Marking and reporting.** Three sources point the same way. Doctrine says to mark no closer than the point of first recognition ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)). UNMAS discourages untrained physical marking. The 3Rs say to mark the area, not the item. In ATAK terms, the item's point should be placed by map tap or range-and-bearing from the observer's own position, never by walking closer to get a GPS fix. The MTTP's EOD 9-line (Ch. IV, Table 2) is a ready reporting template:

1. Date-time group.
2. Reporting unit and 8-digit grid.
3. Contact.
4. Type of EO.
5. CBRN contamination.
6. Resources threatened.
7. Impact on mission.
8. Protective measures taken.
9. Recommended priority.

It warns that "a priority of MINOR or NO HAZARD does not indicate the EO is safe" ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)). Civilian responders need only the Ukrainian police's "what, where, when".

**Recognition aids.** The current recognition aids converge on about nine families:

- **AFVA 32-4022** (30 December 2015, certified current 25 June 2024) uses six classes: A, Bombs and Dispensers (3–6 ft long); B, Rockets and Missiles (5–8 ft); C, Projectiles and Mortars; D, Landmines; E, Bomblets; F, Grenades. It also gives the "5 C's": Confirm, Clear, Cordon, Check, Control ([AFVA 32-4022](https://static.e-publishing.af.mil/production/1/af_a4/publication/afva32-4022/afva32-4022.pdf)).
- **The GICHD Explosive Ordnance Guide for Ukraine, 3rd edition (2025)** adds modern categories such as Loitering Munitions and Improvised Drone Munitions ([GICHD 3rd ed.](https://www.gichd.org/fileadmin/user_upload/Explosive_Ordnance_Guide_for_Ukraine__GICHD___Third_Edition.pdf)).

**Existing apps.** Civilian apps combine a catalogue, behaviour rules, a geotagged report and hazard maps:

- SESU's Demining Ukraine has a danger-zone map with proximity alerts ([MVS Ukraine](https://mvs.gov.ua/en/news/mobilnii-zastosunok-rozminuvannya-ukrayini-vasa-bezpeka-u-vasomu-smartfoni)).
- MineFree offers reporting and threat-area alerts ([Counter-IED Report](https://counteriedreport.com/ukraine-minefree-mine-safety-mobile-application-launched/)).
- The UNMAS app (about 2013) had training and photo-plus-GPS reports ([GICHD tools page](https://gichd.org/publications-resources/tools-and-applications/unmas-landmine-erw-safety-app)).

None of these advertises category keep-back distances. Only EOD Toolkit, which is aimed at EOD professionals, does ([App Store: EOD Toolkit](https://apps.apple.com/app/eod-toolkit/id6751351736)). No public ATAK plugin for civilian or responder UXO recognition and reporting surfaced. TAK.gov's catalogue, which requires login, was not searched.

## Licences sort sources into bundle, cite and link-only

US federal works carry no copyright: "Copyright protection under this title is not available for any work of the United States Government" ([17 U.S.C. §105](https://www.govinfo.gov/content/pkg/USCODE-2020-title17/html/USCODE-2020-title17-chap1-sec105.htm)). That makes the US doctrinal sources the only ones Blast can embed freely, subject to checking third-party images and distribution statements item by item.

| Source | Terms found | Blast use |
|---|---|---|
| ATP 4-32.2 EO MTTP | US federal work; posted publicly on marines.mil; no formal distribution statement seen in the extracted text ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)) | Bundle values with citation once the distribution statement is confirmed |
| AFVA 32-4022 | "There are no releasability restrictions on this publication" ([AFVA 32-4022](https://static.e-publishing.af.mil/production/1/af_a4/publication/afva32-4022/afva32-4022.pdf)) | Bundle class scheme, size cues and chart; credit, no implied endorsement |
| DoD 3Rs (DENIX) | Federal; no explicit reuse statement; logos and branding guides offered ([DENIX](https://denix.osd.mil/uxo/explosives-safety)) | Use wording; avoid logos unless branding guide permits |
| ERG 2024 | Federal (already used by Blast) ([ERG 112/114](https://www.quantico.marines.mil/Portals/147/Offices_and_Staff/G3_Ops/Ammo_Supply_Point/Other_Pubs/2024_Emergency_Response_Guides_%28ERG%29_112_and_114.pdf)) | Bundle values |
| DoD imagery | Public domain where marked; DoD visual-information limits on insignia, identifiable people and endorsement ([DMA limitations](https://www.dma.mil/Services/Visual-Information/References/Limitations/)) | Bundle per image, with credit |
| Iraq/Afghanistan Ordnance ID Guides (NAVEODTECHDIV, 2004) | No copyright or distribution statement; likely §105 but may contain third-party images ([JMU CISR](https://www.jmu.edu/cisr/research/iraq-oig/index.shtml)) | Verify per image before bundling |
| UNMAS handbook (2015) | "© United Nations 2015… All rights reserved" ([UNMAS Handbook](https://unmas.org/sites/default/files/handbook_english.pdf)) | Paraphrase principles, cite figures, link |
| GICHD Ukraine guide (3rd ed.) | © GICHD; site terms allow only non-commercial, unmodified copies credited "Reproduced with permission of GICHD"; many images are third-party ([GICHD Terms](https://www.gichd.org/terms-of-use/)) | Link only, unless written permission |
| CORD (ex-ORDATA) | Login-gated; "protected by United States copyright law"; no reproduction, no commercial use, scraping "subject to legal action" ([CORD Disclaimer](https://www.ordata.info/disclaimer)) | "See also, for trained users" mention only |
| LMAC NMAS 12.10 | "must not be copied or altered without the written consent of the LMAC" ([LMAC](https://lebmac.org/wp-content/uploads/2025/01/LEBANON_NMAS_12.10_Explosive_Ordnance_Risk_Education_E3.pdf)) | Cite; use reporting numbers as facts |
| ARES ARCS, OSMP, CAT-UXO | All rights reserved / fair-use reposts / no licence ([ARES](https://armamentresearch.com/wp-content/uploads/2022/08/The-ARES-Arms-Munitions-Classification-System-ARCS-ver1.3-public-release.pdf); [OSMP](https://osmp.ngo/about); [CAT-UXO](https://mwm.ai/apps/cat-uxo/6720751417)) | Do not bundle |
| Ukrainian government leaflets | 1993 law excluded official documents; status under the 2022 law and for photos unconfirmed ([Commons: Ukraine](https://c.enwp.org/wiki/Commons:CRT/Ukraine)) | Link only |

Blast-TAK is published openly on GitHub, so anything inside the repository is effectively redistributed to everyone downstream. The GICHD "non-commercial, unmodified" and UNMAS "all rights reserved" terms cannot coexist with that. Numeric facts with attribution are lower-risk than reproduced text or images, but this is not legal advice.

## Recommended phased design

**Anchor on ATP 4-32.2 Table 1 and its 300 m minimum, with no alternative default.** It is current, multi-service and written for non-EOD personnel. It is stable across 2006 and 2022+ editions, quantity-based like Blast's existing card engine, and a federal work. Store it as a new `StandoffTable` with its own source and edition (for example `src="ATP4-32.2-T1"` plus the confirmed edition date), with golden tests asserting every row. Keep it a separate mode from the IED card, so the two sources' rings never mix in one incident. The CoT detail and remarks fallback should state the basis ("Evacuation distance, unprotected personnel, ATP 4-32.2 Table 1; fragments may travel farther") so that clients without the plugin see the meaning, not just the circle.

**Phase 1: recognise, retreat, mark, report at the doctrinal floor.** Build this first.

- **Category picker:** the AFVA A–F classes plus two additions, "Drone-dropped / loitering munition" and "Unknown / other / out of place". As with the card threats, nothing is pre-selected.
- **Rings:** every category draws at least the 300 m ring. Bombs, dispensers, rockets and missiles prompt for an estimated item weight, apply the 50% rule and take the next Table 1 row at or above.
- **Beyond the table:** above 500 lb NEW, draw the 800 m row with a persistent "exceeds doctrinal table; authority cordon is likely larger" label, rather than drawing nothing as the card mode does above 60,000 lb.
- **Unknown items:** default to 300 m with a one-tap "item looks bomb- or missile-sized" escalation to 800 m, which also matches the ERG Guide 112 evacuation figure. That follows doctrine's "do not approach beyond first observation" and the Dutch EOD's "plan for the larger plausible item".
- **Mines and submunitions:** draw a point plus a "suspected hazard area" polygon tool, and show the 300 m ring for people outside it. Prompt "leave by your known route; assume more items". The 15 m figure is military perimeter-marker spacing, not a distance, so show it only as a note.
- **Placement:** default to map tap or range-and-bearing from self, with a warning against approaching to get a fix.
- **Behaviour card:** don't touch, don't run, retreat the way you came, keep others out.
- **Transmit after retreat:** a banner reading "transmit only after reaching the ring" settles the radio conflict.
- **Report builder:** prefill a 9-line from the marker (DTG, grid, category, protective measures) and include a plain "what, where, when" variant.
- **Emergency number:** a user-set field (911, 101/102, 999, or a mine-action centre), not hard-coded per country.
- **Cover:** a checkbox that changes advice text only ("stay behind hard cover and out of line of sight; distances are not reduced"). Doctrine allows reduction behind barricades but publishes no factor ([ATP 4-32.2](https://www.marines.mil/Portals/1/Publications/MCRP%2010-10D.1%20%28SECURED%29.pdf)), so the radius must not shrink automatically.

**Phase 2: modifiers from the same doctrine.**

- **CBRN-suspected:** the 381 m isolation ring plus a 2,000 m downwind sector, sharing wind input with HAZ-TAK's planned plume work.
- **Cache or vehicle load on fire:** the ERG Guide 112 1,600 m ring, labelled as a transport-incident value.
- **Two-ring display:** "doctrinal evacuation" plus an optional "typical authority cordons for this class" note with example cases, never a ring that implies safety. Italy's red and yellow zones show that two-zone presentation is in use, though Essen abandoned it.
- **Figure 31:** if it proves public and readable, it replaces Blast-built category defaults wherever it differs.

**Phase 3: fragment and recognition depth, gated on verification.**

- **Diameter ring:** an optional outer "maximum fragment distance by diameter" ring from DESR V5.E3.T1, once the table and its distribution are confirmed. The calibre picker would ask "about how wide?" instead of asking for NEW.
- **Recognition chart:** bundle the AFVA chart image.
- **Links:** link out to the GICHD guide and the UNMAS handbook.
- **Proximity alert:** an alert when approaching existing UXO markers, matching the civilian apps' strongest feature.
- **Above-table extrapolation:** a K328 extension for bombs beyond 500 lb NEW, only with explicit sign-off and an "extrapolated, not doctrinal" label.

**What not to build.** Do not use the UNMAS lethal radii, the CMAS demolition table or national incident cordons as default rings. Do not embed GICHD, CORD, UNMAS or NGO text or images. Do not give anything about fuzing, handling or disposal; the content stops at the 9-line.

## Open items to verify before any value is coded

| # | Item | Why it matters | How to resolve |
|---|---|---|---|
| 1 | ATP 4-32.2 **Figure 31 "Cordon Distances by Type"** (p. 59) and Figure 36 (p. 63) | Possibly the doctrinal category table; could override Blast-built defaults | Download the PDF and read the page images directly |
| 2 | ATP 4-32.2 exact **edition date** and **distribution statement** | Needed for the `ed` attribute and to confirm public release | Cover page; armypubs or Marine Corps publications listing |
| 3 | Whether the current Table 1 has a **250 lb row** | Affects golden tests and the 625 vs 637.5 m discrepancy | Read p. 18 of the PDF directly |
| 4 | **DESR 6055.09 Ed.1 Ch.2** V3.E3.T2, V5.E3.T1/T2, V7.E6.1 values and the 200 ft minimum HFD | Basis for any fragment ring and "unknown item by diameter" | Obtain the PDF outside the proxy |
| 5 | **IMAS** 10.10, 10.20, 09.30, 12.10, 04.10 and the explosion-danger-area technical note: editions, clauses, values and copyright | Humanitarian users will expect IMAS alignment; nothing IMAS is cited yet | mineactionstandards.org (allowlist it) or user download |
| 6 | **UNMAS handbook Section 4 and Annex A5** (evacuation distance guide, nine-liner) | Unread; may contain UXO distances or only the IED chart | OCR pages after about p. 72 |
| 7 | **UNMAS poster** row/column pairings | Extracted values were scrambled | View the PDF visually |
| 8 | **CMAS 10.20** Fig. 2 paired values and audience | Unclear whether the figures are for staff or the public, or which condition each applies to | Read the PDF directly; check for a final (non-draft) version |
| 9 | **Essen Zentner** reading (250/500 kg vs 500/1,000 kg) | Changes the authority-practice comparison | Read the German page directly |
| 10 | **USACE 81 mm slide** column mapping (K40/HFD/MFD) | Fragment comparison relies on it | Read slide 11 directly |
| 11 | **DoD 3Rs** reuse terms and logo/branding rules; 3Rs.mil radio guidance | Wording reuse and any radio distance | 3Rs.mil and DENIX branding guide |
| 12 | **AFVA 32-4022** image credits | Bundling the chart image | Inspect the chart's credits |
| 13 | **Radio stand-off** in current doctrine (30 m) vs UNMAS (50 m) vs FM 4-30.51 (100 m) | Banner wording | Confirm ATP p. 10–11 text; read the UNMAS original |
| 14 | Unread leads: FM 3-100.38 (DTIC ADA458030), GTA 90-10-046 UXO card, JSP 403 Vol 5 Ch 2, CIRIA C681, official DSNS guidance, Lebanon NMAS 10.10/10.20, UNSCO observer lesson (Jul 2026) ([UNSCO](https://unsco.unmissions.org/sites/default/files/2026-07/UN%20Military%20Observers%20-%20Module%203%20-%20Lesson%203.7%20Mines%2C%20Explosive%20Remnants%20of%20War%2C%20Improvised%20Explosive%20Devices%20and%20Crater%20Analysis.pdf)) | Any could supply a by-category public distance table | Targeted retrieval |
| 15 | **K328 interpretation** of Table 1 and the 381 m = 1,250 ft link | Both are my inferences; they justify the fragment caveat and any extrapolation | Check the derivation in DESR or ATP rationale text, or with an EOD reviewer |
| 16 | **Pforzheim 1,350 kg NEM** figure and current GICHD/UNMAS app availability | Supports the 50% rule caveat; affects link targets | Re-read the sources directly |

## Conclusion

The research shows that the gap Blast would fill is a translation layer, not missing data. Doctrine already holds a defensible keep-back rule (300 m, scaled by weight). Public guidance already holds the right behaviour (retreat the way you came, report). But public guidance withholds numbers because it cannot assume the reader understands what a ring does and does not promise. Blast's existing design (provenance in every ring, golden-tested tables, conservative rounding, no pre-selected threat) already handles that risk. The new risk is semantic: a 300 m circle on a map looks like a safe line, when doctrine itself says fragments may go farther, and when authorities for the largest legacy bombs routinely clear one to one and a half kilometres. So the UXO mode's main job is to label rather than calculate: every ring names its scenario, its source and its known blind spot. Designed that way, the mode stays a guideline tool, keeps the person at the doctrinal distance and beyond every published radio stand-off before they transmit, and leaves cordons and disposal to the authorities.

## Verification addendum (2 November 2026): primary documents read

The user supplied three primary documents. They were read directly (text and page images), so the values below replace the corresponding unverified statements above.

| Document | Edition | Release | SHA-256 (prefix) |
|---|---|---|---|
| ATP 4-32.2 / MCRP 10-10D.1 / NTTP 3-02.4.1 / AFTTP 3-2.12, *Multi-Service TTP for Explosive Ordnance* | **01 March 2024** (supersedes 12 March 2020) | **Distribution Statement A: approved for public release; distribution is unlimited** (cover) | 450f8b4b… |
| DESR 6055.09, *DoD Explosives Safety Standards* | Edition 1, **Change 1, 23 February 2024** | "Cleared for public release" (cover) | 4205ea14… |
| UNMAS *Landmines, ERW and IED Safety Handbook* | 3rd edition, 2015 | "© United Nations 2015 … All rights reserved" (p. ii) | 68ff6a97… |

DESR Change 2 (25 November 2025) supersedes Change 1. The values below come from Change 1 and must be re-checked in Change 2 before coding.

### Open items now closed

**Item 1: ATP 4-32.2 Figure 31 is the doctrinal category table.** "Quick Reference Guide: Explosive Ordnance (EO) and Unexploded Explosive Ordnance (UXO) Cordon Distances by Type", Figure 31, printed page 58 (PDF page 70; the earlier "p. 59" was wrong):

| Type of EO/UXO (figure's wording) | Average weight of EO/UXO | Cordon distance |
|---|---|---|
| Grenades, Landmines, Submunitions | 27 lb or less | **300 m** |
| Projectiles and Mortars | 50 lb or less | **375 m** |
| Missiles and Rockets | 100 lb or less | **475 m** |
| Large and Guided Missiles | 200 lb or less | **600 m** |
| Bombs and Dispensers | 300 lb or less | **675 m** |
| Stockpiles of EO or UXO | 500 lb or less | **800 m** |

The figure's own rules (quoted):
- "These distances are for SINGLE EO/UXO. When more than one EO/UXO is found the CORDON should be extended by 50 meters or more."
- "Minimum safe distance for exposed personnel is 300 meters from EO/UXO. If you can identify the EO/UXO by TYPE use the recommended safe distances below."
- "NEVER compromise safety for positive identification of a suspected EO/UXO."
- The 5-Cs: Confirm, Clear, Cordon, Check (5 and 25 m searches around the incident control point), Control.

The distances are Table 1's rows applied to typical items. The weight column is labelled "Average Weight of EO/UXO" and doesn't say "net explosive weight". This makes it a category-to-distance table Blast can use directly, with no estimate needed from the user. The figure's photographs should not be bundled (provenance unknown); the category names and numbers are public-release doctrine.

**Items 2 and 3: edition and Table 1 rows confirmed.**
- The cover reads "March 2024", and page footers read "01 MAR 2024".
- Table 1 (printed p. 18) has exactly these rows: ≤27, 30, 35, 40, 45, 50, 100, 150, 200, 300, 400 and 500 lb, giving 300, 310, 330, 350, 360, 375, 475, 550, 600, 675, 725 and 800 m. **There is no 250 lb row.** The worked example interpolates a 500 lb bomb (250 lb explosive) to 637.5 m.
- One wording inconsistency to note in Blast's documentation: paragraph 2.a.(1) says the Table 1 distances "provide a reasonable degree of safety for **barricaded** personnel and equipment", while the worked example calls 637.5 m "the safe distance for **unprotected** personnel". Blast should present the distances as minimums for people in the open, which is the more conservative reading.

**Other ATP values confirmed:**
- 300 m "minimum safe distance for exposed personnel" (para 3.a.(2), p. 19).
- Communications emitters: "ALWAYS transmit … from a minimum safe distance of 100 feet (30 meters)" (Chapter II warning, p. 11).
- CBRN: a 381 m isolation zone and a **2,000 m downwind** isolation and evacuation zone, drawn in Figure 11 (p. 23) as a sector extending **20° either side** of the downwind axis from the 381 m circle (attributed to ATP 4-32).
- The 9-line (Table 2, p. 26). Line 4 asks for a description "without touching, disturbing, or approaching the item".
- The Chapter II warning: "Once you recognize EO, STOP, leave the area by the same path you entered, and make necessary observations with binoculars or other optical devices."

**Item 4: DESR fragment tables read (Change 1).**
- *Table V3.E3.T2, HD 1.1 hazardous fragment distance (accidental detonations, open):*

  | NEWQD | HFD (open) |
  |---|---|
  | ≤0.5 lb | 236 ft (71.9 m) |
  | 1 lb | 291 ft |
  | 10 lb | 474 ft |
  | 31 lb | 563 ft |
  | 50 lb | 601 ft |
  | 100 lb | 658 ft (200.4 m) |
  | 200 lb | 927 ft |
  | 300 lb | 1,085 ft |
  | 450 lb | 1,243 ft |
  | >450 lb | **1,250 ft (381 m)** |

  The "structure" column has a **200 ft (61 m)** floor up to 31 lb. Equations V3.E3.T2-1 to -12 are given. This confirms the "1,250 ft above 450 lb" rule. 381 m equals ATP's CBRN isolation radius.
- *Table V3.E3.T3, HFD for selected HD 1.1 items (one unit, accidental detonation):*

  | Item | HFD |
  |---|---|
  | 105 mm projectile | 340 ft (103.6 m) |
  | 155 mm projectile | 415 ft (126.5 m) |
  | 5 in/54 projectile | 300 ft (91.4 m) |
  | Mk 82 500 lb bomb | 670 ft (204.2 m) |
  | Mk 83 1,000 lb bomb | 815 ft (248.4 m) |
  | Mk 84 2,000 lb bomb | 925 ft (281.9 m) |
  | M117 750 lb bomb | 690 ft (210.3 m) |
  | BLU-109 | 880 ft (268.2 m) |
  | CBU-87 | 800 ft (243.8 m) |
  | AIM-7 | 280 ft |
  | AIM-9 | 400 ft |
  | AGM-65A/B/D | 400 ft |
  | AGM-65E/F/G | 670 ft |
- *Table V5.E3.T1, default maximum case-fragment distance by diameter, for intentional detonations:* there are columns for robust, extremely-heavy-case and non-robust munitions. Robust examples:

  | Diameter | Robust MFD |
  |---|---|
  | 2.0 in | 1,220 ft (371.8 m) |
  | 3.0 in | 1,640 ft (499.8 m) |
  | 4.0 in | 2,012 ft (613.1 m) |
  | 6.0 in | 2,662 ft (811.2 m) |
  | 8.0 in | 3,228 ft (984.0 m) |
  | 10.0 in | 3,738 ft (1,139.3 m) |
  | 16.0 in | 5,044 ft (1,537.2 m) |
  | 30.0 in | 7,385 ft (2,250.7 m) |

  The equations (V5.E3.T1-3 robust, -11 non-robust, metric) reproduce the table exactly. They give these robust values for common calibres, with non-robust in brackets:

  | Calibre | Robust MFD (non-robust) |
  |---|---|
  | 60 mm | ≈420 m (290 m) |
  | 81/82 mm | ≈527 m (358 m) |
  | 105 mm | ≈627 m |
  | 120/122 mm | ≈690–696 m |
  | 152/155 mm | ≈821 m (535 m) |
  | Mk 82 (10.75 in) | ≈1,194 m |
  | Mk 84 (18 in) | ≈1,654 m |

  By the Glossary, "general purpose bombs, artillery projectiles, and penetrator warheads" are robust; torpedoes and underwater mines are non-robust.

  The stated limitations (V5.E3.2.7.4):
  - cylindrical munitions only;
  - "rogue" fragments (nose plugs, base plates, lugs) "can travel significantly greater distances (> 10,000 ft [3,048 m])";
  - shaped-charge jets are excluded;
  - low-probability reduced-drag fragments can fly farther than predicted.
- *Volume 7 (munitions response):* the minimum separation distance for **unintentional** detonations, for non-essential personnel, is "the greatest distance of" K40 blast overpressure or the calculated HFD (V7.E4.5.8.3.2.1). For intentional detonations it is the greater of K328 or the calculated MFD (V7.E4.5.8.3.2.2).

**Calibration result.** For a person waiting beside an undisturbed item (the "unintentional detonation" case), Figure 31 is roughly three times more conservative than DoD's accidental-detonation HFD:
- A 155 mm projectile has an HFD of 126.5 m, against Figure 31's 375 m for projectiles and mortars.
- A Mk 82 bomb has an HFD of 204 m, against 675 m for bombs and dispensers.

Against the worst-case maximum fragment distance (a planned-demolition criterion), Figure 31 is shorter for most calibres: 155 mm ≈821 m against 375 m. That supports the design already proposed: **Figure 31 as the default ring, and the diameter-based maximum fragment distance as an optional, clearly labelled outer "maximum fragment range" ring** (Phase 3).

**Items 6 and 7: UNMAS handbook read in full.**
- *Annex A5 (p. 114)* reproduces the older ATF/NCTC-style bomb-threat chart, not a UXO table. It has nine IED threats, each with "minimum evacuation distance, behind wall" and "recommended evacuation distance, in the open":

  | Threat | Behind wall | In the open |
  |---|---|---|
  | Pipe bomb, 5 lb | 130 m / 430 ft | 375 m / 1,200 ft |
  | Suicide bomber, 20 lb | 150 m / 500 ft | 525 m / 1,700 ft |
  | Briefcase/backpack, 50 lb | 190 m / 600 ft | 575 m / 1,900 ft |
  | Compact car, 500 lb | 270 m / 870 ft | 600 m / 2,100 ft |
  | Full-size car, 1,000 lb | 300 m / 1,000 ft | 725 m / 2,400 ft |
  | Van/SUV, 4,000 lb | 375 m / 1,225 ft | 1,160 m / 3,800 ft |
  | Small truck/moving van, 10,000 lb | 440 m / 1,450 ft | 1,560 m / 5,100 ft |
  | Water tanker, 30,000 lb | 525 m / 1,725 ft | 2,410 m / 7,900 ft |
  | Semi-trailer, 60,000 lb | 575 m / 1,875 ft | 2,840 m / 9,300 ft |

  The row weights differ from the CISA Aug 2025 card (e.g. Van/SUV 4,000 lb vs 1,000 lb), so the two charts must not be mixed. It adds nothing for UXO.
- *IED find (4.2.1, p. 85):* "Clear the area: Use the evacuation distance guide (annex A5.1) or **at minimum a distance of 300 metres**." This independently corroborates the 300 m floor.
- *Radio (3.4.4, p. 71):* "Avoid making radio or cellular phone transmissions around UXO, booby traps or IEDs in a distance closer than **50 metres**."
- *On foot in a minefield (4.1.1, p. 79), the MINED drill:*
  - "Movement stops immediately. Stop! Remain still and do not move your feet."
  - Inform others and call for help.
  - Note the area and the nearest known safe surface.
  - Evaluate.
  - "Do not move, if there is no indication of a safe area, or you can't reach it without stepping on unknown ground. Wait for help to arrive."
- *Retracing footsteps (4.1.1.2, p. 81):* "To retrace footsteps is **not a safe option** for getting out of a minefield … the procedure is extremely dangerous … If you want to learn the method … specialists … must be consulted." In a vehicle (4.1.2, p. 83): stop, "Do not attempt to reverse out of the area," and stay in the vehicle unless it is on fire.
- *Marking (4.1.1.1, p. 80):* "Never leave the safe path to make or place a warning sign … Never mark individual mines."

### Design corrections from the verified texts

1. **Category table.** Phase 1 should use Figure 31's six categories and distances as-is, with "unknown" set to 300 m and a one-tap escalation. It replaces the Blast-built category assignment from AFVA classes plus the 50% rule. AFVA classes can stay as recognition help, mapped onto Figure 31's rows:
   - grenades, landmines and bomblets → 300 m;
   - projectiles and mortars → 375 m;
   - rockets and missiles → 475 m (large or guided missiles → 600 m);
   - bombs and dispensers → 675 m;
   - stockpile or cache → 800 m.
2. **Multiple items:** add Figure 31's "+50 m or more" when more than one item is seen.
3. **Mines and submunition areas: the guidance had to change.** The earlier "leave by your known route" was too permissive for someone who may already be in a mined area. Blast should split the guidance:
   - **"I can see an item from a road or path I know is safe":** stop, leave by the same path (ATP Ch. II), and keep at least the ring distance.
   - **"I may be in a minefield or submunition area":** stop and don't move your feet. Call for help and wait, unless you can reach a known safe surface without stepping on unknown ground (UNMAS MINED). In a vehicle, stop, don't reverse, and stay in it.

   Neither branch should advise retracing footsteps as a general method.
4. **Radio banner.** The verified values are 30 m (ATP 2024) and 50 m (UNMAS 2015). The "retreat to the ring first, then transmit" sequence still clears both. Blast's own default ring is at least 300 m.
5. **CBRN sector geometry:** use a 381 m circle plus a 2,000 m downwind sector of ±20°, as drawn in ATP Figure 11.
6. **Licensing unchanged.** ATP 4-32.2 (Distribution A) and DESR (cleared for public release) values can be bundled with citation. The UNMAS handbook is all rights reserved: cite and paraphrase, don't copy.

### Still open

- DESR Change 2 (Nov 2025): confirm that the V3.E3.T2/T3, V5.E3.T1 and V7 values are unchanged.
- IMAS standards (item 5) and everything in items 8–16 not covered above.

## Verification addendum 2 (2 November 2026): IMAS read

The user supplied the full IMAS set as of 1 May 2026 (77 documents: IMAS, TNMA technical notes and TEP test protocols). This closes item 5.

**Editions read:**

| Document | Edition |
|---|---|
| IMAS 04.10 Glossary | Ed. 2, Amendment 12, 25 October 2024 |
| IMAS 09.30 EOD | Ed. 2, Amendment 6, September 2022 |
| IMAS 10.10 OHS general requirements | Ed. 2, 16 May 2025 |
| IMAS 10.20 Demining worksite safety | Ed. 1, Amendment 7, June 2013 |
| IMAS 12.10 EORE | Ed. 2, Amendment 3, September 2020 |
| TNMA 10.20/01 Estimation of explosion danger areas | Version 2.0, Amendment 1/2, July 2013 |
| TNMA 12.10/01 Risk education for IEDs | Ed. 1, 22 November 2022 |
| TEP 12.10/01/2026 EORE practitioner competencies | Ed. 1, 16 January 2026 |

**Main finding: IMAS gives no keep-back distance for a member of the public, or a responder, who finds an item.** Every IMAS distance belongs to one of these:
- **Worksite working distances between PPE-wearing deminers.** IMAS 10.20 Annex B Table 1 gives 10–50 m depending on mine type and risk, and says they "should not be applied during demolitions".
- **Planned-demolition safety distances.** IMAS 10.20 Annex B Table 2, for a single mine plus minimum demolition charges:

  | Mine type | Demolition staff (PPE) | Other staff |
  |---|---|---|
  | AP blast | 25 m | 60 m |
  | AP fragmentation | 60 m | 100 m |
  | AT | 200 m | 300 m |

  Other staff without PPE should also be "out of line of sight".
- **Calculated danger areas for large single items and multi-item demolitions** (TNMA 10.20/01). Public access is R = 634 × AUW^(1/6) m and controlled access is R = 444 × AUW^(1/6) m (AUW = all-up weight in kg, untamped). These are ranges "outside which one would not normally expect more than one significant fragment to travel". Illustrative public-access results:

  | Item (all-up weight) | Public-access danger area |
  |---|---|
  | 4 kg | ≈800 m |
  | 43 kg (155 mm-class shell) | ≈1,190 m |
  | 227 kg | ≈1,570 m |

  The note says this estimation "is not a task for basic deminers or other field staff". When advice is unobtainable, it says to use the simple tables and "add 50% if the circumstances allow it".

This confirms the report's anchor choice. **For the bystander and responder scenario, the only public keep-back numbers are military: ATP 4-32.2 Figure 31 and Table 1, with a 300 m minimum. The 300 m minimum is independently echoed by the UNMAS handbook for IED finds.** IMAS distances should appear in Blast only as context, not as rings:
- what deminers use at a worksite;
- that planned demolitions push the public out to roughly 0.8–1.6 km.

**Other IMAS content relevant to Blast:**
- **Mine blast and fragment ranges** (IMAS 10.20 B.2): the blast danger area of the largest AP blast mines is "less than 10 metres", eardrum risk "may approach 25 metres", blast debris reaches up to 10 m, and fragmentation mines endanger staff "at distances up to 50 metres". This is consistent with the UNMAS handbook figures.
- **Shaped-charge jets** (TNMA 10.20/01 §7.4): HEAT munitions ("shoulder-launched anti-tank rockets, anti-tank missile warheads and even some sub-munitions and anti-tank mines") produce a jet that "can be expected to travel up to 1800 metres in free air". **Design implication:** the rockets/missiles category should add a plain warning: "Shaped-charge (anti-tank) items can project a jet far along their axis. Never stand in line with the nose or tail; keep to the side." This is protective advice only, with no handling content.
- **EORE messages** (IMAS 12.10, TNMA 12.10/01): the three common elements are recognition, safer behaviour and reporting:
  - "Stay away from hazardous areas and suspicious objects"
  - "Do not touch, do not approach suspicious objects"
  - marking is "sometimes included"

  In emergencies, messages stress "recognition of potentially dangerous areas, as well as who to report to".
- **Reporting can carry reprisal risk** (TNMA 12.10/01 §7.2–7.4): in non-permissive environments, marking or reporting items "currently in use by parties to the conflict" can expose people to reprisal. Modifying the report message, or confidential and anonymous reporting, is suggested. **Design implication:** Blast's sharing must stay deliberate, never automatic:
  - no auto-send for UXO markers;
  - a clear choice of recipients;
  - the 9-line marked as optional;
  - the option to keep a marker local.
- **Definitions** (IMAS 04.10): explosive ordnance covers mines, cluster munitions, UXO, abandoned ordnance, booby traps, other devices and IEDs. UXO is "explosive ordnance that has been primed, fuzed, armed or otherwise prepared for use or used… yet remains unexploded". These are good wording sources for Blast's UI glossary.

**IMAS licence** (copyright notice in each standard): owned and copyrighted by the United Nations. "The use, reproduction or re-dissemination of IMAS by third parties, in whole or in part, is permitted provided that the United Nations is appropriately attributed and provided also that such use, reproduction or re-dissemination is not for commercial purposes." Required attribution: "Used and reproduced with permission of the United Nations." Blast is free and non-commercial, so short quoted messages with that attribution are permitted. Citing values is simpler still.

**Open items after both addenda:**
- DESR Change 2 (Nov 2025): confirm values unchanged.
- The minor items from the original list: CMAS 10.20 final version, Essen page, USACE slide mapping, 3Rs terms, AFVA image credits, and the unread leads in item 14.

None of these block a Phase 1 specification based on ATP 4-32.2 Figure 31.

## Verification addendum 3 (2 November 2026): DESR Change 2

The user supplied **DESR 6055.09, Edition 1, Change 2 (25 November 2025)**, which is marked "CLEARED FOR PUBLIC RELEASE" (SHA-256 d71fa4c8…). It was compared with Change 1 by extracting every number from each table:

| Table | Change 2 printed pages | Result |
|---|---|---|
| V3.E3.T2, HD 1.1 HFD (with equations) | 123–124 | **Identical** (239 values) |
| V3.E3.T3, HFD for selected items | 125–126 | **Identical** (326 values) |
| V5.E3.T1, default MFD by diameter (with equations) | 226–229 | **Identical** (532 values) |
| V5.E3.T2, default MFD by NEW | following pages | **Identical** (640 values) |

The V5.E3.2.7.4 limitations text (cylindrical items only, "rogue" fragments beyond 10,000 ft, shaped-charge jets excluded) is unchanged. The robust, non-robust and extremely-heavy-case glossary definitions differ only in layout.

**Volume 7 was restructured** (munitions response distances are now in V7.E6.1, Enclosure 7, pp. 324–326), but the rules are the same:
- **Unintentional detonation, non-essential personnel and the public** (V7.E6.1.2.1.3 and V7.E6.1.2.2.6.1): the greater of K40 blast overpressure for the MCE, the maximum HFD for the munitions expected (DDESB TP 16), or, for chemical agents, the 1% lethality downwind distance.
- **Intentional detonation, all personnel and the public** (V7.E6.1.1): the greatest of K328 blast overpressure, with a 200 ft minimum; the calculated MFD (DDESB TP 16); or the CA downwind hazard distance.

**Item 4 is fully closed.** Blast may cite the following with `ed="Ed1-Ch2 2025-11-25"`, and golden tests should assert the Change 2 values:
- the HFD values (V3.E3.T2 and T3);
- the MFD-by-diameter equations and values (V5.E3.T1).
