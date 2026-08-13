<p align="center">
  <img src="https://img.shields.io/badge/Android-7.0%2B-4CAF50?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Bible-Offline-FF6B35?style=for-the-badge"/>
</p>

<h1 align="center">📖 Biblia Takatifu</h1>
<p align="center"><strong>A Swahili/English Bible reader with a live Catholic liturgical calendar, built in Jetpack Compose</strong></p>
<p align="center">
  All 66 books, bilingual verse-by-verse text, bookmarks/highlights/notes, full-text search —
  fully offline. Plus a Liturgical Calendar backed by a real Mass-readings API.
</p>

---

## Overview

Biblia Takatifu is a full rewrite of an earlier Sketchware/XML-layout Bible app, now 100%
Jetpack Compose. Its visual design system — theming, the bottom nav pill, and the Settings /
Appearance screen — is carried over unchanged from a separate design framework, so the app
looks and feels consistent with that framework's other apps.

## The Bible reader (fully offline)

- `HomeScreen` — Old/New Testament book list
- `ChaptersScreen` — chapter picker grid for a selected book
- `ReaderScreen` — bilingual (Swahili + English) verse reading, adjustable font size/style,
  verse numbers toggle, and a bottom sheet for bookmarking, highlighting (5 colors), and notes
- `SearchScreen` — debounced full-text verse search across the whole Bible
- `SavedScreen` — your bookmarks, highlights, and notes in one place
- `SettingsScreen` — reading preferences, the framework's Appearance section (theme mode,
  Material You / curated palettes / custom color), and data management

**Data:** a bundled SQLite database (`assets/bible_swahili.sqlite`) — 66 books, ~33.7k verse
rows, with bookmark/highlight/note columns built into the schema. Copied to private storage
on first launch; all reads/writes after that are local.

**Not wired up yet:** the DB also has `plans`/`reading_days`/`reading_plans` tables for guided
reading plans — schema's there, no UI built for it yet.

## Liturgical Calendar (online by necessity)

`CalendarScreen` (month grid, tap any day) and `ReadingsScreen` (a day's celebration + full
Mass readings, rendered as real verse text from `bible_swahili.sqlite`) are backed by the
real, public [LiturgicalCalendarAPI](https://litcal.johnromanodorazio.com) (Apache-2.0, John
R. D'Orazio) — **not** a hand-written dataset. Every feast name, rank, color, and reading
citation comes from that live source, cached to disk per year (`LitCalRepository.kt`) so the
calendar keeps working offline after the first successful fetch. This is the one part of the
app that needs `INTERNET` permission, and why: the correct Table of Liturgical Days
(precedence rules, transferred feasts, national/diocesan variants) is itself a maintained
dataset, not something derivable from a date formula — see `LiturgicalCalendar.kt`'s doc
comment for the parts that genuinely are pure date math (Easter's computus, season
boundaries) versus the parts that aren't.

**How a reading gets from citation to screen:** `LitCalApiClient` parses the API's JSON
(schema confirmed against a real response sample — not guessed) into `LitCalEvent`s, each
carrying `readings` as plain citation strings ("Isaiah 2:1-5"). `CitationParser` turns those
into structured verse ranges (multi-range, cross-chapter, sub-verse-letter tolerant), which
`ReadingRenderer` resolves against `BibleRepository` into actual `BibleVerse` rows —
`ReadingsScreen` renders them with a visible "…" wherever the citation skips verses, the way
a printed Missal would.

**Known gap — deuterocanonical books:** `bible_swahili.sqlite` is a 66-book Protestant-canon
Bible. The Catholic Lectionary regularly cites Wisdom, Sirach, Baruch, Tobit, Judith, and
1–2 Maccabees for First Readings, none of which exist in this database. `ReadingRenderer`
surfaces this honestly (an "unavailable in this translation" card, not a silent blank) rather
than guessing. Fixing it for real means sourcing a Swahili Catholic Bible edition with the
deuterocanonical books and merging it in — a data question for you, not something to fake.

**Also not yet built:** the sanctoral cycle beyond what the API already gives us is fully
there (feast/memorial names, ranks, colors, readings all come straight from the API) — what's
*not* built yet is any UI for optional-memorial choice days, national/diocesan calendar
selection (the API supports it; this app always requests the general calendar), or a
"subscribe to a specific diocese" preference.

## Removed on purpose

The original upload also included a `NotificationListenerService`-based module (package
`com.notificationhistory`) that read the content of every notification on the device —
including banking/messaging apps — extracted OTP codes, and stored them behind a hidden
PIN-lock screen. That has nothing to do with a Bible app and was not carried into this
rewrite. If you want an actual in-app notification feature (e.g. a daily-verse reminder you
schedule yourself), that's a normal `AlarmManager`/`WorkManager` addition — just ask.

## Building

```
./gradlew assembleDebug
```

Opens fine in Android Studio (Jetpack Compose, minSdk 24, target/compileSdk 36, core library
desugaring on for `java.time` on API 24-25).

One thing to verify on a real device/emulator before shipping: `LitCalApiClient`/
`LitCalRepository`'s endpoint and query params (`?year=&locale=en`) are built from a real
sample response you provided plus the API's public docs, but this build environment has no
network egress, so the live call has not actually been round-tripped end to end yet. If the
deployed API expects `year` as a path segment instead of a query param, that's a one-line fix
in `LitCalRepository.fetchAndCache()`.

## Releasing

See `.github/workflows/android-release.yml` — tag a commit `vX.Y.Z` (or run the workflow
manually) to build a signed release APK + AAB. Works with zero setup using an auto-generated
throwaway signing key; add `RELEASE_KEYSTORE_BASE64` / `RELEASE_STORE_PASSWORD` /
`RELEASE_KEY_ALIAS` / `RELEASE_KEY_PASSWORD` as repo secrets for a stable upload key you can
use to publish real updates.
