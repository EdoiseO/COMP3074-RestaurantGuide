# Restaurant Guide

COMP3074 Mobile Application Development I · Fall 2026 · Group P12.

A Kotlin and Jetpack Compose Android app for saving restaurant details, finding saved restaurants and revisiting favourite places. The planned architecture is MVVM with Room/SQLite local storage and Google Maps integration.

## Current state

The Android prototype implements the purple Splash Screen and Restaurant List with scrollable rounded cards, name search, multiple selected tags, All reset, result counts, Add/About entries and distinct empty/no-results states. Back from a selected restaurant retains query, filters and scroll. A basic Add form creates session records; Details displays the selected record and About includes the approved team. These destinations support the List demo; full Details and shared Add/Edit workflows remain later work.

Splash startup verification passed previously. The refined List debug build, nine unit tests and four Compose device tests passed; lint reports zero errors and three warnings. See [List verification](docs/restaurant-list-verification.md). All three original photos are bundled; the circular Add button, search field, compact heading, purple colors and saved count wording follow the current Figma Home design. Live search, tag filters and Back state remain working. Demo data and changes are held in saved UI state; durable storage, full CRUD, maps/directions, gallery, notes and email handoff remain to implement. The current Figma/PNG wireframes are the visual reference.

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
