---
name: release-manager
description: Defines the automated GitHub Release workflow and my exact behavior as a Release Manager when preparing a new version.
---

# Release Manager Skill

When the user asks you to "prepare a release for version X" or similar, you are to act as the Release Manager. Follow this exact workflow strictly.

## Phase 1: Prepare Release (On-Demand)
When asked to prepare a release:

1. **Bump Version:**
   Execute the version bump helper script: `./scripts/bump_version.sh <version_name>`.

2. **Build and Verify Release APKs:**
   Execute the Gradle release task: `./gradlew release`.
   After it completes, verify the output APKs are actually signed using the Android `apksigner`. If `apksigner` is not in the system PATH, locate it via `find $ANDROID_HOME/build-tools -name "apksigner" | sort -r | head -n 1`.
   Run the verification on the generated APKs (e.g., `app/build/outputs/apk/arm64/release/*.apk`).
   If any verification fails, **STOP** and report the error loudly to the user. Do not proceed to draft notes.

3. **Draft Release Notes:**
   Run `git log $(git describe --tags --abbrev=0)..HEAD --oneline` (or `git log --oneline` if no tags exist) to view commits since the last tag.
   Synthesize these raw commits into user-facing release notes categorized sensibly (e.g., "New Features", "Improvements", "Fixes"). Ensure the language matches the app's feature set.
   Save this draft to a temporary file named `RELEASE_NOTES.md` in the repository root.

4. **STOP AND WAIT:**
   Present the drafted release notes to the user along with confirmation that the APKs were successfully built and signed.
   **CRITICAL:** Do NOT run `gh release create`, do NOT push the tag, and do NOT commit the files yet. Wait for the user's explicit confirmation (e.g., "publish it" or "looks good").

## Phase 2: Publish (On Confirmation)
When the user explicitly confirms the release notes:

1. **Commit and Tag:**
   Commit the version bump and release notes:
   `git add app/build.gradle.kts RELEASE_NOTES.md`
   `git commit -m "chore: bump version to <version_name> and prepare release"`
   Create the Git tag: `git tag v<version_name>`
   Push to origin: `git push origin main && git push origin v<version_name>`

2. **Create GitHub Release:**
   Use the GitHub CLI to publish the release and attach the signed APKs:
   `gh release create v<version_name> app/build/outputs/apk/*/release/*.apk --title "v<version_name>" --notes-file RELEASE_NOTES.md`

3. **Confirm Success:**
   Verify the `gh` command succeeded and provide the user with the resulting GitHub release URL.
