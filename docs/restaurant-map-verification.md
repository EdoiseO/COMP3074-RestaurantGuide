# Full-Screen Map — October 10, 2026

Details → **View full map** opens the selected restaurant's saved map preview. The screen retains **Full-Screen Map**, **Back**, **Map preview** and **Get directions** wording. Back returns to that restaurant's Details. The saved name/address appear below the map; Get directions hands its encoded destination to an external map app or browser.

## Native layout and states

There is no named map frame in the retained Figma inventory. This native layout follows the app's existing purple colors, white background, compact toolbar, rounded corners and spacing; it does not claim a new Figma frame or pixel-perfect comparison.

- The toolbar has a 48dp minimum height and applies native system insets once. The image area takes the remaining space, with 20dp side margins and a **Map preview** badge.
- The original selected portrait map is shown with `ContentScale.Fit`, preserving the complete image, restaurant pin and Google attribution. All three bundled demo maps are reused unchanged. A portrait-only resource is a valid preview, even when its embedded-map resource is absent.
- The rounded lavender information panel displays the saved name/address. Its information can scroll when text is long or the window is short; the 48dp minimum **Get directions** action stays outside that scroller. Back remains available above the map.
- A record without a saved map image uses the clearly labelled existing illustrative preview. Changing an address through Edit clears its stale map references. A blank address shows **Add an address to get directions.** and disables the action. A missing external handler shows **Directions unavailable** with the existing message and **Got it**, returning to the same map.

`RestaurantMapEntry` now lives in [RestaurantMapScreen.kt](../app/src/main/java/ca/gbc/comp3074/p12/restaurantguide/ui/RestaurantMapScreen.kt). The shared embedded preview and URI helper remain in `DetailsDestinations.kt`; suppressing its fallback badge on the full-screen map avoids duplicate labels without changing Details.

## Directions and requirement boundary

The existing URL uses `https://www.google.com/maps/dir/?api=1&destination=…`, with the selected name/address encoded as one destination. It does not supply an origin. Google documents that these URLs open its Android app or browser, and that an omitted origin may use a relevant starting location or prompt for one. This does not prove that Restaurant Guide has obtained device location or handled permission. [Official Maps URLs documentation](https://developers.google.com/maps/documentation/urls/get-started#directions).

The original requirement numbering and wording remain in [requirements.md](requirements.md):

| Source requirement | Current prototype coverage |
| --- | --- |
| First FR-13: full-screen map with an option to get directions to the selected restaurant. | Selected static preview, Back and destination URL handoff. |
| Second FR-13: obtain the user's current location with permission handling and use it as the starting point. | Deferred, as requested. No location retrieval or permission request in this screen. |
| FR-14: Google Maps with current location and selected restaurant as origin and destination. | Destination handoff only; app-obtained origin and complete live integration remain deferred. |
| FR-15: denied permission or unavailable device location feedback, preventing incorrect directions. | Deferred. Missing-address and missing-handler feedback do not replace location permission/error handling. |

The illustrative preview is not geocoding, and image controls are part of a static capture. Live Maps SDK, current location, denied/unavailable-location states and successful real external directions remain later integration work.

## Verification

Fresh debug app/test builds and lint passed: zero errors, two existing warnings and three existing state-boxing hints. The first test compilation exposed a missing semantics extension import; it was corrected, and its build log is retained separately. Unit/model suites were not rerun because this change affects native map layout and handoff rather than restaurant data.

**Seven scoped device tests passed**: six new map checks plus the existing selected-record map/email and List Back-state regression. They cover all three saved records, Back to the same Details, blank-address disabling, Edit clearing a stale map, a portrait-only image, the actual directions-button intent and missing-handler dismissal. A simulated 320×420dp window verifies that a long address scrolls while Back and Get directions remain available. Its screenshot includes unused host space outside the deliberately smaller fixture; that space is not the production screen.

The directions test captures `ACTION_VIEW` and checks the decoded selected destination, `api=1`, host and path without launching an external app. It proves the handoff request, not successful real navigation or device-location acquisition. Unit/model suites and the earlier full 18-test form suite were not rerun for this map change.

Production MainActivity was opened through List → Details → View full map for all three records. Native UI readbacks confirmed each saved address and Back to the same Details. Source review and independent visual review found no app layout blocker: selected pins, names, addresses, complete image attribution, rounded panels and actions are intact. Two Brothers and Midyeci production captures are unobstructed. La Unica's production capture has a guest Google Play services heads-up notification over the toolbar; its clean device-test capture verifies that layout. No images were edited to hide the overlay.

The API 37.1 read-only guest used 1.5GB RAM, two cores and a 412×940dp display, with guest network/animations and nonessential Google background services disabled. It settled before three clean startup observations. Physical-device, minimum-API, large-font and landscape behavior remain unverified. Saved logs contain no fatal exception or ANR; production MainActivity was resumed. The owned emulator was stopped immediately in `finally` after captures; fresh readback confirms PID **74859** and **emulator-5558** absent. Shared adb and student apps were preserved.

[Two Brothers production preview](../../../outputs/2026-10-10-full-screen-map/production-map-two-brothers.png) · [Midyeci production preview](../../../outputs/2026-10-10-full-screen-map/production-map-midyeci.png) · [La Unica clean test preview](../../../outputs/2026-10-10-full-screen-map/full-map-restaurant-3.png) · [Long-address fixture](../../../outputs/2026-10-10-full-screen-map/full-map-small-viewport-long-address.png) · [Missing-handler dialog](../../../outputs/2026-10-10-full-screen-map/full-map-directions-unavailable.png)

[Build receipt](../../../outputs/2026-10-10-full-screen-map/build-receipt.json) · [Runtime receipt](../../../outputs/2026-10-10-full-screen-map/runtime-receipt.json) · [Layout/log evidence](../../../outputs/2026-10-10-full-screen-map/layout-verification.json) · [Cleanup readback](../../../outputs/2026-10-10-full-screen-map/cleanup-readback.json)
