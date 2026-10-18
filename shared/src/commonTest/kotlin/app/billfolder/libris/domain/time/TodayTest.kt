package app.billfolder.libris.domain.time

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

class TodayTest {

    private fun clockAt(iso: String) = object : Clock {
        override fun now(): Instant = Instant.parse(iso)
    }

    @Test
    fun todayIso_usesLocalCalendarDate() {
        // 23:30 in São Paulo is already the next day in UTC.
        assertEquals("2026-10-16", todayIso(clockAt("2026-10-17T02:30:00Z"), TimeZone.of("America/Sao_Paulo")))
    }
}
