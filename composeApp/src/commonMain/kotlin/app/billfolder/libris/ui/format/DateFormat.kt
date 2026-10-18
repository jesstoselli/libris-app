package app.billfolder.libris.ui.format

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

private val MONTHS = listOf(
    "janeiro", "fevereiro", "março", "abril", "maio", "junho",
    "julho", "agosto", "setembro", "outubro", "novembro", "dezembro",
)

private fun Int.twoDigits() = toString().padStart(2, '0')

/** 16/10/2026 */
fun formatDate(date: LocalDate): String = "${date.day.twoDigits()}/${(date.month.ordinal + 1).twoDigits()}/${date.year}"

/** 16/10 */
fun formatShortDate(date: LocalDate): String = "${date.day.twoDigits()}/${(date.month.ordinal + 1).twoDigits()}"

fun monthName(month: Int): String = MONTHS[month - 1]

fun monthShort(month: Int): String = MONTHS[month - 1].take(3)

// Material's DatePicker works in UTC-midnight millis; converting through UTC (never the device zone) keeps the day.
fun dateFromPickerMillis(millis: Long): LocalDate =
    kotlin.time.Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date

fun pickerMillisFrom(date: LocalDate): Long = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
