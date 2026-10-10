# Restaurant Guide design decisions

This records the saved [Project Design Proposal](https://docs.google.com/document/d/1NKr3KEbKyUrFnKXlOMboBQbfbuzSqtX0LajzcLMMMd0/edit), retained Figma evidence and current Android implementation decisions. The original proposal and [requirements](requirements.md) remain unchanged. Historical checkpoints below describe their state at the time; the October 10 shared form, Map, About and gallery-zoom work supersedes earlier placeholders and basic forms.

## Current implementation decisions — October 10 UI prototype

| Item | Saved evidence | Current decision and boundary |
| --- | --- | --- |
| Rating value | FR-07 specifies a “1-5 star integer rating”; the student explicitly requested dragging directly across the stars and fractional values. Figma illustrates 4.5/5. | Implement half-star increments with enlarged draggable stars, numeric value and Save/Cancel. Add starts unrated; existing records retain their ratings. The model uses `Float`, and `restaurants-v3` UI state restores v2/legacy integer values. The user's request authorizes this prototype choice; the source requirement is not silently changed. |
| Phone number(s) | FR-06 allows phone number(s); the retained form shows one Phone field. | Keep `phone:String` for the prototype, with multiple numbers entered on separate lines, an Add phone number control and invalid-number feedback. These are native extensions; no retained multiple-phone frame exists. Durable storage representation remains later work. |
| Address entry | FR-08 requires separated fields and/or formatting assistance; Figma shows “Street address, city and postal code.” | Retain that field/prompt and add an address-details dialog with separated fields and Canadian postal-code formatting. This is a native extension, not an exported Figma variant; it does not geocode the address. |
| User-defined tags | FR-10 permits selecting one or more tags; the proposal declares `tags:String`. The accepted demo uses any-selected-category matching (OR). | Store tags as a session `List<String>`, add/remove tags through the shared form and deduplicate without case. List filters are selectable; Details tags remain read-only outlined labels. Search combines with selected-tag matching using AND. |
| Photos, captions and visit notes | The student explicitly requested these additions; they are absent from the original FR attributes. Existing menu photos are illustrative. | Include cover replacement/removal, album add/remove, optional captions for chosen photos and visit notes in the shared Add/Edit draft. Form Back discards draft changes; Save updates the selected/new record. Cover references and album/caption references are separate model fields. The real Android picker selection/returned-image byte identity, caption Save and restoration passed device verification. Durable photo storage remains later work. |
| Gallery zoom | The student's supplied viewer reference includes −/percentage/+ controls; zoom was explicitly requested for the final prototype pass. No named retained zoom Figma frame exists. | Preserve the black gallery and add 100–300% pinch/button zoom, percentage reset and bounded panning for bundled/URI images. One finger swipes at fit and pans while enlarged. Arrows/thumbnails select another image at 100%; Close/hardware Back return to the same Details. Loading/unavailable images disable zoom. Long captions remain scrollable; short windows use compact spacing. Existing image resolution limits still apply. |
| About styling | FR-19 requires application/project information and all team names. Existing labels and names come from the approved proposal; no named retained About Figma frame exists. | Use the shared compact purple toolbar, a lavender information panel and a white team panel with soft borders, rounded corners and scrollable content. Retain all four names and the existing purpose text. Toolbar/hardware Back preserve List search and selected tags. A simulated 320×420dp window at 2× text keeps information scrollable and Back reachable. |
| Google Maps setup | The proposal names Google Maps SDK/API and location permissions. The accepted navigation is Details → internal Full-Screen Map → Get directions → Google Maps. | The compact native map layout uses selected static captures, the saved address and a destination-only directions URL. Its address information scrolls independently of Get directions. Seven scoped device checks passed; see [Map verification](restaurant-map-verification.md); the full prototype suite also retains these checks. No named Figma map frame is retained. Live map configuration, geocoding, app-obtained current location and denied/unavailable-location feedback remain unimplemented. Keep future credentials outside version control. |

The shared form implements retained Add **52:213** and Edit **50:166**, preserving their wording, floating labels, 12dp input corners and 16dp photo-to-description border spacing. The retained rating overlay is **76:271**. Native dragging, typing and saving were verified separately from Figma's fixed illustration: the earlier shared-form checkpoint passed **32 unit tests, 18 device tests and zero lint errors**. See [Add/Edit verification](restaurant-form-verification.md) for that scope and evidence. Fresh connector access remains quota-blocked, and no Figma or shared proposal edit was made.

The latest About/gallery-zoom audit passed fresh app/test builds, **32 unit tests and all 33 device tests**. Lint has **zero errors, three warnings and three hints**; the new warning is a benign plural suggestion for the zoom percentage description. The suite retains the connected List/Details/Add/Edit/photo/map regressions and adds About Back/state preservation plus gallery zoom, pan, reset, unavailable-image and compact-window checks. [Final prototype review](prototype-final-verification.md) links the complete native journey and [recorded demo package](demo/README.md), with media/cleanup evidence kept separate from test results. The video explains the design and application purpose using actual Android footage and synthetic narration.

Remaining functional work includes successful composition in a real external email app, durable storage and live map/location/permission flows. Verification uses the API 37.1 test fixture. Simulated short-window gallery and large-text About checks passed; physical touch, minimum-API, actual device rotation and full-application large-font coverage remain unverified. Earlier List and Details reports remain historical checkpoints.

## Earlier snapshot — saved Figma coverage before the Android shared form

The Home filters and read-only Details tags were completed in Figma: eight fixed filter states, any-selected-category matching, reset with All, and preservation of filters after Details → Back. This is prototype behavior, not proof of Android data persistence.

The October 8 arrangement changed canvas positions only and preserved existing frame IDs, contents and prototype wiring. It did not add missing screens.

At that saved design checkpoint, the requirements comparison identified:

- **Splash Screen:** application logo/name and transition to Restaurant List. A logo placeholder was accepted for drafting.
- **Full-Screen Map:** selected restaurant marker, Back and Get directions, reached from Details. The accepted flow is Details → internal Full-Screen Map → Get directions → Google Maps.
- **About:** application/project information and team names, with an entry from Home/navigation.
- **Tags:** custom-tag entry and removal. Multiple selected Home filters are now represented; live arbitrary tags are not established by that fixed-state demo.
- **Forms:** multiple phone numbers, address-format assistance and representative invalid-field feedback.
- **Directions:** permission request plus denied/unavailable-location messages.
- **Save and email sharing:** representative successful-save behavior and email handoff prefilled with name, address, phone number(s), rating, tags and description. Existing Details map/email buttons are present; their real handoffs remain separate implementation work.
- **Empty/no-results states:** required as Should Have under FR-22.

The gallery has arrow/thumbnail navigation and Close; swipe and zoom are not implemented in the saved Figma checkpoint. Photo selection, caption entry, form entry, uploads, thumbnail removal and persistence remain visual controls at that checkpoint. The first prototype’s fixed-state navigation should not be described as a completed functional application.

Facebook, Twitter/X, favourites and rating filtering remain optional/future scope in the proposal.

## Earlier snapshot — October 10 Restaurant List implementation

The following records the original List checkpoint. Later implementation bundled all three original covers, built Details and replaced its basic Add/Edit forms with the shared form above; its placeholder and integer-rating statements are historical.

For the Android UI prototype, name search is a trimmed, case-insensitive substring match. Multiple selected tags use OR, and search combines with that result using AND. All clears tag selection while retaining the query; the no-results action clears both. This follows the previously accepted demo behavior. Tags are a list of strings in the in-memory model, with entry split on commas and deduplicated without case; the storage representation remains undecided. Integer ratings are retained for this step; the requested draggable half-star picker is still part of the future shared form. The prototype keeps query/filter/scroll and session records through saved UI state; this is not durable storage.

Restaurant List has basic Details/Add/About destinations to exercise navigation. Full restaurant workflows remain separate. A custom tag named All is displayed with “(tag)” to distinguish it from the reset control, and list keys separate reset/tag items. Two Brothers uses the retained source photo; the other two originals are unavailable locally and Figma asset retrieval is quota-blocked, so their image slots have neutral placeholders.

Device tests explicitly use Espresso 3.7.0 because the Compose test dependency otherwise pulls an older version that calls a removed reflective InputManager API on the available API 37 image. This is a test-only compatibility fix documented in the [official AndroidX Test release notes](https://developer.android.com/jetpack/androidx/releases/test#espresso-3.7.0).
