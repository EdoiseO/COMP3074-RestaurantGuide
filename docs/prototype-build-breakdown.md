# Restaurant Guide — UI prototype screen sections

Screen names and requirement IDs follow [requirements.md](requirements.md). Phillip Onofua owns the UI prototype; all four members and broader responsibilities remain in the [development plan](development-plan.md). The [wireframes](wireframes/) provide the design references below. Filter combinations and gallery images are states of reusable screens.

| Section from the proposal | Components and prototype behavior to build | Current design reference | Requirement coverage |
| --- | --- | --- | --- |
| **Splash Screen** | Purple background and centered white application name; automatic transition to Restaurant List. No logo is needed for the current prototype. | Native launch theme; no named retained Figma frame. | FR-17 |
| **Restaurant List** | Scrollable cards, name search, selected tag filters, All reset, result count, Add button and About entry. Tapping a card opens that restaurant's details. Preserve search/filter state after Back. Include empty-list and no-results states. | `01-home-*`, `01-filter-*`, `02-search-results.png`, `restaurant-card.png`; native empty/no-results states. | FR-01, FR-00, FR-10, FR-22 |
| **Restaurant Details** | Cover image and compact white rating badge; name, description, read-only tags, My photos, address, phone number(s), embedded map, View full map, Share by email and Notes. Back, shared Edit and confirmed session Delete are wired; every card uses its own record. | `03-restaurant-description.png`; static map previews do not implement live FR-11 Maps integration. | FR-03, FR-06, FR-11, FR-16 |
| **Add Restaurant** | Empty shared form: cover/photo controls, required name, description, draggable half-star rating, custom tags, address, phone number(s), visit notes and Save. Cover/album add-remove, captions, address assistance and multiple numbers are implemented. Valid Save creates a session record visible in the list. | `07-add-restaurant.png` / **52:213**, `08-name-required.png`, `10-rating-picker.png`; address/phone extensions are native. | FR-02, FR-06, FR-07, FR-08, FR-23 |
| **Edit Restaurant** | The same full form pre-fills the selected record. Save commits its fields, tags, cover/album references, captions, notes and half-star rating; Back discards the draft. Rating-dialog Save/Cancel applies only to the rating draft until the form is saved. | `06-edit-restaurant.png` / **50:166** and shared photo/rating references. | FR-04, FR-07, FR-23 |
| **Full-Screen Map** | Selected static map preview, saved address, Back and Get directions URL handoff, reached from Details. Live maps, current location, permission denial and unavailable-location feedback remain later work. | Native layout; no named retained Figma frame. Live-location supporting states remain needed. | Both source FR-13 rows, FR-14, FR-15 |
| **About** | Application/project information, names of all four members and Back. Reached from Restaurant List navigation. Compact purple toolbar and rounded information/team panels; scrollable content preserves readability. | Native styling; no named retained Figma frame. | FR-19 |
| **Confirmation / Dialogs** | Compact centered Delete confirmation, exact required-name warning, quick notes, rating, custom-tag, address-details, invalid-phone and add-photo/caption dialogs. Confirmed deletion updates the session list. Location permission/error states remain later work. | `03-delete-confirmation.png`, `08-name-required.png`, `10-rating-picker.png`. | FR-05, FR-15, FR-23 |

The source repeats FR-13; both requirements remain mapped without renumbering them.

## Shared behavior

- Tags are selectable filters on Restaurant List, read-only labels on Details and editable values in Add/Edit.
- Add starts empty; Edit pre-fills the selected record. Save applies the full draft; Back discards it. Rating-dialog Save/Cancel only changes the form's rating draft until the form is saved.
- My photos follows tags on Details and the required name in Add/Edit. **+ photo** follows short albums with an 8dp gap, starts left when empty and remains visible beside scrolling long albums. A caption belongs to one photo; visit notes describe the restaurant visit.
- Gallery supports swiping at fit, arrows/thumbnails, 100–300% zoom and bounded panning. Changing photos resets zoom. Close returns to the selected restaurant.
- View full map and Share by email sit below the embedded map and above Notes. The map uses a selected static preview; email uses the required fields and a copyable fallback if unavailable.

## Remaining functional work

Implement durable records and photo storage, live maps/address resolution, current-location permission/error handling and successful external directions/email handoffs. The current prototype uses session records and saved UI state. Half-star dragging, photos, captions and visit notes are requested additions; the original proposal remains unchanged.

The [demo package](demo/README.md) provides the design/application-purpose video and timed recording script.
