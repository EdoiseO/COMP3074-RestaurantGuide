# Restaurant Details implementation and verification

October 10, 2026 · Native Jetpack Compose UI prototype · Team P12.

Restaurant List now opens a complete scrollable Details screen for the selected record. Two Brothers Shawarma uses the Lake Shore branch; Midyeci Riza and La Unica use the researched Wilson Avenue entries. Each has its own description, six food tags, original cover, address, phone number and selected restaurant map. The existing 4.0, 5.0 and 3.0 ratings remain sample personal ratings.

## Design evidence and visual scope

The target is Figma **03 — Restaurant description**, node **24:121**, in [Restaurant Guide](https://www.figma.com/design/8Z8T16iL2SBTzZm6pUShAA). Fresh connector design-context and screenshot requests were blocked by the Starter plan's MCP call limit. The retained October 7 [canonical export](wireframes/03-restaurant-description.png) and saved native node properties supplied the visual evidence; the Figma file was not changed.

The Android screen uses a 48dp toolbar, 20dp content margins, a 180dp cover, a compact 126×36dp white star badge, purple headings, outlined read-only tags, 84dp photo thumbnails, paired contact columns, a 160dp embedded map, two compact purple map/email buttons, and rounded lavender Notes. The toolbar was reduced from the reference's 66dp on the student's spacing request. No whole wireframe screenshot is used as a UI asset. Native system insets, font metrics, natural contact wrapping and the shorter device viewport affect the rendered positions. This establishes scoped visual parity, not an exact pixel match across devices.

The latest Details tags are labels, without checked state or click actions. Restaurant List filters retain their selectable behavior. The Notes pencil opens a quick editor; optional captions belong to individual photos. Supporting basic Edit, map, gallery and dialogs are native destinations. The full shared Add/Edit design and draggable fractional stars are separate planned sections.

## Implemented interactions

- Back preserves the List's search, selected tags and scroll. A newly opened card starts Details at the top; returning from its supporting actions retains its Details position.
- Edit pre-fills the selected record's name, description, tags, address, phone and integer rating. Save changes that same record; Back discards the draft. Changing an address clears both stale static map images.
- Delete requires confirmation. Cancel retains the record; confirmation removes only that restaurant and updates the List/count.
- Notes Save and Cancel update or preserve the selected record. Notes and photo references/captions survive saved UI state restoration, without crossing between restaurants.
- Cover and thumbnails open a dark photo viewer with a count, Close, swiping, wraparound previous/next arrows and thumbnail selection. Missing or revoked photo references show a placeholder without crashing.
- The + photo tile opens a photo/caption dialog connected to Android's image picker. Loading is off the main thread, bounds decoded images and respects orientation. An actual system-picker selection round trip remains unverified.
- View full map opens the selected restaurant's portrait map, saved address, Back and Get directions. The directions URL targets the selected name/address. The previews are static screenshots; pictured Google controls are illustrative. Live Maps SDK, geocoding, current location and permission workflows are not implemented.
- Share by email creates an `ACTION_SENDTO` email-draft intent with name, address, phone number(s), rating, tags and description. A missing email handler gives a copyable, scrollable preview. No email is sent automatically. A real external email client's draft composition remains unverified.

Records remain session data retained by saved UI state, not durable Room storage. Existing Add and About remain available; the supporting Edit form does not complete the full shared Add/Edit wireframe.

## Restaurant and asset sources

The researched contacts, menu descriptions and tags use the sources in [demo-restaurant-research.md](demo-restaurant-research.md). The three original cover files remain unchanged; their provenance is in [restaurant-list-photo-sources.md](restaurant-list-photo-sources.md).

Two Brothers also uses the existing Figma gallery originals from its [official menu](https://twobrothersshawarma.ca/menu.html): [chicken shawarma](https://twobrothersshawarma.ca/images/py1_0531251x167.jpg?crc=18501946), [falafel](https://twobrothersshawarma.ca/images/py1_0536250x167.jpg?crc=3880655379) and [baklava](https://twobrothersshawarma.ca/images/baklava250x167.jpg?crc=4293132223). The 250×167 menu images and student meal original are illustrative demo seeds, not evidence of a personal visit. The previously collected Midyeci/La Unica website food photos remain reference-only outside the app; their existing supplied storefront images seed their galleries.

Each restaurant has separate embedded and portrait Google map captures with its selected pin and Google attribution retained. Two Brothers' embedded map is the unchanged original October 2 map-only capture used in Figma. The other five map-only captures were made October 10. None was extracted from a wireframe export, synthesized, or stretched. Map sources: [Two Brothers — Lake Shore](https://www.google.com/maps/place/Two+Brothers+Shawarma/@43.6172762,-79.4877795,17z/data=!3m1!4b1!4m6!3m5!1s0x882b3729700d7e15:0xa05a056e52c3859f!8m2!3d43.6172762!4d-79.4877795!16s%2Fg%2F11smnm5rc3), [Midyeci Riza](https://www.google.com/maps/place/Midyeci+Riza+by+Prince+of+Mussels/@43.7300443,-79.4710286,17z/data=!3m1!4b1!4m6!3m5!1s0x882b31867a95576f:0x1082bb3ec78b0500!8m2!3d43.7300443!4d-79.4710286!16s%2Fg%2F11vwzjhyt4), [La Unica — Wilson](https://www.google.com/maps/place/La+%C3%9Anica+Panader%C3%ADa/@43.730735,-79.4677359,17z/data=!3m1!4b1!4m6!3m5!1s0x882b330b69afd299:0x6a4a531911bca2ac!8m2!3d43.730735!4d-79.4677359!16s%2Fg%2F11vkdp66zw).

The dated [asset verification manifest](../../../outputs/2026-10-10-restaurant-details/asset-verification.json) records dimensions, original sources and SHA-256 identity for all nine newly bundled raster files. The six map originals and adjacent source/cleanup receipts are retained in the dated outputs. Icons use native vector assets; AndroidX ExifInterface 1.4.2 is the only added dependency.

## Verification results

The final debug app/test APK build, unit tests and lint passed using the existing Android Studio JBR and Gradle wrapper:

```sh
./gradlew --no-daemon --max-workers=2 :app:assembleDebug :app:assembleDebugAndroidTest :app:testDebugUnitTest :app:lintDebug
```

- **13 unit tests passed:** nine existing List/filter tests and four email-body tests covering all required fields, multiple phone text, selected-record data, Unicode/multiline values and blank fallbacks.
- **10 Compose device tests passed:** four List regressions and six Details tests. These cover selected-record contacts, read-only tag semantics, Notes Save/Cancel/restoration, Delete Cancel/confirm, Edit Cancel/Save, photo swipe/arrow wrapping/Close, caption cancellation, missing photo restoration, selected map/Back, and email intent/fallback fields with preserved List search/count.
- **Lint: zero errors, two warnings and one hint.** Warnings are the existing tooling update and missing app icon; the hint suggests specialized integer state for the gallery's selected index. No logo was added.
- Actual top/bottom captures for all three restaurants, Delete, gallery, map and email preview were visually reviewed against the retained design. MainActivity resumed after production launch; saved logcat contains no fatal exception or application ANR.

The device run used a task-owned, read-only Pixel_7/API 37.1 lightweight fixture: 1.5GB RAM, two cores, 412×940dp display, guest network/animations and nonessential Google background packages disabled. It establishes prototype behavior in that fixture; minimum API, physical gestures, stock-device startup, real external email composition, actual photo-picker selection, live directions/GPS permissions, zoom and durable storage were not verified.

The task-owned emulator was stopped immediately in `finally`. Serial `emulator-5558` and PID 62821 are absent. Task-owned map browser tabs were closed and temporary viewport settings reset; student apps/tabs and the shared adb server were preserved.

Evidence: [build receipt](../../../outputs/2026-10-10-restaurant-details/build-receipt.json), [device test output](../../../outputs/2026-10-10-restaurant-details/instrumentation.txt), [runtime/cleanup receipt](../../../outputs/2026-10-10-restaurant-details/runtime-receipt.json), [Two Brothers top](../../../outputs/2026-10-10-restaurant-details/two-brothers-details-top.png), [Two Brothers lower section](../../../outputs/2026-10-10-restaurant-details/two-brothers-details-bottom.png), [Midyeci](../../../outputs/2026-10-10-restaurant-details/midyeci-details-top.png), [La Unica](../../../outputs/2026-10-10-restaurant-details/la-unica-details-top.png), [gallery](../../../outputs/2026-10-10-restaurant-details/photo-gallery.png), [full map](../../../outputs/2026-10-10-restaurant-details/la-unica-full-map.png).

## Toolbar spacing follow-up

The student requested less unused white space above the Details content. The shared toolbar now measures 48dp instead of 66dp, bringing every restaurant's cover 18dp higher while retaining 48dp action targets and the single native status/camera inset. No navigation or record behavior changed. The earlier test activity used white status icons against white; the actual MainActivity uses dark icons, so production captures represent that area accurately.

Fresh debug app/test builds and two existing device tests passed for this change, covering Notes Save/Cancel/restoration, all three selected records and Back/scroll preservation. The prior full 13-unit/10-device suite above was not rerun for this layout-only adjustment. All three new top captures and the actual production Details view were visually reviewed. The task-owned emulator was stopped immediately; PID 63739 and serial emulator-5558 are absent. See [updated production preview](../../../outputs/2026-10-10-details-spacing/production-details.png), [build receipt](../../../outputs/2026-10-10-details-spacing/build-receipt.json) and [runtime/cleanup receipt](../../../outputs/2026-10-10-details-spacing/runtime-receipt.json).
