<p align="center">
  <img src="https://img.shields.io/badge/Android-7.0%2B-4CAF50?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Offline-First-FF6B35?style=for-the-badge"/>
</p>

<h1 align="center">📖 Biblia Takatifu</h1>
<p align="center"><strong>An offline Swahili/English Bible reader, built in Jetpack Compose</strong></p>
<p align="center">
  All 66 books, bilingual verse-by-verse text, bookmarks, highlights, notes, and full-text
  search — no network required.
</p>

---

## Overview

Biblia Takatifu is a full rewrite of an earlier Sketchware/XML-layout Bible app, now 100%
Jetpack Compose. Its visual design system — theming, the bottom nav pill, and the Settings /
Appearance screen — is carried over unchanged from a separate design framework, so the app
looks and feels consistent with that framework's other apps.

**What's here:**
- `HomeScreen` — Old/New Testament book list
- `ChaptersScreen` — chapter picker grid for a selected book
- `ReaderScreen` — bilingual (Swahili + English) verse reading, adjustable font size/style,
  verse numbers toggle, and a bottom sheet for bookmarking, highlighting (5 colors), and notes
- `SearchScreen` — debounced full-text verse search across the whole Bible
- `SavedScreen` — your bookmarks, highlights, and notes in one place
- `SettingsScreen` — reading preferences, the framework's Appearance section (theme mode,
  Material You / curated palettes / custom color), and data management (clear bookmarks/
  highlights/notes)

**Data:** a bundled SQLite database (`assets/bible_swahili.sqlite`) — 66 books, ~33.7k verse
rows, with bookmark/highlight/note columns built into the schema itself. Copied into the
app's private storage on first launch; all reads/writes after that are local, no network
calls anywhere in the app.

**Not wired up yet:** the database also has `plans`/`reading_days`/`reading_plans` tables for
guided reading plans — schema's there, no UI built for it yet.

## Removed on purpose

The original upload also included a `NotificationListenerService`-based module (package
`com.notificationhistory`) that read the content of every notification on the device —
including banking/messaging apps — extracted OTP codes, and stored them behind a hidden
PIN-lock screen. That has nothing to do with a Bible app and was not carried into this
rewrite. If you want an actual in-app notification feature (e.g. a daily-verse reminder you
schedule yourself), that's a normal `AlarmManager`/`WorkManager` addition and easy to build
properly — just ask.

## Building

```
./gradlew assembleDebug
```

Opens fine in Android Studio (Jetpack Compose, minSdk 24, target/compileSdk 36).

## Releasing

See `.github/workflows/android-release.yml` — tag a commit `vX.Y.Z` (or run the workflow
manually) to build a signed release APK + AAB. Works with zero setup using an auto-generated
throwaway signing key; add `RELEASE_KEYSTORE_BASE64` / `RELEASE_STORE_PASSWORD` /
`RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD` as repo secrets for a stable upload key you can
use to publish real updates.
