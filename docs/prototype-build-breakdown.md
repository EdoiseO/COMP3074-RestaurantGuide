# Restaurant Guide — UI prototype build sections

Prepared October 10, 2026 for team P12. Screen names below retain the wording in [requirements.md](requirements.md). Phillip Onofua will build the UI prototype alone, as requested on October 10. This changes prototype build ownership; the approved proposal's team names and broader roles remain in [development-plan.md](development-plan.md).

Based on the 24 current [wireframe PNGs](wireframes/), the October 10 Figma inventory and the current Android implementation. Splash Screen has a purple background, centered white application name and an automatic transition to Restaurant List; debug build/lint and API 37 emulator visual verification passed. Restaurant List includes scrolling cards, live name search, multi-tag filtering, result counts, All reset and distinct empty/no-results states. Selected-record Restaurant Details follows the retained October 7 visual reference, with contacts and supporting actions for all three restaurants. The current debug build, 13 unit tests and 10 API 37.1 device tests passed; see [Details verification](restaurant-details-verification.md). The basic Add/About entries remain available, while the full shared Add/Edit design remains later work. Filter combinations and gallery images are states of reusable screens, not separate app screens.

## Screen sections

| Section from the proposal | Components and prototype behavior to build | Current design reference | Requirement coverage |
| --- | --- | --- | --- |
| **Splash Screen** | Purple background and centered white application name; automatic transition to Restaurant List. No logo is needed for the current prototype. | Android layout and launch theme implemented; debug build/lint and API 37 startup visual verification passed. No named Figma frame in the current inventory. | FR-17 |
| **Restaurant List** | Scrollable cards, name search, selected tag filters, All reset, result count, Add button and About entry. Tapping a card opens that restaurant's details. Preserve search/filter state after Back. Include empty-list and no-results states. | `01-home-*`, `01-filter-*`, `02-search-results.png`, `restaurant-card.png`; Android empty/no-results states implemented. Debug build, nine unit tests and four API 37.1 Compose device tests passed. | FR-01, FR-00, FR-10, FR-22 |
| **Restaurant Details** | Cover image and compact white rating badge; name, description, read-only tags, My photos, address, phone number(s), embedded map, View full map, Share by email and Notes. Back, basic Edit and confirmed session Delete are wired; every card uses its own record. | `03-restaurant-description.png`; Android implementation visually reviewed for all three records; Details behavior and List regressions passed. Static map preview is not live FR-11 Maps integration. | FR-03, FR-06, FR-11, FR-16 |
| **Add Restaurant** | Empty shared form: cover/photo controls, required name, description, rating, custom tags, address, phone number(s), visit notes and Save. Add/remove tags; demonstrate address assistance and multiple numbers. Valid Save adds a record to the demo list. | `07-add-restaurant.png`, `08-name-required.png`, `10-rating-picker.png`; tag entry, address/phone variants and successful save need representation. | FR-02, FR-06, FR-07, FR-08, FR-23 |
| **Edit Restaurant** | Basic prefilled name, description, tags, address, phone and integer rating edits save to the session record; Back discards drafts. Later reuse the full shared Add/Edit form and include the requested drag directly across enlarged stars. | Basic supporting form Save/Cancel tested. `06-edit-restaurant.png` and shared rating/photo references remain the full form target. | FR-04, FR-07, FR-23 |
| **Full-Screen Map** | Selected static map preview, saved address, Back and Get directions URL handoff, reached from Details. Live maps, current location, permission denial and unavailable-location feedback remain later work. | Basic Android destination visually reviewed; selected address, Back and directions URL tested. No named frame in the current inventory; final design and live-location supporting states needed. | Both source FR-13 rows, FR-14, FR-15 |
| **About** | Application/project information, names of all four members and Back. Reached from Restaurant List navigation. | Basic Android About implemented; no named Figma frame in the current inventory. | FR-19 |
| **Confirmation / Dialogs** | Compact centered Delete confirmation with Cancel/Delete, required-name feedback, quick notes and add-photo/caption dialogs. Confirmed deletion updates the session list. Broader invalid-field and location permission/error states remain later work. | `03-delete-confirmation.png`, `08-name-required.png`; Delete, Notes Save/Cancel and photo-dialog cancellation tested. Picker round trip remains unverified. | FR-05, FR-15, FR-23 |

The source repeats FR-13. Both requirements are mapped above without renumbering them.

## Supporting components from the current wireframe

Build these once and reuse them across the sections:

- **Restaurant card:** image, name, summary and rating; light grey/lavender 1px outline and 24px corners.
- **Tags:** selectable chips on Restaurant List; read-only labels on Restaurant Details; editable/addable/removable tags in Add/Edit. Use the current Shawarma, Turkish and Mexican sample categories. Match any selected category, as in the current demo; record this behavior in the implementation decisions.
- **Form fields and buttons:** existing purple styling, rounded inputs (12px reference corners), compact spacing and clear labels. Preserve the 16px gap between the photo strip and Description.
- **Rating picker:** the stars themselves respond to finger dragging, with enlarged targets, a numeric value and Save/Cancel. Preserve the user's half-star design intent. Resolve the proposal's integer `rating:Int` conflict before finalizing the stored model; do not present the fixed Figma illustration as a working slider.
- **Photos:** My photos follows tags and precedes address, with square thumbnails and + photo. The dark Android gallery includes title/count, Close, swiping, previous/next and thumbnail selection; zoom is not implemented. Add-photo includes a system photo picker and optional caption. The picker device round trip is unverified; the fixed Figma states do not establish photo picking or storage.
- **Visit notes:** quick edit from the Notes pencil saves/cancels a session draft. The full shared Add/Edit form will also include visit notes. Notes describe the restaurant visit; a photo caption belongs to one image.
- **Map/email actions:** compact purple buttons below the embedded map and above Notes. View full map opens the selected static preview with Back and Get directions. Email attempts an email-app draft containing name, address, phone number(s), rating, tags and description, with a copyable fallback when no email app is available. Selected-record map navigation, directions URL and email intent/fallback passed device checks; external email composition remains unverified.

Photos, captions and visit notes are user-requested additions. Keep them in this prototype plan and reconcile them with the proposal/data model before persistence work; they are not original FR requirements.

## Solo prototype build sequence

**Phillip Onofua owns every UI prototype section.** Build one working path at a time so each new screen can use the same navigation and demo records.

| Step | Prototype work | Current state |
| --- | --- | --- |
| 1 | Splash Screen: purple launch background, white application name and transition to Restaurant List. | Implemented; debug build/lint and API 37 startup visual verification passed. |
| 2 | Shared sample records and navigation; Restaurant List/cards, typed search, tag filtering, All reset and empty/no-results states. | Implemented in memory; debug build, nine unit tests and four API 37.1 Compose device tests passed. |
| 3 | Restaurant Details with its selected record, contacts, read-only tags, photos, Notes and actions. | Implemented and visually reviewed for all three records; debug build, 13 unit tests and 10 device tests passed. |
| 4 | Shared Add/Edit form, custom tags, required-field validation, draggable rating, Save/Cancel and Delete confirmation. | Basic prefilled integer-rating Edit and confirmed session Delete tested. Full shared form and draggable fractional rating remain planned. |
| 5 | Photo gallery/add-photo UI, visit notes, Full-Screen Map/directions states, email handoff and About. | Gallery, quick notes, selected static map and email intent/fallback tested. Photo picker/caption API wired; Basic About already implemented. Zoom, actual picker selection, external email composition and live location states remain. |
| 6 | Test the complete demo journey, then prepare the required design/application-purpose video and repository link. | Planned. |

The official project team remains **Kalid Ali, Phillip Onofua, Mohamed Amine OUATAR and Joshua Zaakir**. Their approved proposal responsibilities are preserved in [development-plan.md](development-plan.md); the solo sequence above does not reassign the broader project roles. About still includes all four names.

## Build order and prototype boundary

1. **Launch and List:** verify the purple Splash Screen and transition to Restaurant List, search/filter/reset/count behavior, both empty states and Back-state preservation.
2. **Foundation and main path:** define shared fields/callbacks, sample records, routes and reusable styling, then build Restaurant List → Restaurant Details → Edit Restaurant, plus Restaurant List → Add Restaurant. Search/filter and Save/Delete should update the same in-memory records during the demo. Preserve user-requested photos/notes and half-star gesture; record unresolved storage choices.
3. **Supporting paths:** photos, notes, rating, Full-Screen Map/directions, email and About. Add missing Map/About designs before building their final layouts.
4. **Review:** check scrolling, keyboard behavior, compact spacing, Back/Cancel, validation and the complete journey below.

For the UI prototype, use sample records and session-only changes to demonstrate interactions. Current Details uses static map previews and a Get directions URL handoff, with no live geocoding, current-location retrieval or permission flow. Mark simulated map behavior in the team's demonstration/documentation. Email-app draft handoff and system photo picker APIs are wired; external email composition and a picker round trip require device verification. Saved UI state is not durable persistence.

The later functional prototype must add durable storage and persistent CRUD, complete device-photo verification/storage, live maps/address resolution, current-location permission handling and verified directions/email-app handoffs. FR-15 must prevent incorrect directions requests when permission is denied or location is unavailable. No optional Facebook/Twitter features are needed for this build section plan.

## Demo completion check

- Launch → Splash Screen → Restaurant List.
- Search by name; select multiple tags; reset with All; demonstrate no results.
- Open a restaurant → scroll complete details → Back preserves the list selection/query.
- Add a restaurant: blank-name warning first, then a successful Save visible in the list.
- Edit its description/rating/tags → Save → updated details; Cancel leaves the saved record unchanged.
- Open a photo, change the selected image and close; add a photo/caption and edit visit notes according to the declared demo boundary.
- View full map → restaurant marker → Back; show successful directions and denied/unavailable-location states.
- Share by email → all specified restaurant fields represented in the email handoff or declared preview.
- Cancel deletion once; confirm deletion once; list/count updates.
- Open About → all four names → Back; demonstrate an empty list separately.

The Splash → List launch path was verified previously. The current debug build, 13 unit tests and 10 API 37.1 Compose device tests passed, covering List regressions and the implemented Details actions. See [List verification](restaurant-list-verification.md) and [Details verification](restaurant-details-verification.md). The journey above remains the complete demo acceptance checklist. Full shared Add/Edit, draggable fractional rating, zoom, actual picker selection, external email composition, durable storage and live map/location permission workflows remain planned. The UI prototype brief separately requires the GitHub repository link and a video explaining the design and application purpose. This document update makes no publication or school-submission claim.

## Restaurant List implementation checkpoint

The Android prototype uses three sample records, integer ratings from the proposal and any-selected-tag matching combined with name search. All clears selected tags; Clear search clears the name independently. Query, selected tags, scroll and session records are retained across navigation/recreation. Add has a basic name/description/tags/whole-star form for demonstrating a new List record; it is not the full Add/Edit wireframe. About shows all four approved names, and each card opens its own read-only record.

All three original photos are bundled: the user-supplied Two Brothers meal and the original Midyeci Riza/La Unica storefront uploads recovered from Figma with exact source-byte identity. Storefront crops preserve their signs. Whole wireframe screenshots are not app assets. Sample photos are illustrative, not evidence of a personal visit. Records are session-only; Room storage remains separate work.

## Restaurant Details implementation checkpoint

Details uses each selected record's researched description, tags, contacts and original cover. The retained October 7 PNG and native layout records supplied its visual target because the Figma connector remains call-limited; no Figma design was changed. Tags display as read-only outlined labels, while List filters remain interactive. Notes, basic edits, confirmed deletion, added photo references and captions update the same session record and survive saved-state restoration.

The dark gallery supports arrows, swiping and thumbnail selection, without zoom. Add-photo is connected to the Android system photo picker and optional caption field; a picker round trip has not been verified. Map previews are static and belong to the selected restaurant when source images are available; otherwise the UI labels an illustrative preview. Changing an address clears its stale static map. The internal map offers a Get directions Google Maps URL, without claiming live Maps SDK, geocoding, app-obtained current location or permission handling. Email attempts an email-app draft with the required fields and presents a copyable preview if no app is available. Basic Edit retains integer ratings; the full shared form and draggable fractional-star picker remain later sections.

**Verification passed for this scope:** final debug app/test builds, 13 unit tests, 10 Compose device tests and scoped visual review; lint has zero errors, two existing warnings and one hint. [Details verification](restaurant-details-verification.md) records captures, source evidence, tested actions and the remaining integration limits. The task-owned emulator was stopped immediately after testing.
