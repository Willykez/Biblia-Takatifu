package com.biblia.app.data.liturgical

import java.time.LocalDate

enum class LiturgicalSeason { ADVENT, CHRISTMAS, ORDINARY_TIME, LENT, EASTER }

enum class SundayCycle { A, B, C }

enum class LiturgicalColor { VIOLET, WHITE, GREEN, RED, ROSE }

/**
 * Simplified rank for v1: this app does not yet carry the sanctoral cycle (saints'
 * memorials), so only the ranks that matter for what we actually compute/seed are here.
 * A full Table of Liturgical Days (GNLYC #59) has finer precedence classes than this -
 * this is intentionally a v1 subset, not the complete precedence system.
 */
enum class LiturgicalRank { SOLEMNITY, FEAST, SUNDAY, WEEKDAY }

/**
 * What LiturgicalCalendar.dayFor(date) computes for any date - pure function of the date,
 * no data lookup involved. This offline engine now mainly powers the calendar grid's
 * season/day-name display as a fallback for when the live LitCalRepository (see
 * LitCalRepository.kt) has no cached data yet - the live API is the source of truth for
 * exact feast names, ranks, colors and readings whenever it's reachable.
 */
data class LiturgicalDay(
    val date: LocalDate,
    val season: LiturgicalSeason,
    val isSunday: Boolean,
    val ordinaryTimeWeek: Int?,
    val sundayCycle: SundayCycle,
    val weekdayCycle: Int, // 1 or 2
    val rank: LiturgicalRank,
    val color: LiturgicalColor,
    val title: String,
    val lectionaryKey: String,
)
