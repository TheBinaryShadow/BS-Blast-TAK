# Blast pre-release audit

Date: 31 Oct 2026. Version audited: 0.6 (commit 42b57dc); fixes in 0.7
(commit 357b935 and the gradlew fix after it).

## Scope and method

- Every source file read line by line: `core/` (12 classes), `atak/`
  (5), `ui/BlastPane`, `BlastPlugin`, manifest, resources, Gradle and
  ProGuard configuration, README and manual.
- Every ATAK behaviour Blast depends on checked against the public
  atak-civ source (tag 5.5.1.8) rather than assumed.
- Static analysis: `javac -Xlint:all` against ATAK 5.5.1.8 `main.jar`,
  Android lint (`lintCivRelease`), SpotBugs 4.8.6 at maximum effort with
  the FindSecBugs 1.13 security rules (the nearest open equivalent of the
  Fortify scan the TAK.gov pipeline runs).
- TAK.gov pipeline dry run: `git archive` source zip, extracted with no
  git history, `assembleCivRelease` for ATAK 5.6.0, APK inspected.

Result: 0.7 has no compiler warnings, 0 lint errors, no open SpotBugs
findings that are real issues, and 61 unit tests passing (32 before the
audit), including fuzzing of everything Blast reads from the network.

## Findings and fixes

Severity: High = could put wrong or missing data in front of users or
block release; Medium = incorrect behaviour in realistic cases; Low =
robustness, hygiene, maintainability.

| ID | Sev | Finding | Fix | Test |
|---|---|---|---|---|
| H1 | High | Card buttons kept acting on an incident after it was deleted elsewhere. SEND TO ALL could re-broadcast a deleted incident to the whole team; Rename/Clear could draw orphan rings. | Every pane action re-checks the incident is on the map (toast "no longer on the map"); `BlastStore.write`/`ensureRings` refuse removed markers. | Device checklist 2 |
| H2 | High | `gradlew` stored in git without the executable bit (hidden by `core.fileMode=false`). The pipeline runs `./gradlew`, so the submission build would fail. | `git update-index --chmod=+x gradlew`. | Zip inspected: mode 755 |
| H3 | High | Clear/Reopen/Rename rewrote the marker remarks, deleting notes people added in ATAK (with or without Blast). | `Labels.mergeRemarks` replaces only Blast's `[BLAST] … Minimum distances, not guarantees.` block; anything else is kept; a damaged block is never dropped. | `RemarksMergeTest` |
| M1 | Med | ATAK's `EventSet` keeps duplicate listeners, so every update added another delete watcher to the marker and rings (slow leak, repeated delete checks). | Each item watched once (weak identity set); all removed on plugin stop. | Source check (`EventSet.addListenerImpl`) |
| M2 | Med | A received `__blast` detail could carry any distances or label for a known edition; Blast drew them (a corrupted or tampered detail could shrink rings on every Blast client). | For editions this build has, the table's values and label are used; a mismatch is flagged on the card ("differs from this build's copy") and never drawn. Unknown editions capped at 100,000 ft. | `knownEditionTableWinsOverTamperedDistances`, `unknownEditionIsCappedAndChecked` |
| M3 | Med | GPS accuracy worse than 1,000 m (or unknown) was treated as 15 m, so a poor fix gave confident zone guidance. | Real CE used; unknown CE handled (NaN, CotPoint 9999999); card warns when CE exceeds the mandatory radius. | `unusableDistanceGivesNoResult`; device checklist 7 |
| M4 | Med | Clear/Reopen used the record read when the dialog opened, overwriting any update received meanwhile. | Record re-read on confirm. | Code review |
| M5 | Med | Incident number from the network unbounded (overflow to negative numbering). | Clamped to 0…999,999. | `incidentNumberIsClamped` |
| M6 | Med | Deprecated ATAK APIs, two slated for removal (`WidgetIcon`, `MapWidget`), plus `GeoCalculations.distanceTo` and `PreferenceManager`. | Replaced with `Icon.Builder`/`IMapWidget`, `core.Geo` (WGS84 Vincenty, tested against GeographicLib), `AtakPreferences`. `inverseWithElevation` kept (see Accepted). | javac: 0 deprecation warnings |
| L1 | Low | Saved incident registry never shrank for incidents deleted while Blast wasn't loaded. | Pruned of UIDs ATAK no longer knows (re-found if they load later). | Code review |
| L2 | Low | Geodesic distance NaN for near-antipodal points would read as "shelter-in-place". | Spherical fallback; `PositionCheck.check` returns null for non-finite distance; non-finite counts as mandatory. | `distanceNeverNaNForValidPoints` |
| L3 | Low | Unknown attributes from the network unbounded. | At most 64 attributes, 256 chars each. | `unknownAttributesAreBounded` |
| L4 | Low | Public mutable arrays (`Guidance.CAVEATS`, `ErgData.LPG_NOTES`; SpotBugs MS, high). | Unmodifiable lists. | SpotBugs clean |
| L5 | Low | Radial button could NPE if ATAK had no UI service. | `showPane()` returns false; callers check. | Code review |
| L6 | Low | Incident list re-parsed each record many times per refresh (inside the sort). | Records read once per refresh. | Code review |
| L7 | Low | Custom quantity had no input limit; `1e999` became infinity. | 12-character limit; non-finite input rejected. | `hostileQuantities` |
| L8 | Low | Export file path: defence in depth (Fortify path manipulation). | Whitelisted file stem plus canonical-path containment check. | `fileStems` |
| L9 | Low | Received details weren't mirrored into ATAK's opaque store, so they'd be lost if Blast were later uninstalled. | `BlastStore.store()` used on create and receive. | Code review |
| L10 | Low | Dead code (`markerName`, `blast.sentAll`, unused fields), "zone" vs "incident" wording, disclaimer without ERG2024, catch-all `Exception`, non-final plugin class. | Removed / unified / narrowed / `final`. | SpotBugs, lint |

## ATAK behaviour verified in source

| Assumption | Where checked | Result |
|---|---|---|
| SEND TO ALL reaches mesh and TAK servers | `CotDispatcher.dispatchToBroadcast` → `CommsMapComponent.sendCoT(null…)` → `commo.broadcastCoT` | Yes |
| Remote delete format accepted | `CotDeleteImporter` (type `t-x-d-d`, link uid/relation/type, `__forcedelete`) | Matches; `findAssocShape` leaves our marker alone (no `shapeUID`) |
| Rings don't rename or move the marker | `DrawingCircle.setTitle/setCenterMarker/onPointChanged` | Correct; marker drag moves rings |
| Incident markers don't expire | `CotEventFactory` (marker stale 1 year), `CotMarkerRefresher` (markers only, a-u only with expireUnknowns) | New a-h-G markers never auto-expire; rings aren't markers |
| Non-Blast renames keep the detail | `MarkerImporter` updates in place; `OpaqueHandler` keeps/re-emits unknown details | Yes |
| Duplicate listeners | `EventSet.addListenerImpl` | Duplicates kept → M1 |
| Radial icon API change is equivalent | `AbstractButtonWidget.setIcon` → `setWidgetIcon` | Identical path |

## Accepted (by design, documented)

- **Numbering race:** two devices placing at the same moment can both
  show "BLAST 4"; UIDs differ, nothing merges. Rename fixes the label.
- **Non-Blast clients:** deleting only the marker in plain ATAK leaves the
  rings (ATAK re-centres them); Blast clients remove the whole incident.
- **`MapView.inverseWithElevation`** is deprecated but not for removal
  (`forRemoval=false`); ATAK's own `Tool.findPoint` uses it.
- **Lint (22 warnings, 0 errors):** 17 SetTextI18n (English-only UI by
  design); template items: IconLocation, DataExtractionRules,
  UnusedResources (`versionName`, archive opt-out xml).
- **SpotBugs remaining:** SECBROAD (`AtakBroadcast` is ATAK's in-process
  broadcast), EI/EI2 (constructor injection), SECPTI (see L8), SECXML
  (constant style ids), CT (ATAK instantiates the plugin; class is final),
  UrF on `ErgData` (reserved for the ERG presets, golden-tested), SIC.
- ~~Last threat row pre-selected~~: changed in 0.8 at the user's request.
  No threat is pre-selected; the user chooses one for every incident.

## Pipeline dry run

- `git archive --prefix=Blast/`: single root folder, 123 files, no build
  output, keystores or local.properties; `gradlew` 755 after H2.
- Built from the extracted zip without `.git`: `assembleCivRelease`
  (ATAK 5.6.0) and unit tests pass. APK: version code 106 from
  `takStaticVersion`, `plugin-api com.atakmap.app@5.6.0.CIV`, targetSdk 34,
  no permissions, only `BlastPlugin` keeps its name (rest
  `atakplugin.Blast.*`), debug diagnostics stripped.
- Not reproducible here: `./gradlew` downloading Gradle (services.gradle.org
  blocked in this workspace; the same Gradle 8.14.3 was used directly).

## Device checklist before 1.0

1. Install the **minified release** APK (what the pipeline builds) and
   repeat the main flows; earlier rounds mostly used the debug build.
2. Two devices: open an incident's card on A, delete it from B, then tap
   SEND TO ALL / Rename / Mark cleared on A → "no longer on the map", and
   the incident does not come back on B.
3. Add a note in the marker's remarks (ATAK details), then Clear and
   Rename → the note is still there.
4. Rename from a phone without Blast → the tablet shows the new name, and
   Clear keeps it.
5. Unload and reload the plugin (Plugins menu) → no crash; incidents stay;
   deleting one ring still removes the whole incident.
6. Restart ATAK with incidents on the map → list, numbering and rings OK.
7. Indoors (poor GPS) → the "GPS accuracy is poor" line appears.
8. Export all three formats; import the data package on a second device;
   open the GPX on a Garmin (check the "Danger Area" symbol).
9. Radial button looks and works as in 0.3+.

## Release steps (1.0)

`PLUGIN_VERSION = "1.0"`, `takStaticVersion=10000`, manual
`plugin-version: "1.0"`, README status; tag `v1.0`;
`git archive --format=zip --prefix=Blast/ -o Blast-1.0-source.zip v1.0`;
submit to the Third Party Pipeline; keep the returned zip; publish the
signed APK, SBOM and Fortify PDF with the APK's SHA-256.
