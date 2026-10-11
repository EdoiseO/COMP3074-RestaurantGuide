# Working together

Clone this repository and open it in Android Studio. Accept your GitHub invitation to get ordinary write access.

Create a short feature branch for your change and open a pull request back to `main`. Coordinate before editing the same screen or shared data files. Describe what changed, include UI screenshots when useful, and record the checks you ran. Ask another member to review before merging team changes.

```sh
git switch main
git pull --ff-only
git switch -c feature/your-change
# Make and verify your changes.
git add <files>
git commit -m "Describe the change"
git push -u origin feature/your-change
```

Run `./gradlew :app:assembleDebug :app:lintDebug` after changing the Android project. Verify actual user interactions on an emulator or device before claiming a feature is complete; a successful build alone is not runtime verification.

Use the saved proposal, current Figma design and `docs/design-decisions.md` to guide changes. Keep credentials, local SDK paths, generated files and unrelated coursework out of commits. Keep project work and school submission separate; retain genuine receipts for any later authorized submission.

## Commit scope

Commit application code/assets/tests, build configuration, useful setup/design/requirements/source documentation and explicitly requested project deliverables. Keep course memory, agent notes, audit reports, screenshots used only for verification and machine-specific receipts outside this repository.

Markdown paths are explicitly listed in `.gitignore`. Add an exception only for a useful project document. Stage named files and inspect `git diff --cached --name-status` and `git diff --cached --check` before committing; do not force-add ignored notes or use broad staging without reviewing every file. Record test results in the commit/PR description instead of adding a new checkpoint report for each change.
