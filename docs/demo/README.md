# Restaurant Guide UI prototype demonstration

The UI prototype brief requires a GitHub repository link and a video explaining the design and application purpose. This repository is public as requested. The official team remains **Kalid Ali, Phillip Onofua, Mohamed Amine OUATAR and Joshua Zaakir**. Phillip is building this UI prototype alone; the approved broader team roles are retained in [Development Plan](../development-plan.md).

[Public repository](https://github.com/EdoiseO/COMP3074-RestaurantGuide) · [Final UI review](../prototype-final-verification.md)

The completed [demo video (4:58)](Restaurant-Guide-UI-Prototype.mp4) explains the design and application purpose using actual Android footage. It is 1920×1080 H.264/AAC at 30 fps, with twelve chapter markers, captions and synthetic narration. Full decoding, measured audio levels and sampled visual checks passed.

- [Narration script](narration.txt)
- [Subtitles](narration.srt)
- [About preview](about.png) and [200% gallery preview](gallery-200.png)
- [Verification receipt](verification.json) — build/test results, video hash, media details and cleanup.

The walkthrough explains why the app brings restaurant information, ratings, photos and visit notes together, then demonstrates Splash, List, live search/tags, Details, My photos/zoom, Notes, the selected Map, email fallback, Add validation, half-star rating/custom tag, Edit/Delete, the empty list and About. On-screen app labels and all four names retain the project wording.

Footage comes from the actual production MainActivity in isolated API37.1 emulator guests, edited into chapters with system bars cropped and native footage played at 1.5× speed. Narration uses the local macOS **Samantha synthesized voice**, with captions; it is not a team member's recorded voice. Two short system-notification intervals were removed by temporal cuts, preserving clean complete-card, count and saved-record states. The other ten chapters retain their full media. Raw clips, native UI readbacks, build/device logs and media receipts remain in the dated course output directory.

Records use session data and saved UI state. The Map is a selected static preview and Get directions prepares an external destination URL. The email chapter uses an isolated guest with Gmail disabled to show the copyable no-handler fallback. Successful composition/navigation in external apps, durable storage/photo access, live Maps and current-location permissions remain later functional work. No email is sent, and this preparation does not submit the assessment to the school portal.
