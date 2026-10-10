# Restaurant Guide — UI prototype build sections

Prepared October 10, 2026 for team P12. Screen names below retain the wording in [requirements.md](requirements.md). Phillip Onofua will build the UI prototype alone, as requested on October 10. This changes prototype build ownership; the approved proposal's team names and broader roles remain in [development-plan.md](development-plan.md).

Based on the 24 current [wireframe PNGs](wireframes/), the October 10 Figma inventory and the current Android implementation. Splash Screen now has a purple background, centered white application name and an automatic transition to Restaurant List; debug build/lint and API 37 emulator visual verification passed. Restaurant List now includes scrolling cards, name search, multi-tag filtering, result counts, All reset and distinct empty/no-results states. Its navigation opens a basic selected-record Details view, Add form and About; the full Details and shared Add/Edit designs remain later work. Filter combinations and gallery images are states of reusable screens, not separate app screens.

## Screen sections

| Section from the proposal | Components and prototype behavior to build | Current design reference | Requirement coverage |
| --- | --- | --- | --- |
| **Splash Screen** | Purple background and centered white application name; automatic transition to Restaurant List. No logo is needed for the current prototype. | Android layout and launch theme implemented; debug build/lint and API 37 startup visual verification passed. No named Figma frame in the current inventory. | FR-17 |
| **Restaurant List** | Scrollable cards, name search, selected tag filters, All reset, result count, Add button and About entry. Tapping a card opens that restaurant's details. Preserve search/filter state after Back. Include empty-list and no-results states. | `01-home-*`, `01-filter-*`, `02-search-results.png`, `restaurant-card.png`; Android empty/no-results states implemented. Debug build, nine unit tests and four API 37.1 Compose device tests passed. | FR-01, FR-00, FR-10, FR-22 |
| **Restaurant Details** | Cover image and compact white rating badge; name, description, read-only tags, My photos, address, phone number(s), embedded map, View full map, Share by email and Notes. Wire Back, Edit and Delete. Every card should show its own record. | `03-restaurant-description.png`; map/email buttons already drawn. | FR-03, FR-06, FR-11, FR-16 |
| **Add Restaurant** | Empty shared form: cover/photo controls, required name, description, rating, custom tags, address, phone number(s), visit notes and Save. Add/remove tags; demonstrate address assistance and multiple numbers. Valid Save adds a record to the demo list. | `07-add-restaurant.png`, `08-name-required.png`, `10-rating-picker.png`; tag entry, address/phone variants and successful save need representation. | FR-02, FR-06, FR-07, FR-08, FR-23 |
| **Edit Restaurant** | Reuse the Add form with the selected record prefilled. Save updates that record during the demo; Back/Cancel discards unsaved changes. Include the requested drag directly across enlarged stars. | `06-edit-restaurant.png`, shared rating/photo references. | FR-04, FR-07, FR-23 |
| **Full-Screen Map** | Large map with selected restaurant marker, Back and Get directions. Reached from Details. Represent permission request, denial and unavailable-location feedback; successful directions continue to Google Maps. | No named frame in the current inventory; design and supporting states needed. | Both source FR-13 rows, FR-14, FR-15 |
| **About** | Application/project information, names of all four members and Back. Reached from Restaurant List navigation. | Basic Android About implemented; no named Figma frame in the current inventory. | FR-19 |
| **Confirmation / Dialogs** | Compact centered Delete confirmation with Cancel/Delete; name-required warning; invalid-field feedback; location permission/error messages. Confirmed deletion updates the demo list. | `03-delete-confirmation.png`, `08-name-required.png`; other feedback states needed. | FR-05, FR-15, FR-23 |

The source repeats FR-13. Both requirements are mapped above without renumbering them.

## Supporting components from the current wireframe

Build these once and reuse them across the sections:

- **Restaurant card:** image, name, summary and rating; light grey/lavender 1px outline and 24px corners.
- **Tags:** selectable chips on Restaurant List; read-only labels on Restaurant Details; editable/addable/removable tags in Add/Edit. Use the current Shawarma, Turkish and Mexican sample categories. Match any selected category, as in the current demo; record this behavior in the implementation decisions.
- **Form fields and buttons:** existing purple styling, rounded inputs (12px reference corners), compact spacing and clear labels. Preserve the 16px gap between the photo strip and Description.
- **Rating picker:** the stars themselves respond to finger dragging, with enlarged targets, a numeric value and Save/Cancel. Preserve the user's half-star design intent. Resolve the proposal's integer `rating:Int` conflict before finalizing the stored model; do not present the fixed Figma illustration as a working slider.
- **Photos:** My photos row below tags and above address; small square thumbnails and + photo. Reuse a dark gallery with title/count, Close, previous/next and thumbnail selection. Add-photo UI includes an optional caption about that photo. Gallery swipe/zoom and actual photo picking are separate implementation tasks; they are not established by the current Figma states.
- **Visit notes:** quick edit from the Notes pencil plus the Add/Edit form. Notes describe the restaurant visit; a photo caption belongs to one image.
- **Map/email actions:** compact purple buttons below the embedded map and above Notes. Email should include name, address, phone number(s), rating, tags and description.

Photos, captions and visit notes are user-requested additions. Keep them in this prototype plan and reconcile them with the proposal/data model before persistence work; they are not original FR requirements.

## Solo prototype build sequence

**Phillip Onofua owns every UI prototype section.** Build one working path at a time so each new screen can use the same navigation and demo records.

| Step | Prototype work | Current state |
| --- | --- | --- |
| 1 | Splash Screen: purple launch background, white application name and transition to Restaurant List. | Implemented; debug build/lint and API 37 startup visual verification passed. |
| 2 | Shared sample records and navigation; Restaurant List/cards, typed search, tag filtering, All reset and empty/no-results states. | Implemented in memory; debug build, nine unit tests and four API 37.1 Compose device tests passed. |
| 3 | Restaurant Details with its selected record, contacts, read-only tags, photos, Notes and actions. | Planned. |
| 4 | Shared Add/Edit form, custom tags, required-field validation, draggable rating, Save/Cancel and Delete confirmation. | Planned. |
| 5 | Photo gallery/add-photo UI, visit notes, Full-Screen Map/directions states, email handoff and About. | Gallery, notes, maps and email planned; basic About implemented. |
| 6 | Test the complete demo journey, then prepare the required design/application-purpose video and repository link. | Planned. |

The official project team remains **Kalid Ali, Phillip Onofua, Mohamed Amine OUATAR and Joshua Zaakir**. Their approved proposal responsibilities are preserved in [development-plan.md](development-plan.md); the solo sequence above does not reassign the broader project roles. About still includes all four names.

## Build order and prototype boundary

1. **Launch and List:** verify the purple Splash Screen and transition to Restaurant List, search/filter/reset/count behavior, both empty states and Back-state preservation.
2. **Foundation and main path:** define shared fields/callbacks, sample records, routes and reusable styling, then build Restaurant List → Restaurant Details → Edit Restaurant, plus Restaurant List → Add Restaurant. Search/filter and Save/Delete should update the same in-memory records during the demo. Preserve user-requested photos/notes and half-star gesture; record unresolved storage choices.
3. **Supporting paths:** photos, notes, rating, Full-Screen Map/directions, email and About. Add missing Map/About designs before building their final layouts.
4. **Review:** check scrolling, keyboard behavior, compact spacing, Back/Cancel, validation and the complete journey below.

For the UI prototype, use sample records and session-only changes to demonstrate interactions. A static map preview can demonstrate the layout, with permission/error examples represented as demo states. Mark simulated flows in the team's demonstration/documentation. This does not establish live Google Maps integration, actual location retrieval, email-app handoff, photo picking or persistence.

The later functional prototype must add durable storage and real CRUD, device photo handling, live maps/address resolution, current-location permission handling, directions and email-app handoffs. FR-15 must prevent incorrect directions requests when permission is denied or location is unavailable. No optional Facebook/Twitter features are needed for this build section plan.

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

The Splash → List launch path was verified previously. Restaurant List debug build, nine unit tests and four API 37.1 Compose device tests passed. See [List verification](restaurant-list-verification.md). The complete journey above is the remaining acceptance checklist. Splash Screen is implemented with debug build/lint and API 37 startup visual verification passed; full Details, shared Add/Edit, map, gallery and email workflows remain planned. The UI prototype brief separately requires the GitHub repository link and a video explaining the design and application purpose. This document update makes no publication or school-submission claim.

## Restaurant List implementation checkpoint

The Android prototype uses three sample records, integer ratings from the proposal and any-selected-tag matching combined with name search. All clears selected tags; Clear search clears the name independently. Query, selected tags, scroll and session records are retained across navigation/recreation. Add has a basic name/description/tags/whole-star form for demonstrating a new List record; it is not the full Add/Edit wireframe. About shows all four approved names, and each card opens its own read-only record.

Original Two Brothers photo is bundled. Midyeci Riza and La Unica use neutral image placeholders because their originals are not retained locally and the Figma connector reached its Starter call limit. Whole wireframe screenshots are not app assets. Sample photos are illustrative, not evidence of a personal visit. Records are session-only; Room storage and full restaurant workflows remain separate work.
