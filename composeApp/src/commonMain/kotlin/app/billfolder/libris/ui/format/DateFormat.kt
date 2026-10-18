package app.billfolder.libris.ui.format

import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

// Calendar dates are shown as typed ("2026-10-16" → "16/10/2026"); nothing goes through an instant.

private val MONTHS = listOf(
    "janeiro", "fevereiro", "março", "abril", "maio", "junho",
    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
)

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

// Material's DatePicker works in UTC-midnight millis; converting through UTC (never the device zone) keeps the day.
fun isoFromPickerMillis(millis: Long): String =
    kotlin.time.Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date.toString()

fun pickerMillisFromIso(iso: String): Long =
    kotlinx.datetime.LocalDate.parse(iso).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
