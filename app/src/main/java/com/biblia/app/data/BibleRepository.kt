package com.biblia.app.data

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val VERSE_COLS =
    "_id, chapter_id, chapter_num, position, rank, text, head, " +
        "bookmark, highlight, note, bookmark_date, highlight_date, note_date"

class BibleRepository(context: Context) {
    private val appContext = context.applicationContext
    private val db: SQLiteDatabase get() = BibleDatabase.getInstance(appContext)

    // ── Books ────────────────────────────────────────────────────────────────

    suspend fun getBooksByMode(mode: Int): List<BibleBook> = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT _id, title, num, mode, short_title FROM chapters WHERE mode = ? ORDER BY _id ASC",
            arrayOf(mode.toString()),
        ).use { c -> buildList { while (c.moveToNext()) add(cursorToBook(c)) } }
    }

    suspend fun getBookById(id: Int): BibleBook? = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT _id, title, num, mode, short_title FROM chapters WHERE _id = ? LIMIT 1",
            arrayOf(id.toString()),
        ).use { c -> if (c.moveToFirst()) cursorToBook(c) else null }
    }

    suspend fun searchBooks(query: String): List<BibleBook> = withContext(Dispatchers.IO) {
        val like = "%$query%"
        db.rawQuery(
            "SELECT _id, title, num, mode, short_title FROM chapters WHERE title LIKE ? OR short_title LIKE ? ORDER BY _id ASC",
            arrayOf(like, like),
        ).use { c -> buildList { while (c.moveToNext()) add(cursorToBook(c)) } }
    }

    // ── Verses ───────────────────────────────────────────────────────────────

    suspend fun getVerses(bookId: Int, chapterNum: Int): List<BibleVerse> = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT $VERSE_COLS FROM texts WHERE chapter_id = ? AND chapter_num = ? ORDER BY rank ASC",
            arrayOf(bookId.toString(), chapterNum.toString()),
        ).use { c -> buildList { while (c.moveToNext()) add(cursorToVerse(c)) } }
    }

    suspend fun searchVerses(query: String, limit: Int = 300): List<BibleVerse> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        db.rawQuery(
            "SELECT $VERSE_COLS FROM texts WHERE text LIKE ? AND head = 0 LIMIT ?",
            arrayOf("%$query%", limit.toString()),
        ).use { c -> buildList { while (c.moveToNext()) add(cursorToVerse(c)) } }
    }

    // ── Saved content ────────────────────────────────────────────────────────

    suspend fun getBookmarks(): List<BibleVerse> = withContext(Dispatchers.IO) {
        db.rawQuery("SELECT $VERSE_COLS FROM texts WHERE bookmark = 1 ORDER BY bookmark_date DESC", null)
            .use { c -> buildList { while (c.moveToNext()) add(cursorToVerse(c)) } }
    }

    suspend fun getHighlights(): List<BibleVerse> = withContext(Dispatchers.IO) {
        db.rawQuery("SELECT $VERSE_COLS FROM texts WHERE highlight > 0 ORDER BY highlight_date DESC", null)
            .use { c -> buildList { while (c.moveToNext()) add(cursorToVerse(c)) } }
    }

    suspend fun getNotes(): List<BibleVerse> = withContext(Dispatchers.IO) {
        db.rawQuery(
            "SELECT $VERSE_COLS FROM texts WHERE note IS NOT NULL AND note != '' ORDER BY note_date DESC",
            null,
        ).use { c -> buildList { while (c.moveToNext()) add(cursorToVerse(c)) } }
    }

    suspend fun getDataCounts(): BibleDataCounts = withContext(Dispatchers.IO) {
        BibleDataCounts(
            bookmarks = scalarInt("SELECT COUNT(*) FROM texts WHERE bookmark = 1"),
            highlights = scalarInt("SELECT COUNT(*) FROM texts WHERE highlight > 0"),
            notes = scalarInt("SELECT COUNT(*) FROM texts WHERE note IS NOT NULL AND note != ''"),
        )
    }

    // ── Write ────────────────────────────────────────────────────────────────

    suspend fun setBookmark(verseId: Int, bookmarked: Boolean) = withContext(Dispatchers.IO) {
        db.execSQL(
            "UPDATE texts SET bookmark = ?, bookmark_date = ? WHERE _id = ?",
            arrayOf(if (bookmarked) 1 else 0, if (bookmarked) System.currentTimeMillis() else 0, verseId),
        )
    }

    suspend fun setHighlight(verseId: Int, colorIndex: Int) = withContext(Dispatchers.IO) {
        db.execSQL(
            "UPDATE texts SET highlight = ?, highlight_date = ? WHERE _id = ?",
            arrayOf(colorIndex, if (colorIndex > 0) System.currentTimeMillis() else 0, verseId),
        )
    }

    suspend fun setNote(verseId: Int, note: String?) = withContext(Dispatchers.IO) {
        db.execSQL(
            "UPDATE texts SET note = ?, note_date = ? WHERE _id = ?",
            arrayOf(note, if (!note.isNullOrBlank()) System.currentTimeMillis() else 0, verseId),
        )
    }

    suspend fun clearAllBookmarks() = withContext(Dispatchers.IO) {
        db.execSQL("UPDATE texts SET bookmark = 0, bookmark_date = 0 WHERE bookmark = 1")
    }

    suspend fun clearAllHighlights() = withContext(Dispatchers.IO) {
        db.execSQL("UPDATE texts SET highlight = 0, highlight_date = 0 WHERE highlight > 0")
    }

    suspend fun clearAllNotes() = withContext(Dispatchers.IO) {
        db.execSQL("UPDATE texts SET note = NULL, note_date = 0 WHERE note IS NOT NULL AND note != ''")
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun scalarInt(sql: String): Int =
        db.rawQuery(sql, null).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    private fun cursorToBook(c: Cursor): BibleBook = BibleBook(
        id = c.getInt(0),
        title = c.getString(1),
        numChapters = c.getInt(2),
        mode = c.getInt(3),
        shortTitle = c.getString(4) ?: "",
    )

    private fun cursorToVerse(c: Cursor): BibleVerse = BibleVerse(
        id = c.getInt(0),
        bookId = c.getInt(1),
        chapterNum = c.getInt(2),
        position = c.getInt(3),
        rank = c.getInt(4),
        text = c.getString(5) ?: "",
        head = c.getInt(6) == 1,
        bookmark = c.getInt(7) == 1,
        highlight = c.getInt(8),
        note = c.getString(9),
        bookmarkDate = c.getLong(10),
        highlightDate = c.getLong(11),
        noteDate = c.getLong(12),
    )
}
