# Restaurant Guide

COMP3074 Mobile Application Development I · Fall 2026 · Team P12.

Android UI prototype built with Kotlin and Jetpack Compose. Save restaurant details, search and filter your list, view photos, add visit notes and choose half-star ratings.

## Run the app

1. Clone this repository and open it in Android Studio.
2. Install Android SDK Platform 37 and use JDK 17 or newer.
3. Sync Gradle, then run `app` on a device or emulator (Android API 24 or newer).

```sh
git clone https://github.com/EdoiseO/COMP3074-RestaurantGuide.git
cd COMP3074-RestaurantGuide
./gradlew :app:assembleDebug :app:lintDebug
```

Unit tests: `./gradlew :app:testDebugUnitTest`. Device tests: `./gradlew :app:connectedDebugAndroidTest`. Stop the emulator when finished.

## Prototype status

Includes Splash, Restaurant List, Details, Add/Edit, photo gallery with zoom, visit notes, a map preview, email sharing and About. Back preserves list filters; form Save applies changes and Back discards them.

Records and photo references currently use session data and saved UI state. Durable storage and persistent photo access remain future work. Maps use static previews with a directions URL; live maps, geocoding and current-location permissions are not implemented. Email prepares a draft intent with a copyable fallback; successful external email/navigation handoffs remain to be verified.

## Project links

- [Figma design](https://www.figma.com/design/8Z8T16iL2SBTzZm6pUShAA)
- [Final presentation](https://drive.google.com/file/d/1W0jKW0yuCQhGVjLw8sTrYKg9-VvY8BYh/view)
- [Project design proposal](https://docs.google.com/document/d/1NKr3KEbKyUrFnKXlOMboBQbfbuzSqtX0LajzcLMMMd0/edit)

Team: Kalid Ali, Phillip Onofua, Mohamed Amine OUATAR and Joshua Zaakir.

## Demo data and asset sources

Restaurant cover/storefront photos were supplied for the prototype. Gallery images are illustrative demo seeds, not evidence of personal visits. Ratings are sample personal ratings, not public review scores. Restaurant details were checked on October 10, 2026.

Two Brothers gallery photos come from its [official menu](https://twobrothersshawarma.ca/menu.html): [chicken shawarma](https://twobrothersshawarma.ca/images/py1_0531251x167.jpg?crc=18501946), [falafel](https://twobrothersshawarma.ca/images/py1_0536250x167.jpg?crc=3880655379), [baklava](https://twobrothersshawarma.ca/images/baklava250x167.jpg?crc=4293132223).

Static map captures retain Google attribution. Sources: [Two Brothers — Lake Shore](https://www.google.com/maps/place/Two+Brothers+Shawarma/@43.6172762,-79.4877795,17z/data=!3m1!4b1!4m6!3m5!1s0x882b3729700d7e15:0xa05a056e52c3859f!8m2!3d43.6172762!4d-79.4877795!16s%2Fg%2F11smnm5rc3), [Midyeci Riza](https://www.google.com/maps/place/Midyeci+Riza+by+Prince+of+Mussels/@43.7300443,-79.4710286,17z/data=!3m1!4b1!4m6!3m5!1s0x882b31867a95576f:0x1082bb3ec78b0500!8m2!3d43.7300443!4d-79.4710286!16s%2Fg%2F11vwzjhyt4), [La Unica — Wilson](https://www.google.com/maps/place/La+%C3%9Anica+Panader%C3%ADa/@43.730735,-79.4677359,17z/data=!3m1!4b1!4m6!3m5!1s0x882b330b69afd299:0x6a4a531911bca2ac!8m2!3d43.730735!4d-79.4677359!16s%2Fg%2F11vkdp66zw).

Other demo details: [Midyeci Riza contact](https://midyeciriza.com/contact/) and [menu](https://midyeciriza.com/menu/); [La Unica locations](https://launica.ca/), [Wilson menu](https://launica.ca/wp-content/uploads/2026/04/Menu-La-Unica-Wilson.pdf) and [Wilson ordering page](https://www.ubereats.com/store/la-unica-panaderia-%C2%B7-wilson/wyHgx-N6UwurWSBfJJpprg). Official Midyeci/La Unica website food photographs are not bundled.
