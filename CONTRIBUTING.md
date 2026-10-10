# Working together

Accept the GitHub invitation to this private repository, then clone it and open it in Android Studio. Collaborators have ordinary write access.

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
