package com.biblia.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biblia.app.data.BibleRepository
import com.biblia.app.data.liturgical.LitCalEvent
import com.biblia.app.data.liturgical.LitCalRepository
import com.biblia.app.data.liturgical.LiturgicalCalendar
import com.biblia.app.data.liturgical.LiturgicalDay
import com.biblia.app.data.liturgical.ReadingRenderer
import com.biblia.app.data.liturgical.RenderedDay
import com.biblia.app.data.liturgical.RenderedReading
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

sealed class CalendarLoadState {
    data object Loading : CalendarLoadState()
    data class Loaded(val fromCache: Boolean) : CalendarLoadState()
    data class Offline(val hasCachedData: Boolean, val message: String) : CalendarLoadState()
}

/**
 * Backs the Calendar and Today's Readings screens. Source of truth is the live
 * LiturgicalCalendarAPI (LitCalRepository, cached on disk per year); LiturgicalCalendar's
 * offline date math is the fallback used for season colour/day naming on dates the API
 * hasn't been fetched for yet, so the calendar grid never looks empty.
 */
class LiturgicalViewModel(application: Application) : AndroidViewModel(application) {
    private val bibleRepository = BibleRepository(application)
    private val litCalRepository = LitCalRepository(application)
    private val renderer = ReadingRenderer(bibleRepository)

    private val _loadState = MutableStateFlow<CalendarLoadState>(CalendarLoadState.Loading)
    val loadState: StateFlow<CalendarLoadState> = _loadState.asStateFlow()

    private val eventsByYear = mutableMapOf<Int, List<LitCalEvent>>()

    fun offlineDayFor(date: LocalDate): LiturgicalDay = LiturgicalCalendar.dayFor(date)

    fun ensureYearLoaded(year: Int, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _loadState.value = CalendarLoadState.Loading
            when (val result = litCalRepository.getYear(year, forceRefresh = forceRefresh)) {
                is LitCalRepository.YearResult.Success -> {
                    eventsByYear[year] = result.events
                    _loadState.value = CalendarLoadState.Loaded(fromCache = result.fromCache)
                }
                is LitCalRepository.YearResult.Failure -> {
                    result.staleCache?.let { eventsByYear[year] = it }
                    _loadState.value = CalendarLoadState.Offline(
                        hasCachedData = result.staleCache != null,
                        message = result.message,
                    )
                }
            }
        }
    }

    fun eventsForDate(date: LocalDate): List<LitCalEvent> =
        (eventsByYear[date.year] ?: emptyList()).filter { it.date == date && !it.isVigilMass }

    fun eventsInMonth(month: YearMonth): Map<LocalDate, List<LitCalEvent>> =
        (eventsByYear[month.year] ?: emptyList())
            .filter { !it.isVigilMass && it.date.month == month.month && it.date.year == month.year }
            .groupBy { it.date }

    suspend fun renderDay(date: LocalDate): RenderedDay {
        val events = eventsForDate(date)
        val readingsByEvent = mutableMapOf<String, List<RenderedReading>>()
        for (event in events) {
            readingsByEvent[event.eventKey] = renderer.renderEvent(event)
        }
        return RenderedDay(date, events, readingsByEvent)
    }

    fun lastSyncedLabel(year: Int): String? = litCalRepository.lastSyncedLabel(year)
}
