package app.billfolder.libris.domain.model

import kotlinx.datetime.LocalDate

data class ReadingLog(val id: String, val date: LocalDate, val page: Int, val fromPage: Int, val pagesRead: Int)

/** A log change also moves the book's progress, so the API answers with both. */
data class ReadingProgress(val book: Book, val logs: List<ReadingLog>)
