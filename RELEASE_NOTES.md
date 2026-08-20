# Release Notes - v1.0.5

## What's New
- **Overhauled Update Mechanism**: Updates now download silently and robustly in the background using Android's native `DownloadManager`, complete with system-level progress notifications. 
- **Smart Update Caching**: The app will now safely cache downloaded updates for 24 hours. If you dismiss the installation prompt, you can seamlessly tap the persistent notification to retry without re-downloading!
- **Storage Protection**: The app now strictly checks to ensure your device has at least 150MB of free space before initiating an update.
- **Modern Android Support**: Bumped `targetSdk` to 37 (Android 17 target), ensuring Julie runs flawlessly and utilizes the latest privacy and efficiency features on modern devices.

## Improvements
- Updated the "100% Offline" tag in our documentation to accurately reflect the new optional GitHub update checks (now "Offline First").
- Added a convenient "Download Latest Release" button directly to the project's README.

## Fixes
- Replaced the placeholder images for Guinea Pig, Mouse, and Bird in the Pet Facts Carousel with higher quality, accurate illustrations.
