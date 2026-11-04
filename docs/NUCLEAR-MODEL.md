# Blast nuclear reference mode: model and verification (1.1)

Model id `BLAST-NUC`, edition `2026-11` (carried in `<__blast_nuc/>`;
bump the edition whenever any number below changes).

## Scope and constraints

- Protective planning and training reference. Not a prediction of a real
  event; no casualty estimates.
- No burst height is ever asked for or shown. Air-burst rings are the
  farthest ground range each effect reaches over all heights (an outer
  bound); "Unknown" (the default) draws the larger of surface and air for
  every ring, plus fallout.
- Every number comes from a US government work (FEMA, Glasstone & Dolan,
  LLNL). Yield presets are public, unofficial estimates, cited, not
  copied as tables.
- Protective actions lead the card: FEMA's "Get Inside, Stay Inside, Stay
  Tuned".

## Sources verified against the primary PDFs (3 Nov 2026)

| Item | Where | Value used | Status |
|---|---|---|---|
| SDZ / MDZ / LDZ at 10 kT ground level | FEMA 3rd ed. pp. 18-19 | 0.5 / 1 / 3 mi | verified (text) |
| Yield scaling | FEMA Fig. 5 (p. 20) | "factor of two" per tenfold; MDZ 1/4, 1/2, 1, 2 1/4 mi at 0.1-100 kT | verified; cube root used (2.15 per decade) |
| "Zone descriptions apply to any size" | FEMA fn. 7 | zone meanings | verified |
| Get Inside wording, 12-24 h, 50 miles, 10 min | FEMA pp. 43-46 | card DO NOW | verified |
| DRZ > 10 R/h, Hot Zone 10 mR/h-10 R/h | FEMA fn. 10-11, §1.6.2 | Reference text | verified |
| Outer clothing removes up to 90% | FEMA Communicating (2024) | Reference text | verified |
| Burn and 1 Gy rings, cross-check | FEMA Fig. 7 (p. 25) | 100 rad: ~0.4/0.7/1.0/1.3 mi; burns ~1 mi at 10 kT, ~2.9 mi at 100 kT | read from figure; golden tests |
| Fireball | Glasstone §2.127-2.128 | max radius ~2x breakaway radius (110 W^0.4 air, 145 W^0.4 surface) | verified; not drawn (always inside SDZ) |
| Overpressure envelopes, 1 kt | Glasstone Fig. 3.73c | surface / max ground range: 8 psi 1,068 / 1,683 ft; 2 psi 2,516 / 4,199 ft; 1 psi 3,807 / 7,006 ft | digitized (±5%) |
| Fig. 3.73a range | Glasstone p. 111 | high-pressure chart only (100-10,000 psi) | verified; no curve below 1 psi exists |
| Blast accuracy | Glasstone §5.142 | ±20% | verified |
| Window failure | Glasstone Table 5.145 | 0.5-1.0 psi | verified |
| Thermal exposure vs slant range | Glasstone Fig. 7.42 (air bursts to 15,000 ft, 12-mile visibility) | 3/5/8/12/25/50 cal/cm² curves, 1 kt-10 Mt | digitized (41 yields); §7.43 example (8 cal at ~7 mi, 1 Mt) reproduced |
| Surface thermal partition | Glasstone §7.101-7.102, Table 7.101 | f = 0.18 surface vs 0.35 air | verified |
| Skin-burn thresholds | Glasstone Fig. 12.64 | second degree, lowest curve: 3.55 (1 kt), 3.85 (10 kt), 4.51 (100 kt), 5.46 (1 Mt), 6.63 (10 Mt) cal/cm² | digitized |
| Gamma dose | Glasstone Figs. 8.33a (fission, 1-100 kt), 8.33b (thermonuclear, 100 kt-20 Mt) | 30-10,000 rad curves | digitized; §8.34 example (50 kt, 2,000 yd ≈ 250 rad) reproduced |
| Neutron dose | Glasstone Figs. 8.64a/b | same | digitized |
| Reliability | Figs. 8.33a/b captions | 0.5-2 (fission), 0.25-1.5 (thermonuclear) | verified (both text and caption say 0.25-1.5) |
| Surface corrections | Glasstone Table 8.37; §8.65 | gamma x2/3 (1-50 kt) … x3 (5-20 Mt); neutrons x1/2 | verified |
| Fallout contours | Glasstone Table 9.93 | all 24 coefficients | verified (page image) |
| Contour shape | Glasstone §9.93, Fig. 9.93 | upwind semicircle r = GZ width / 2; widest near 0.8 of length | verified / read from figure |
| Wind scaling | Glasstone §9.96-9.98 | F = 1 + (v-15)/60 above, /30 below 15 mph; 8-45 mph; widths unscaled | verified |
| Fission scaling, terrain | Glasstone §9.94-9.95 | 100% fission, no terrain factor (conservative) | verified |
| 7-10 rule | Glasstone §9.15 | t^-1.2 | verified |
| Shelter PF | LLNL-TR-410067 Table 1, Table 4 | wood house 2-3, basement 10-20, vehicle 1.7-2, apartment 10 / 100, blockhouse 10-150 / 30-1,000, underground 5,000; Good 100 / Adequate 10 / Inadequate 3 | verified (page image) |
| Dose effects | LLNL Table 2 | 100-1,000 rad rows | verified |
| LLNL licence | front matter | DOE auspices statement and standard disclaimer; no copyright claim | checked |

## Model

- Damage zones: R = R10 (W/10)^(1/3), R10 = 0.5 / 1 / 3 mi. Air
  envelope ratios: SDZ 1683/1068 = 1.58 (8 psi, the larger of the 5-8 psi
  readings), MDZ 4199/2516 = 1.67 (2 psi), LDZ 2.0. FEMA's 3rd edition
  prints no psi; the zone overpressures (SDZ edge 5-8, MDZ 2-3, LDZ
  ~0.5 psi) are from the 2nd edition (2010). Glasstone's charts end at
  1 psi; the envelope ratio rises toward low pressure (1.51 at 15 psi,
  1.67 at 2 psi, 1.84 at 1 psi), so 2.0 is used for 0.5 psi and flagged.
- Burns: threshold q(W) from Fig. 12.64 (log-log interpolation); air
  range = Fig. 7.42 slant range at q (log range vs log exposure); surface
  range = air chart at q x 0.35/0.18. Drawn as ground range.
- Initial radiation: total = gamma + neutron (log dose linear in range
  between curves); fission figures to 100 kt, thermonuclear above (the
  larger of the two just above 100 kt, so rings never shrink with yield).
  Surface: gamma x Table 8.37 (log-yield interpolation), neutrons x1/2.
  Ring at 100 rad (FEMA's "radiation injuries" line). Solved by bisection.
- Fallout: Table 9.93 contours for 1,000 / 100 / 10 R/h at H+1, downwind
  x F(v); max width = max(table max width, GZ width). Drawn only for
  surface or unknown bursts with a wind. Arrival estimate = downwind
  distance / wind speed.
- Outside 1 kt-10 Mt the thermal and radiation curves are extrapolated
  along their end segments (flagged on the card). Yield input is limited
  to 0.1 kt-20 Mt.

## Results (rounded)

| Yield | Burst | SDZ | MDZ | LDZ | Burns | 100 rad |
|---|---|---|---|---|---|---|
| 0.1 kt | surface | 0.11 mi | 0.22 mi | 0.65 mi | 0.12 mi | 0.42 mi |
| 1 kt | surface | 0.23 mi | 0.46 mi | 1.4 mi | 0.38 mi | 0.69 mi |
| 10 kt | surface | 0.50 mi | 1.0 mi | 3.0 mi | 1.1 mi | 0.98 mi |
| 10 kt | air / unknown | 0.79 mi | 1.7 mi | 6.0 mi | 1.6 mi | 1.0 mi |
| 100 kt | surface | 1.1 mi | 2.2 mi | 6.5 mi | 2.9 mi | 1.4 mi |
| 1 Mt | surface | 2.3 mi | 4.6 mi | 13.9 mi | 6.7 mi | 1.9 mi |
| 1 Mt | air | 3.7 mi | 7.7 mi | 27.8 mi | 8.4 mi | 1.9 mi |

## Military picture (1.1 test 5): legacy public doctrine

Sources read from the PDFs (3 Nov 2026): FM 3-3-1 (9 Sep 1994, approved
for public release), FM 3-11.3 (2 Feb 2006, Change 1 30 Apr 2009,
distribution unlimited), JP 3-11 (26 Aug 2008), LLNL NARAC briefing
products. The current TM 3-11.32 and ATP-45 (Ed. F) are restricted; the
user's ATP-45 Ed. E (2014) file contains only the front matter.

| Item | Source | Used |
|---|---|---|
| Zone I / II definitions | FM 3-11.3 p. G-23 | 125 cGy in < 4 h / 75 cGy in 24 h (FM 3-3-1's older text says 150/50 with garbled units) |
| Zone I distance | FM 3-3-1 Table E-4 (p. E-11), 57 yields 0.01 kt-10 MT x 20 winds 8-160 km/h | transcribed from the page image; log-log interpolation; cells where the table drops with larger yield/wind (6-13 km band, e.g. 4 kt at 8 km/h printed 6 < 3 kt's 6.1) raised to the neighbour |
| Worked examples | FM 3-3-1 p. 3-24 (16 kt, 16 km/h -> 18 km), FM 3-11.3 Fig. G-16 (35 kt, 29 km/h -> 33 km nomogram; table gives ~34.6), FM 3-3-1 p. 3-28 (100 kt, 16 kn -> 30 nmi) | unit tests |
| Zone II | FM 3-11.3 G-31 | 2 x Zone I |
| Sector | FM 3-3-1 p. 3-23; CM5206 | 40° (radials ±20°), EDM 9-digit expanded angle |
| Cloud radius | FM 3-3-1 p. 3-23 predictor: 1.2/1.9/4.2/6.8/11.2/18.0/28 km for 2/5/30/100/300 kt/1/3 MT; Table E-1 above 3 MT (10 MT 46 km, 20 MT 62 km) | yield-group radius |
| Low wind | FM 3-11.3 G-31, FM 3-3-1 p. 3-16/3-24 | < 8 km/h: circles; Zone I from table at 8 km/h or 3-digit EDM |
| Arrival | FM 3-3-1 p. 3-23 | arcs every EWS km; actual as early as half; none by 2x or 12 h |
| Air burst | FM 3-11.3 G-23 | no prediction |
| NBC 1 NUC | FM 3-11.3 C1 Fig. G-3, Table III-2 | lines BRAVO DELTA FOXTROT GOLF HOTEL JULIET KILO LIMA YANKEE GENTEXT; ALFA left to the CBRN cell |
| NBC 3 PAPAB | FM 3-11.3 G-46/47, Fig. G-26 | sssKPH/xxxKM/rrKM/left/right (rr rounded up) |
| Polar plot | FM 3-11.3 G-15 | flash-to-bang x 0.35 km/s |
| RES / severity / OEG | JP 3-11 (2008) Figs. D-2, D-3, D-6 | Reference tab |

Not done: yield estimation from cloud width (FM 3-11.3 Fig. G-8 nomogram)
and the detailed (wind-vector) prediction.

## Not modelled (deferred)

Burst heights between surface and air (partial fallout), altitude, terrain
and building shielding, cloud heights and wind shear, precipitation hot
spots, dose accumulation, mass fire, EMP, multiple bursts, casualty
estimates. WSEG-10 was rejected (fixed source constant 3-4x later models,
activity loss at low yields).
