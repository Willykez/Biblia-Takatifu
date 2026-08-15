package com.biblia.app.data.liturgical

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Movable-feast anchor dates for one civil year, all derived from Easter's computus. Follows
 * the spec in APP_LOGIC.md \u00a73 exactly, including its explicit choice to default
 * Ascension/Corpus Christi to their transferred Sunday (see [keepThursdaySolemnities]).
 */
data class LiturgicalAnchors(
    val ashWednesday: LocalDate,
    val palmSunday: LocalDate,
    val holyThursday: LocalDate,
    val goodFriday: LocalDate,
    val easterVigil: LocalDate,
    val easterSunday: LocalDate,
    val ascension: LocalDate,
    val pentecost: LocalDate,
    val trinitySunday: LocalDate,
    val corpusChristi: LocalDate,
    val sacredHeart: LocalDate,
    val firstSundayAdvent: LocalDate,
    val christTheKing: LocalDate,
)

object LiturgicalComputus {

    /** Anonymous Gregorian algorithm (Meeus/Jones/Butcher). */
    fun easterSunday(year: Int): LocalDate {
        val a = year % 19
        val b = year / 100
        val c = year % 100
        val d = b / 4
        val e = b % 4
        val f = (b + 8) / 25
        val g = (b - f + 1) / 3
        val h = (19 * a + b - d - g + 15) % 30
        val i = c / 4
        val k = c % 4
        val l = (32 + 2 * e + 2 * i - h - k) % 7
        val m = (a + 11 * h + 22 * l) / 451
        val month = (h + l - 7 * m + 114) / 31
        val day = (h + l - 7 * m + 114) % 31 + 1
        return LocalDate.of(year, month, day)
    }

    /** Sunday nearest Nov 30, i.e. the Sunday in [Nov 27, Dec 3]. */
    fun firstAdventSunday(year: Int): LocalDate {
        var d = LocalDate.of(year, 11, 27)
        while (d.dayOfWeek != DayOfWeek.SUNDAY) d = d.plusDays(1)
        return d
    }

    /**
     * @param keepThursdaySolemnities false (default) transfers Ascension and Corpus Christi
     *   to their following Sunday, matching most of the US Ordo; true keeps them on Thursday,
     *   matching the handful of provinces (and most of the rest of the world) that don't
     *   transfer. Exposed as a user setting per APP_LOGIC.md's explicit instruction not to
     *   hard-code one option.
     */
    fun anchorsFor(year: Int, keepThursdaySolemnities: Boolean = false): LiturgicalAnchors {
        val easter = easterSunday(year)
        val advent1 = firstAdventSunday(year)
        return LiturgicalAnchors(
            ashWednesday = easter.minusDays(46),
            palmSunday = easter.minusDays(7),
            holyThursday = easter.minusDays(3),
            goodFriday = easter.minusDays(2),
            easterVigil = easter.minusDays(1),
            easterSunday = easter,
            ascension = if (keepThursdaySolemnities) easter.plusDays(39) else easter.plusDays(42),
            pentecost = easter.plusDays(49),
            trinitySunday = easter.plusDays(56),
            corpusChristi = if (keepThursdaySolemnities) easter.plusDays(60) else easter.plusDays(63),
            sacredHeart = easter.plusDays(68),
            firstSundayAdvent = advent1,
            christTheKing = advent1.minusDays(7),
        )
    }
}
