# Restaurant List verification

Checked October 10, 2026. Covers FR-01, FR-00, FR-10 and FR-22 for the current in-memory UI prototype.

| Check | Result |
| --- | --- |
| Debug app and Android test APK builds | Passed |
| Name and tag filtering unit tests | 9 passed, zero failures/errors |
| Compose device behavior tests | 4 passed on API 37.1 |
| Android lint | Zero errors; AGP update, missing application icon and unused legacy plain-close icon warnings |

The device tests cover scrolling to the third card and opening its own Details, Back retaining scroll, combined name/multiple-tag filtering, All resetting tags while retaining the query, saved-state restoration, no-results reset, About and all four names, an injected empty collection, blank-name validation and adding/restoring/filtering a custom record. A user tag named “all” is tested separately from the All reset control. The refinement run also checks that the count updates before the keyboard's Search action, the circled clear button works for both empty and populated input, and La Unica has its original photo.

The default app seeds three restaurant records. Empty collection behavior is exercised with an injected empty dataset and a Compose preview; deletion is part of the later full Details workflow. Add, Details and About are working supporting destinations for this List section. The shared full Add/Edit form, half-star dragging, full Details actions, gallery, maps, email and Room storage remain later work.

All three original photos are now bundled. Recovered Midyeci Riza and La Unica bytes exactly match Figma's uploaded image hashes; see [photo sources](restaurant-list-photo-sources.md). The current Home wireframe remains the visual reference. Fresh browser inspection was possible while the Figma connector remained at its call limit.

The October 10 refinement uses the 80dp circular Add button, a smaller 22sp heading with its extra top padding removed, a purple outlined search field with an always-floating label and circled clear icon, matching purple heading/chip colors, and “saved restaurant(s)” wording in all count states. Search remains editable and updates the list immediately; it does not request focus or open the keyboard on launch. The field can grow at larger font scales. Original storefront photos use top-biased crops to preserve their signs. About and additional user-defined tags remain available.

The refinement build, nine unit tests and four device tests passed. Visually reviewed default, live-search and third-card captures and the production List after Splash. The production activity was resumed; the saved log contains no application fatal exception or application ANR. [Dated verification evidence](../../../outputs/2026-10-10-restaurant-list-parity/) preserves build/runtime receipts and all six app-window captures, plus the production screenshot. Runtime screenshots use a 412 × 940dp display override, with Android system-bar/cutout insets. Those safe insets and the retained About/custom-tag controls explain remaining differences from Figma's bare phone frame; this is not a claim of pixel-perfect full-app parity.

Testing used a task-owned, read-only Pixel_7 emulator with 1.5 GB guest RAM, two cores, disabled guest animations/network and nonessential Google background packages. The stock API 37.1 guest had system-wide cold-boot stalls; the lighter fixture completed the behavior checks. Screenshots use Android’s app-window capture API to exclude unrelated system dialogs. These checks establish Compose behavior and layout, not a clean stock-device startup or physical finger-input test. Minimum-API and physical-device checks were not performed.

Stop the task-owned emulator immediately after testing. Keep the student’s Android Studio, shared adb server and unrelated work running.

To repeat the checks with a suitable connected test device:

```sh
./gradlew :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug
./gradlew :app:connectedDebugAndroidTest
```
