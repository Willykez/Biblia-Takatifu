package com.biblia.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biblia.app.data.AppearancePrefs
import com.biblia.app.data.BibleBook
import com.biblia.app.data.BibleDataCounts
import com.biblia.app.data.BibleRepository
import com.biblia.app.data.BibleVerse
import com.biblia.app.data.ReadingPrefs
import com.biblia.app.data.ReadingState
import com.biblia.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Backs Home, Reader, Search, Saved and Settings.
 *
 * Replaces the framework's file-transfer PulseViewModel: same shape (AndroidViewModel,
 * exposed as StateFlows for Compose to collect), different domain.
 */
class BibleViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = BibleRepository(application)
    private val readingPrefs = ReadingPrefs(application)
    private val appearancePrefs = AppearancePrefs(application)

    val readingState: StateFlow<ReadingState> = readingPrefs.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReadingState())

    val themeMode: StateFlow<ThemeMode> = appearancePrefs.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeMode.SYSTEM)

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { appearancePrefs.setThemeMode(mode) }

    private val _oldTestament = MutableStateFlow<List<BibleBook>>(emptyList())
    val oldTestament: StateFlow<List<BibleBook>> = _oldTestament.asStateFlow()

    private val _newTestament = MutableStateFlow<List<BibleBook>>(emptyList())
    val newTestament: StateFlow<List<BibleBook>> = _newTestament.asStateFlow()

    private val _dataCounts = MutableStateFlow(BibleDataCounts())
    val dataCounts: StateFlow<BibleDataCounts> = _dataCounts.asStateFlow()

    init {
        viewModelScope.launch {
            _oldTestament.value = repository.getBooksByMode(1)
            _newTestament.value = repository.getBooksByMode(2)
        }
        refreshDataCounts()
    }

    fun refreshDataCounts() {
        viewModelScope.launch { _dataCounts.value = repository.getDataCounts() }
    }

    suspend fun getBook(id: Int): BibleBook? = repository.getBookById(id)

    suspend fun getVerses(bookId: Int, chapterNum: Int): List<BibleVerse> =
        repository.getVerses(bookId, chapterNum)

    suspend fun searchVerses(query: String): List<BibleVerse> = repository.searchVerses(query)

    suspend fun searchBooks(query: String): List<BibleBook> = repository.searchBooks(query)

    suspend fun getBookmarks(): List<BibleVerse> = repository.getBookmarks()

    suspend fun getHighlights(): List<BibleVerse> = repository.getHighlights()

    suspend fun getNotes(): List<BibleVerse> = repository.getNotes()

    fun toggleBookmark(verse: BibleVerse) {
        viewModelScope.launch {
            repository.setBookmark(verse.id, !verse.bookmark)
            refreshDataCounts()
        }
    }

    fun setHighlight(verse: BibleVerse, colorIndex: Int) {
        viewModelScope.launch {
            repository.setHighlight(verse.id, colorIndex)
            refreshDataCounts()
        }
    }

    fun setNote(verse: BibleVerse, note: String?) {
        viewModelScope.launch {
            repository.setNote(verse.id, note)
            refreshDataCounts()
        }
    }

    fun clearAllBookmarks() {
        viewModelScope.launch { repository.clearAllBookmarks(); refreshDataCounts() }
    }

    fun clearAllHighlights() {
        viewModelScope.launch { repository.clearAllHighlights(); refreshDataCounts() }
    }

    fun clearAllNotes() {
        viewModelScope.launch { repository.clearAllNotes(); refreshDataCounts() }
    }

    fun setLastRead(bookId: Int, chapterNum: Int, bookTitle: String) {
        viewModelScope.launch { readingPrefs.setLastRead(bookId, chapterNum, bookTitle) }
    }

    fun setBilingual(value: Boolean) = viewModelScope.launch { readingPrefs.setBilingual(value) }
    fun setShowVerseNumbers(value: Boolean) = viewModelScope.launch { readingPrefs.setShowVerseNumbers(value) }
    fun setJustifyText(value: Boolean) = viewModelScope.launch { readingPrefs.setJustifyText(value) }
    fun setFontSize(sp: Int) = viewModelScope.launch { readingPrefs.setFontSize(sp) }
}
