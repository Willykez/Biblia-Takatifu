# Changelog

All notable changes to this project are documented here.

## Unreleased — Rebuilt as Biblia Takatifu

- Full rewrite from a Sketchware/XML-layout skeleton to 100% Jetpack Compose.
- Adopted the design framework's theme system, bottom-nav pill, and Settings/Appearance
  screen unchanged; package renamed to `com.biblia.app`.
- New Bible data layer (`BibleDatabase`, `BibleRepository`, `BibleViewModel`) built against
  the bundled `bible_swahili.sqlite` (66 books, ~33.7k verse rows).
- New screens: Home (OT/NT book list), Chapters (picker grid), Reader (bilingual verse view
  with bookmark/highlight/note sheet), Search (full-text), Saved (bookmarks/highlights/notes).
- Settings restructured: kept Appearance verbatim, replaced the framework's file-transfer
  Connectivity/Storage sections with Reading preferences and a "Your Data" section (counts +
  clear bookmarks/highlights/notes).
- Removed a bundled `NotificationListenerService`-based module that read and stored the
  content of other apps' notifications (including OTP codes) behind a hidden PIN lock — out
  of scope for this app and not carried into the rewrite.
- Dropped Firebase/Google Services/Secrets Gradle plugins (unused, unconfigured in this repo).
