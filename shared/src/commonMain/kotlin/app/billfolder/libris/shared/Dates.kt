package app.billfolder.libris.shared

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// The API speaks calendar dates ("2026-10-16"); none of these go through an instant, so nothing shifts a day.

private val MONTHS = listOf(
    "janeiro", "fevereiro", "março", "abril", "maio", "junho",
    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
)

/** The device's local calendar date, as the API expects for "today". */
fun todayIso(clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): String =
    clock.now().toLocalDateTime(zone).date.toString()

fun currentYearMonth(clock: Clock = Clock.System, zone: TimeZone = TimeZone.currentSystemDefault()): Pair<Int, Int> {
    val date = clock.now().toLocalDateTime(zone).date
    return date.year to date.month.ordinal + 1
}

fun formatIsoDate(iso: String): String {
    val (year, month, day) = iso.split("-")
    return "$day/$month/$year"
}

fun formatShortDate(iso: String): String {
    val (_, month, day) = iso.split("-")
    return "$day/$month"
}

fun monthName(month: Int): String = MONTHS[month - 1]

fun monthShort(month: Int): String = MONTHS[month - 1].take(3)
