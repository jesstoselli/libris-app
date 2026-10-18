package app.billfolder.libris.domain.time

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// The API speaks calendar dates ("2026-10-16"); "today" is the device's local calendar day, never shifted.

/** The device's local calendar date, as the API expects for "today". */
fun todayIso(clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): String =
    clock.now().toLocalDateTime(zone).date.toString()

fun currentYearMonth(clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): Pair<Int, Int> {
    val date = clock.now().toLocalDateTime(zone).date
    return date.year to date.month.ordinal + 1
}
