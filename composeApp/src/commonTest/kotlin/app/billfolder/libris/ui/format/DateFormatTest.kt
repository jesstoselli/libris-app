package app.billfolder.libris.ui.format

import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlin.test.assertEquals

class DateFormatTest {
    @Test
    fun formatIsoDate_neverShifts() {
        assertEquals("16/10/2026", formatDate(LocalDate(2026, 10, 16)))
        assertEquals("02/10", formatShortDate(LocalDate(2026, 10, 2)))
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
        val millis = pickerMillisFrom(LocalDate(2026, 10, 16))
        assertEquals(1_792_108_800_000L, millis)
        assertEquals(LocalDate(2026, 10, 16), dateFromPickerMillis(millis))
    }
}
