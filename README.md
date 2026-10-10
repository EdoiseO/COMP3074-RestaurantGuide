# Restaurant Guide

COMP3074 Mobile Application Development I · Fall 2026 · Group P12.

A Kotlin and Jetpack Compose Android app for saving restaurant details, finding saved restaurants and revisiting favourite places. The planned architecture is MVVM with Room/SQLite local storage and Google Maps integration.

## Current state

The Android prototype implements the purple Splash Screen, Restaurant List and selected-record Restaurant Details. The List has scrollable rounded cards, live name search, multiple selected tags, All reset, result counts, Add/About entries and distinct empty/no-results states. Back from Details retains query, filters and scroll. Details uses each restaurant's cover, description, personal rating, read-only tags, contacts, photos, map preview and notes. Back, a basic prefilled Edit form, confirmed session deletion, quick notes, photo viewing, map navigation and email draft sharing are wired. A basic Add form creates session records, and About includes the approved team.

Splash startup verification passed previously. The current debug app/test build, **13 unit tests and 10 Compose device tests passed**; lint reports zero errors, two existing warnings and one integer-state hint. See [List verification](docs/restaurant-list-verification.md) and [Details verification](docs/restaurant-details-verification.md) for scope, captures and device limits. All three original photos are bundled; the List's circular Add button, search field, compact heading, purple colors and saved count wording follow the current Figma Home design. Details follows the retained October 7 PNG and native property records because the Figma connector is call-limited; no Figma design edits were made.

Records, notes, chosen photo references and captions are retained in saved UI state, including recreation; these are session changes, not durable Room storage. The dark gallery supports swiping, arrows and thumbnail selection, without zoom. The system photo picker is wired, but its device picker round trip has not been verified. The Full-Screen Map displays a selected static map preview and a Get directions URL handoff; it does not implement live Maps SDK, geocoding, current-location retrieval or permission handling. Email attempts to open an email-app draft with all requested restaurant fields and offers a copyable preview when no app is available. The full shared Add/Edit design, draggable fractional stars, multiple-phone controls, durable storage and live location services remain later work.

Midyeci Riza and La Unica’s Wilson entries include researched addresses, phone numbers, menu descriptions and food tags. See [verified sources and photo references](docs/demo-restaurant-research.md). Their existing stars are sample personal ratings.

**Phillip Onofua is building the UI prototype alone.** Follow the [solo prototype build sequence](docs/prototype-build-breakdown.md). The approved project team and broader responsibilities remain unchanged in [development-plan.md](docs/development-plan.md).

- [Figma design](https://www.figma.com/design/8Z8T16iL2SBTzZm6pUShAA)
- [Interactive Figma demo](https://www.figma.com/proto/8Z8T16iL2SBTzZm6pUShAA?starting-point-node-id=8%3A2882)
- [Approved proposal](docs/approved-proposal.docx)
- [Requirements and design documentation](docs/README.md)
- [Wireframe PNGs](docs/wireframes)
- [Working together](CONTRIBUTING.md)

## Open and build

1. Clone this repository after accepting your GitHub invitation.
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

The UI prototype milestone is due October 11, 2026 at 23:59 Toronto. Its saved official brief requires a GitHub repository link and a video explaining the design and application purpose. Creating this repository does not complete that milestone or submit anything to Brightspace.

Resolve the documented rating and phone-field decisions before building those features. Photos and visit notes are later design additions; see the design-decision record for how they relate to the proposal.
