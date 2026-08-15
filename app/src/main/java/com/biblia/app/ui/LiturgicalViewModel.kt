package com.biblia.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.biblia.app.data.BibleRepository
import com.biblia.app.data.liturgical.LectionaryRepository
import com.biblia.app.data.liturgical.LiturgicalResolver
import com.biblia.app.data.liturgical.ReadingRenderer
import com.biblia.app.data.liturgical.RenderedDay
import com.biblia.app.data.liturgical.ResolvedDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/**
 * Backs the Calendar and Today's Readings screens. Fully local now - readings, saints, and
 * the A/B/C + I/II cycle table all come from your bundled lectionary_swahili.sqlite via
 * LiturgicalResolver, not a network API. No loading/offline states needed for that reason;
 * [monthCache] just avoids re-resolving the same 30-odd days every time the calendar redraws.
 */
class LiturgicalViewModel(application: Application) : AndroidViewModel(application) {
    private val bibleRepository = BibleRepository(application)
    private val lectionaryRepository = LectionaryRepository(application)
    private val resolver = LiturgicalResolver(lectionaryRepository)
    private val renderer = ReadingRenderer(bibleRepository)

    private val _keepThursdaySolemnities = MutableStateFlow(false)
    val keepThursdaySolemnities: StateFlow<Boolean> = _keepThursdaySolemnities.asStateFlow()

    fun setKeepThursdaySolemnities(value: Boolean) {
        _keepThursdaySolemnities.value = value
    }

    private val monthCache = mutableMapOf<YearMonth, Map<LocalDate, ResolvedDay>>()

    suspend fun resolveDay(date: LocalDate): ResolvedDay = resolver.resolve(date)

    suspend fun resolveMonth(month: YearMonth): Map<LocalDate, ResolvedDay> {
        monthCache[month]?.let { return it }
        val result = (1..month.lengthOfMonth()).associate { day ->
            val date = month.atDay(day)
            date to resolver.resolve(date)
        }
        monthCache[month] = result
        return result
    }

    suspend fun renderDay(date: LocalDate): RenderedDay {
        val resolved = resolver.resolve(date)
        val readings = renderer.renderReadingSet(resolved.readings)
        return RenderedDay(resolved, readings)
    }
}
