# Restaurant Guide

COMP3074 Mobile Application Development I · Fall 2026 · Team P12.

A Kotlin and Jetpack Compose Android prototype for saving restaurant details, finding saved restaurants and remembering meals, ratings, photos and visit notes.

## Prototype features

- Purple Splash Screen and a Restaurant List with live name search, multiple tag filters, result counts and empty/no-results states. Back preserves search, filters and scroll.
- Selected Restaurant Details with photos, contacts, read-only tags, notes, a map preview and email sharing controls.
- Shared Add/Edit form with validation, custom tags, multiple phone numbers, address assistance, photos/captions and swipeable half-star ratings. Form Save applies the draft; Back discards it. Rating Save updates only the form draft.
- My photos keeps **+ photo** beside short albums and left-aligned when empty. Longer albums scroll with Add visible. The gallery supports swiping, thumbnails and 100–300% zoom/panning.
- Confirmed deletion, Notes Save/Cancel, Full-Screen Map and About with all four team members.

Records and photo references use session data and saved UI state. Durable Room storage and persistent photo access remain future work. Maps are static previews with a directions URL; live Maps, geocoding and current-location permissions are not implemented. Email prepares a draft intent and provides a copyable fallback; successful external email/navigation handoffs remain to be verified.

## Open and build

1. Clone the repository and open it in Android Studio.
2. Use JDK 17 or newer and install Android SDK Platform 37. Minimum Android version: API 24.
3. Allow Gradle sync, then run the `app` configuration on a device or emulator.

```sh
git clone https://github.com/EdoiseO/COMP3074-RestaurantGuide.git
cd COMP3074-RestaurantGuide
./gradlew :app:assembleDebug :app:lintDebug
```

Run unit tests with `./gradlew :app:testDebugUnitTest` and device tests with `./gradlew :app:connectedDebugAndroidTest` while a device is connected. Stop the emulator when finished. Keep local SDK paths, credentials, signing keys and generated files out of Git.

## Project references

- [Requirements and application documentation](docs/README.md)
- [Screen sections and prototype behavior](docs/prototype-build-breakdown.md)
- [Implementation decisions](docs/design-decisions.md)
- [Team development plan](docs/development-plan.md) and [contribution guide](CONTRIBUTING.md)
- [Figma design](https://www.figma.com/design/8Z8T16iL2SBTzZm6pUShAA) and [wireframe exports](docs/wireframes/)
- [Demo restaurant sources](docs/demo-restaurant-research.md) and [photo/map sources](docs/restaurant-list-photo-sources.md)
- [Silent demo video and timed recording script](docs/demo/README.md)

Phillip Onofua is building the UI prototype alone; the approved project team and broader responsibilities remain in the development plan. The UI prototype brief requires the GitHub repository link and a video explaining the design and application purpose.
