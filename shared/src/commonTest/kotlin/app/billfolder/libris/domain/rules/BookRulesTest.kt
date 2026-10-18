package app.billfolder.libris.domain.rules

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BookRulesTest {
    private val today = LocalDate(2026, 10, 16)

    @Test
    fun readingDates_finishCannotPrecedeStart() =
        assertEquals(DateProblem.FinishBeforeStart, readingDatesProblem(LocalDate(2024, 3, 20), LocalDate(2024, 3, 1), today))

    @Test
    fun readingDates_cannotBeInTheFuture() {
        assertEquals(DateProblem.InFuture, readingDatesProblem(null, LocalDate(2027, 1, 10), today))
        assertEquals(DateProblem.InFuture, readingDatesProblem(LocalDate(2026, 10, 17), null, today))
    }

    @Test
    fun readingDates_todayAndOrderedPastAreFine() {
        assertNull(readingDatesProblem(LocalDate(2024, 3, 1), today, today))
        assertNull(readingDatesProblem(null, null, today))
    }

    @Test
    fun genres_areAddedOnce_ignoringCase_withTheStoredSpelling() {
        val known = listOf("Ficção Científica")
        assertEquals(listOf("Terror", "Ficção Científica"), listOf("Terror").withGenre(" ficção científica ", known))
        assertEquals(listOf("Terror"), listOf("Terror").withGenre("TERROR", known))
        assertEquals(listOf("Terror"), listOf("Terror").withGenre(" , ", known))
    }

    @Test
    fun genres_stopAtTheLimit() {
        val full = (1..BookRules.MAX_GENRES).map { "G$it" }
        assertEquals(full, full.withGenre("Outro", emptyList()))
    }

    @Test
    fun limits_mirrorTheApi() {
        assertTrue(20000 in BookRules.PAGES && 0 !in BookRules.PAGES && 20001 !in BookRules.PAGES)
        assertTrue(1400 in BookRules.YEARS && 2101 !in BookRules.YEARS)
        assertEquals(300, BookRules.MAX_TITLE)
    }

    @Test
    fun isbn_scannedBarcodesAreBookland() {
        assertTrue(looksLikeIsbn("9786556928784"))
        assertFalse(looksLikeIsbn("7891000100103"))
    }

    @Test
    fun isbn_searchableOnceItHasTenDigits() {
        assertTrue(isSearchableIsbn("85-359-0277-X"))
        assertFalse(isSearchableIsbn("123456789"))
    }
}
