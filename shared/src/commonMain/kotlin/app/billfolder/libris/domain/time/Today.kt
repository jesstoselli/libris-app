package app.billfolder.libris.domain.time

import kotlin.time.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// The API speaks calendar dates ("2026-10-16"); "today" is the device's local calendar day, never shifted.

/** The device's local calendar date, as the API expects for "today". */
fun today(clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDate =
    clock.now().toLocalDateTime(zone).date

fun currentYearMonth(clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): Pair<Int, Int> {
    val date = today(clock, zone)
    return date.year to date.month.ordinal + 1
}
