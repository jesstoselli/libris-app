package app.billfolder.libris.ui.format

import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatTest {
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
