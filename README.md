<p align="center"><img src="assets/abbadi/logo.png" width="600" alt="ABBADI Tube"/></p>

# ABBADI Tube (Android)

Privacy-friendly YouTube client with a **deep black glass** design — dark frosted surfaces, white text, subtle red accents. Optimized for Samsung Galaxy Z Fold6. Interface: English & Arabic.

**By Abdalla Abbadi · connect@3bbadi.com**

## Attribution
ABBADI Tube is a fork of [LibreTube](https://github.com/libre-tube/LibreTube) by the LibreTube contributors, licensed under **GPL-3.0**. This fork is also GPL-3.0 (see `LICENSE`). Original README: `README.libretube.md`. Not affiliated with YouTube/Google or the LibreTube project.

## Build
`./gradlew assembleRelease` (JDK 21). Provide your own signing config in `keystore.properties` (not included).

## Download
See [Releases](../../releases).

## Publishing updates (update.json)
Both apps read [`update.json`](update.json) from this repo's `main` branch on every launch (and from About → Check for updates).

1. Build a new APK with a higher `versionCode` signed with the **same keystore**.
2. Create a GitHub Release (e.g. `v1.5.0`) and attach the APK.
3. Edit `update.json`: set `latestVersionCode`, `latestVersionName`, `apkUrl` (the release asset URL), `notesEn` / `notesAr`. Commit to `main`. Users get an "Update available" dialog that downloads and installs the APK.
4. **Force-stop old versions:** raise `minSupportedVersionCode` (Android) or `minSupportedIosBuild` (iOS). Any installed version below it shows a blocking screen with `forceMessageEn` / `forceMessageAr` and an Update button. Versions before 1.4.0 have no checker and cannot be blocked.
5. iOS: set `iosVersion`, `iosBuild`, `iosUrl` (link users open to get the new build).

If the file can't be fetched (no network), the apps keep working (fail-open). GitHub's raw CDN may take up to ~5 minutes to reflect edits.

**Developer: Abdalla Abbadi · connect@3bbadi.com** (المطور: عبدالله عبادي)
