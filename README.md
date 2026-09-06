<p align="center"><strong>Biblia Takatifu</strong></p>
<p align="center">A Swahili/English Bible reader, built in Jetpack Compose</p>

## What this is

A fully offline Bible app for `com.biblia.app`. Everything lives in one bundled SQLite
database (`assets/bible_swahili.sqlite`) - all 66 books, Swahili and English text side by
side, plus pre-seeded reading plans. No network permission is declared anywhere in the
manifest; the app never needs one.

The liturgical calendar/lectionary feature that used to live here (a Catholic Mass-readings
calendar, backed first by a live API and later by a bundled Swahili lectionary dataset) has
been removed entirely, by request, so the whole app could focus on the Bible-reading
experience instead.

## Features

- **Reading** - verse-by-verse or a flowing "paragraph" reading mode (continuous text with
  small superscript verse numbers, closer to reading an actual book); adjustable font size,
  three font families (serif/sans/mono), justified-text option, bilingual (Swahili + English)
  or Swahili-only.
- **Themes** - System/Light/Dark, plus a warm Sepia "paper" theme for comfortable reading.
- **Bookmarks, highlights (5 colors), and notes** - set from the verse action sheet in the
  Reader, browsed from the Yaliyohifadhiwa (Saved) tab.
- **Share a verse** - formatted text via the Android share sheet.
- **Read aloud** - on-device text-to-speech for the current chapter (Swahili voice if the
  device has one installed, falling back to the device default otherwise).
- **Swipe between chapters** - drag left/right anywhere in the Reader, in addition to the
  arrow buttons.
- **Search** - phrase or any-word matching, scoped to All/Old Testament/New Testament, with
  recent searches remembered.
- **Reading plans** - 3 real plans (Chronological, Canonical, Historical-by-authorship), each
  in 3 pacing variants (1 year / 6 months / 3 months), using the `plans`/`reading_plans`/
  `reading_days` tables that were already seeded in the Bible database. Progress is tracked
  per plan with a ring indicator; mark a day read, jump straight to any chapter.
- **Verse of the Day** - a curated rotation of well-known verses, shown on Home, picked
  deterministically by day-of-year.
- **Daily reminder notification** - opt-in, user-chosen time, delivers that day's curated
  verse. Inexact `AlarmManager` (no exact-alarm permission needed), survives reboot.
- **Reading streak** - tracks consecutive days the Reader was opened, shown on Home.

## Navigation

Three bottom-nav tabs: **Biblia** (Home), **Mipango** (Reading Plans), **Yaliyohifadhiwa**
(Saved). Search and Settings are icon buttons (top bar), not tabs - Search pushes a normal
screen, Settings opens as a bottom sheet.

Hardware/gesture back jumps straight to Home from anywhere else, and is double-press-to-exit
on Home itself. In-app top-bar back arrows still do a normal one-level pop.

## Architecture notes

- No Room, no KSP - both `BibleRepository` and `ReadingPlanRepository` use raw
  `SQLiteDatabase` directly against the bundled asset (copied to app storage read-write on
  first launch, since bookmarks/highlights/notes/plan-progress all need to write to it).
- DataStore Preferences for every setting (`ReadingPrefs`, `AppearancePrefs`, `ReminderPrefs`,
  `ReadingPlanPrefs`, `RecentSearchesPrefs`, `ReadingStreakPrefs`) - one small file each.
- Single-Activity Compose UI with a custom string-based back stack (`MainActivity.kt`) - no
  Jetpack Navigation library, despite `navigation-compose` still sitting in the dependency
  list unused from the original project template.
- `chapter_order` in `reading_plans` maps directly onto the same 1-66 canonical book ordering
  the `chapters` table's own ids already use (`bookId = 8021 + chapter_order`) - see
  `ReadingPlanRepository`'s doc comment for how that was confirmed against the actual data.

## Known gaps

- No cross-references or footnotes - no data source for either was ever provided.
- No Strong's numbers/word study tooling - same reason.
- TTS quality depends entirely on whatever voices are installed on the device; there's no
  bundled audio.
