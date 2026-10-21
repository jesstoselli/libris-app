package app.billfolder.libris.domain.model

import kotlinx.datetime.LocalDate

data class MonthStats(val month: Int, val pagesRead: Int, val booksFinished: Int)

/** [readWithoutDate] is every year's: read books whose finish nobody remembers, so no month counts them. */
data class YearStats(
    val year: Int,
    val booksFinished: Int,
    val pagesRead: Int,
    val readingNow: Int,
    val months: List<MonthStats>,
    val readWithoutDate: Int = 0,
)

data class FinishedBook(val bookId: String, val title: String, val authors: List<String>, val finishedAt: LocalDate)

data class ProgressedBook(val bookId: String, val title: String, val pagesRead: Int)

data class MonthDetail(val year: Int, val month: Int, val pagesRead: Int, val finished: List<FinishedBook>, val progressed: List<ProgressedBook>)
