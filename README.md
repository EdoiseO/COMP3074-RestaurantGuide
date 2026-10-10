# Restaurant Guide

COMP3074 Mobile Application Development I · Fall 2026 · Group P12.

A Kotlin and Jetpack Compose Android app for saving restaurant details, finding saved restaurants and revisiting favourite places. The planned architecture is MVVM with Room/SQLite local storage and Google Maps integration.

## Current state

The Android prototype implements the purple Splash Screen, Restaurant List, selected-record Restaurant Details and a shared Add/Edit form. The List has scrollable rounded cards, live name search, multiple selected tags, All reset, result counts, Add/About entries and distinct empty/no-results states. Back from Details retains query, filters and scroll. Details uses each restaurant's cover, description, personal rating, read-only tags, contacts, photos, map preview and notes. Confirmed session deletion, quick notes, photo viewing, map navigation and email draft sharing are wired. About uses a compact purple toolbar, rounded information/team panels and scrollable content with all four approved names; toolbar and hardware Back preserve the List state.

The shared form follows retained Figma Add **52:213** and Edit **50:166**, with their existing wording, purple styling, rounded inputs, floating labels and 16dp photo-to-description gap. Add starts empty; Edit pre-fills the selected record. Cover replacement/removal, album add/remove, chosen-photo caption editing, custom tags and visit notes belong to a draft: form Save updates the session record, while Back discards unsaved changes. Drag directly across enlarged stars to choose half-star values; rating-dialog Save applies to the form draft and Cancel preserves its prior rating. Separate address entry/formatting assistance and a multiple-phone control are native extensions to the saved wireframe.

The earlier shared-form checkpoint passed **32 unit tests and 18 Compose device tests**, with **zero lint errors**. Its device run includes the real Android photo picker, returned-image byte identity, caption Save/restoration and existing List/Details regressions. See [Add/Edit verification](docs/restaurant-form-verification.md) for captures and fixture limits; [List verification](docs/restaurant-list-verification.md) and [Details verification](docs/restaurant-details-verification.md) retain the earlier checkpoints. All three original photos are bundled; the List and Details use retained Figma exports/native properties because the connector is call-limited. No Figma design edits were made.

Ratings now use `Float` for the user's requested half-star values. Saved UI state uses `restaurants-v3`, restoring the earlier integer-rating v2 and legacy snapshots as fractional records. Records, covers, notes, chosen photo references and captions survive saved UI state restoration; this is not durable Room storage. The dark gallery supports swiping, arrows, thumbnails and 100–300% zoom through pinch or −/percentage/+ controls. Enlarged photos support bounded panning; changing photos resets zoom, and the percentage control restores fit. Bundled and chosen URI photos share this behavior. A real system-picker selection, decoding, Save and caption restoration passed device verification with returned-image byte identity checked. This verifies the session workflow, not durable photo access. The Full-Screen Map displays a selected static map preview and a Get directions URL handoff; it does not implement live Maps SDK, geocoding, current-location retrieval or permission handling. Email attempts an email-app draft with all requested restaurant fields and offers a copyable preview when no app is available. Real external email composition, durable storage and live location services remain later work.

The Full-Screen Map now has a compact native toolbar, a rounded selected map and an address panel with Get directions kept outside its text scroller. Fresh app/test builds, lint and **seven scoped device checks passed**, including all three records, edited/missing addresses, a small window and captured directions intent/error feedback. See [Map verification](docs/restaurant-map-verification.md) for screenshots and integration boundaries. No named Figma map frame was retained; this is a native layout using existing app styling and wording.

The latest About/gallery-zoom audit passed fresh debug app/test builds, **32 unit tests and all 33 Compose device tests**. Lint reports **zero errors, three warnings and three hints**; the new warning is a benign plural suggestion for the zoom percentage description. The suite covers the connected List/Details/form/map/photo workflows, About Back/state preservation, simulated small-window/large-text About scrolling and gallery zoom/pan/reset/error behavior. No physical-device or minimum-API verification is claimed. [Final prototype review](docs/prototype-final-verification.md) tracks the recording and publication evidence separately from these passing tests. About and zoom use existing native styling and the supplied viewer reference; no named retained Figma frame or fresh Figma edit is claimed.

The [completed demo package](docs/demo/README.md) includes the **4:58 design/application-purpose video**, separate transcript, About/gallery previews and verification receipt. The video uses actual production Android footage with chapter explanations and is silent, without on-screen transcript captions. The task-owned emulator is stopped.

Midyeci Riza and La Unica’s Wilson entries include researched addresses, phone numbers, menu descriptions and food tags. See [verified sources and photo references](docs/demo-restaurant-research.md). Their existing stars are sample personal ratings.

**Phillip Onofua is building the UI prototype alone.** Follow the [solo prototype build sequence](docs/prototype-build-breakdown.md). The approved project team and broader responsibilities remain unchanged in [development-plan.md](docs/development-plan.md).

- [Figma design](https://www.figma.com/design/8Z8T16iL2SBTzZm6pUShAA)
- [Interactive Figma demo](https://www.figma.com/proto/8Z8T16iL2SBTzZm6pUShAA?starting-point-node-id=8%3A2882)
- [Approved proposal](docs/approved-proposal.docx)
- [Requirements and design documentation](docs/README.md)
- [Wireframe PNGs](docs/wireframes)
- [Working together](CONTRIBUTING.md)

## Open and build

1. Clone this public repository. A GitHub invitation is needed for collaborator write access.
2. Open the repository folder in Android Studio and allow Gradle sync to finish.
3. Use JDK 17 or newer and install Android SDK Platform 37. The minimum supported Android version is API 24.
4. Run the `app` configuration on an emulator or Android device.

```sh
git clone https://github.com/EdoiseO/COMP3074-RestaurantGuide.git
cd COMP3074-RestaurantGuide
./gradlew :app:assembleDebug :app:lintDebug
```

The Gradle wrapper, AGP 9.4.1, Kotlin Compose plugin 2.2.10 and Compose BOM 2026.02.01 are pinned to the locally installed course-project tooling. Keep `local.properties`, API keys, signing keys, IDE metadata and generated build files out of Git. Google Maps keys have not been added.

## UI prototype milestone

The UI prototype milestone is due October 11, 2026 at 23:59 Toronto. Its saved official brief requires a GitHub repository link and a video explaining the design and application purpose. The completed demo package supplies those deliverables; submitting them to Brightspace is a separate step.

The user's explicit requests authorize half-star dragging, photos, captions and visit notes in this prototype. These implementation decisions do not rewrite the approved proposal's integer-rating requirement; see [design decisions](docs/design-decisions.md). Final runtime evidence and the remaining functional-project work are tracked separately from the UI build.
