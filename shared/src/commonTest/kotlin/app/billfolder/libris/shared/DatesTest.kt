package app.billfolder.libris.shared

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class DatesTest {

    private fun clockAt(iso: String) = object : Clock {
        override fun now(): Instant = Instant.parse(iso)
    }

    @Test
    fun todayIso_usesLocalCalendarDate() {
        // 23:30 in São Paulo is already the next day in UTC.
        assertEquals("2026-10-16", todayIso(clockAt("2026-10-17T02:30:00Z"), TimeZone.of("America/Sao_Paulo")))
    }

    @Test
    fun formatIsoDate_neverShifts() {
        assertEquals("16/10/2026", formatIsoDate("2026-10-16"))
        assertEquals("02/10", formatShortDate("2026-10-02"))
    }

    @Test
    fun monthNames() {
        assertEquals("outubro", monthName(10))
        assertEquals("mar", monthShort(3))
    }
}

class PickerDatesTest {
    @Test
    fun pickerMillis_roundTripKeepsTheDay() {
        val millis = pickerMillisFromIso("2026-10-16")
        assertEquals(1_792_108_800_000L, millis)
        assertEquals("2026-10-16", isoFromPickerMillis(millis))
    }
}
