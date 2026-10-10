# Restaurant List verification

Checked October 10, 2026. Covers FR-01, FR-00, FR-10 and FR-22 for the current in-memory UI prototype.

| Check | Result |
| --- | --- |
| Debug app and Android test APK builds | Passed |
| Name and tag filtering unit tests | 9 passed, zero failures/errors |
| Compose device behavior tests | 4 passed on API 37.1 |
| Android lint | Zero errors; existing AGP update and missing application icon warnings |

The device tests cover scrolling to the third card and opening its own Details, Back retaining scroll, combined name/multiple-tag filtering, All resetting tags while retaining the query, saved-state restoration, no-results reset, About and all four names, an injected empty collection, blank-name validation and adding/restoring/filtering a custom record. A user tag named “all” is tested separately from the All reset control.

The default app seeds three restaurant records. Empty collection behavior is exercised with an injected empty dataset and a Compose preview; deletion is part of the later full Details workflow. Add, Details and About are working supporting destinations for this List section. The shared full Add/Edit form, half-star dragging, full Details actions, gallery, maps, email and Room storage remain later work.

The retained Two Brothers original photo is bundled. Midyeci Riza and La Unica use labelled image placeholders because their originals were unavailable and the Figma connector reached its call limit. The saved wireframes remain the visual reference.

Testing used a task-owned, read-only Pixel_7 emulator with 1.5 GB guest RAM, two cores, disabled guest animations/network and nonessential Google background packages. The stock API 37.1 guest had system-wide cold-boot stalls; the lighter fixture completed the behavior checks. Screenshots use Android’s app-window capture API to exclude unrelated system dialogs. These checks establish Compose behavior and layout, not a clean stock-device startup or physical finger-input test. Minimum-API and physical-device checks were not performed.

Stop the task-owned emulator immediately after testing. Keep the student’s Android Studio, shared adb server and unrelated work running.

To repeat the checks with a suitable connected test device:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```
