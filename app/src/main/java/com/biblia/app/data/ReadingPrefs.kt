package com.biblia.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.readingPrefsDataStore by preferencesDataStore(name = "biblia_reading_prefs")

enum class ReaderFontStyle { SANS, SERIF, MONO }

data class ReadingState(
    val fontSizeSp: Int = 17,
    val fontStyle: ReaderFontStyle = ReaderFontStyle.SERIF,
    val bilingual: Boolean = true,
    val showVerseNumbers: Boolean = true,
    val justifyText: Boolean = false,
    val lastBookId: Int = 8022, // Mwanzo / Genesis
    val lastChapterNum: Int = 1,
    val lastBookTitle: String = "Mwanzo",
)

class ReadingPrefs(private val context: Context) {
    private object Keys {
        val FONT_SIZE = intPreferencesKey("font_size")
        val FONT_STYLE = stringPreferencesKey("font_style")
        val BILINGUAL = booleanPreferencesKey("bilingual")
        val SHOW_VERSE_NUMBERS = booleanPreferencesKey("show_verse_numbers")
        val JUSTIFY_TEXT = booleanPreferencesKey("justify_text")
        val LAST_BOOK_ID = intPreferencesKey("last_book_id")
        val LAST_CHAPTER_NUM = intPreferencesKey("last_chapter_num")
        val LAST_BOOK_TITLE = stringPreferencesKey("last_book_title")
    }

    val state: Flow<ReadingState> = context.readingPrefsDataStore.data.map { p ->
        ReadingState(
            fontSizeSp = p[Keys.FONT_SIZE] ?: 17,
            fontStyle = runCatching { ReaderFontStyle.valueOf(p[Keys.FONT_STYLE] ?: "") }
                .getOrDefault(ReaderFontStyle.SERIF),
            bilingual = p[Keys.BILINGUAL] ?: true,
            showVerseNumbers = p[Keys.SHOW_VERSE_NUMBERS] ?: true,
            justifyText = p[Keys.JUSTIFY_TEXT] ?: false,
            lastBookId = p[Keys.LAST_BOOK_ID] ?: 8022,
            lastChapterNum = p[Keys.LAST_CHAPTER_NUM] ?: 1,
            lastBookTitle = p[Keys.LAST_BOOK_TITLE] ?: "Mwanzo",
        )
    }

    suspend fun setFontSize(sp: Int) {
        context.readingPrefsDataStore.edit { it[Keys.FONT_SIZE] = sp.coerceIn(12, 28) }
    }

    suspend fun setFontStyle(style: ReaderFontStyle) {
        context.readingPrefsDataStore.edit { it[Keys.FONT_STYLE] = style.name }
    }

    suspend fun setBilingual(value: Boolean) {
        context.readingPrefsDataStore.edit { it[Keys.BILINGUAL] = value }
    }

    suspend fun setShowVerseNumbers(value: Boolean) {
        context.readingPrefsDataStore.edit { it[Keys.SHOW_VERSE_NUMBERS] = value }
    }

    suspend fun setJustifyText(value: Boolean) {
        context.readingPrefsDataStore.edit { it[Keys.JUSTIFY_TEXT] = value }
    }

    suspend fun setLastRead(bookId: Int, chapterNum: Int, bookTitle: String) {
        context.readingPrefsDataStore.edit {
            it[Keys.LAST_BOOK_ID] = bookId
            it[Keys.LAST_CHAPTER_NUM] = chapterNum
            it[Keys.LAST_BOOK_TITLE] = bookTitle
        }
    }
}
