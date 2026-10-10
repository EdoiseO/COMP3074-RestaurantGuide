# Restaurant Guide design decisions

This records differences and open questions in the saved [Project Design Proposal](https://docs.google.com/document/d/1NKr3KEbKyUrFnKXlOMboBQbfbuzSqtX0LajzcLMMMd0/edit) and Figma checkpoint. It does not revise the team proposal or infer code decisions. Coverage is based on the October 4 requirements comparison, the October 7 filter-demo verification and the October 8 canvas-arrangement handoff; a fresh full design audit was not performed for this repository setup.

## Decisions requiring agreement

| Item | Saved evidence | Decision still required |
| --- | --- | --- |
| Rating value | FR-07 specifies a “1-5 star integer rating”; the model has `rating:Int`. The student requested direct dragging across the stars and half-star values; Figma illustrates 4.5/5. | Preserve the requested gesture while agreeing whether values snap to whole stars or the team revises the proposal/model for half-stars. The Figma picker itself remains a fixed illustration. |
| Phone number(s) | FR-06 and the main objectives allow phone number(s), while the model has `phone:String` and the form has one field. | Agree how multiple numbers are entered and represented before implementing storage. |
| User-defined tags | FR-10 permits selecting one or more tags; the proposal declares `tags:String`. The October 7 Figma demo uses any-selected-category matching (OR). | Confirm the application’s matching semantics and tag storage/entry behavior. The working demo choice does not silently revise the proposal. |
| Photos, captions and visit notes | These are student-requested additions in Figma; they are absent from the saved requirements and Restaurant model. Sample menu photos are illustrative, not personal-visit evidence. | Agree whether to include them in implementation and update their scope/data model if included. |
| Google Maps setup | The proposal names Google Maps SDK/API and location permissions but does not explain API-key/authentication setup. | Confirm configuration, address-to-location behavior and location handling. Keep any credentials outside version control. |

## Saved design coverage and remaining gaps

The Home filters and read-only Details tags were completed in Figma: eight fixed filter states, any-selected-category matching, reset with All, and preservation of filters after Details → Back. This is prototype behavior, not proof of Android data persistence.

The October 8 arrangement changed canvas positions only and preserved existing frame IDs, contents and prototype wiring. It did not add missing screens.

The latest full requirements comparison still identifies:

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
