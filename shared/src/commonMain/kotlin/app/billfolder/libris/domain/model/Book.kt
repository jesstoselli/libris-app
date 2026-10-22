package app.billfolder.libris.domain.model

import kotlinx.datetime.LocalDate

enum class BookOwnership { Owned, Wishlist }

enum class BookStatus { NotStarted, Reading, Read, Abandoned }

/** A book can be both; physical comes first wherever they are listed. */
enum class BookFormat { Physical, Digital }

data class GenreRef(val id: String, val name: String)

/** A book as the app sees it: real dates, and [coverUrl] already absolute so any image loader can fetch it. */
data class Book(
    val id: String,
    val title: String,
    val authors: List<String>,
    val isbn: String? = null,
    val totalPages: Int? = null,
    val publisher: String? = null,
    val publishedYear: Int? = null,
    val synopsis: String? = null,
    val coverId: String? = null,
    val coverUrl: String? = null,
    val genres: List<GenreRef> = emptyList(),
    val ownership: BookOwnership,
    /** Null for wishlist books, which have no reading yet. */
    val status: BookStatus? = null,
    val startedAt: LocalDate? = null,
    val finishedAt: LocalDate? = null,
    val addedAt: LocalDate,
    val currentPage: Int = 0,
    val percent: Int = 0,
    val formats: List<BookFormat> = listOf(BookFormat.Physical),
    /** ISO 639-1: "pt", "en", "fr"… */
    val language: String = "pt",
    /** The series as the reader names it; the number may be 1.5 (a novella) or 0 (a prequel). */
    val series: String? = null,
    val seriesNumber: Double? = null,
    /** 0.5 to 5 in halves; only read or abandoned books carry one. */
    val rating: Double? = null,
)

/** What the user fills in to create or edit a book. [ownership] and [status] only count when creating. */
data class BookDraft(
    val title: String,
    val authors: List<String>,
    val isbn: String? = null,
    val totalPages: Int? = null,
    val publisher: String? = null,
    val publishedYear: Int? = null,
    val synopsis: String? = null,
    val coverId: String? = null,
    val genres: List<String> = emptyList(),
    val ownership: BookOwnership = BookOwnership.Owned,
    val status: BookStatus? = null,
    val startedAt: LocalDate? = null,
    val finishedAt: LocalDate? = null,
    val formats: Set<BookFormat> = setOf(BookFormat.Physical),
    /** Only when creating a book that is being read or was abandoned: the page already reached. */
    val currentPage: Int? = null,
    val language: String = "pt",
    val series: String? = null,
    val seriesNumber: Double? = null,
)

fun Book.toDraft() = BookDraft(
    title, authors, isbn, totalPages, publisher, publishedYear, synopsis, coverId,
    genres.map { it.name }, ownership, status, startedAt, finishedAt, formats.toSet(), language = language,
    series = series, seriesNumber = seriesNumber,
)

/** One series and its books in the Acervo and the Wishlist. */
data class Series(val name: String, val bookCount: Int, val wishlistCount: Int)

/** A day of the reading calendar. */
data class DayStats(val date: LocalDate, val pagesRead: Int, val books: List<ProgressedBook>)

data class BookFilter(
    val ownership: BookOwnership? = null,
    val status: BookStatus? = null,
    val genreId: String? = null,
    val query: String? = null,
    val format: BookFormat? = null,
    val language: String? = null,
    val author: String? = null,
    /** One spelling exactly as on the book. */
    val publisher: String? = null,
    val series: String? = null,
    val minRating: Double? = null,
    val byRating: Boolean = false,
)

/** One spelling of a publisher and its books in the Acervo and the Wishlist. */
data class Publisher(val name: String, val bookCount: Int, val wishlistCount: Int)

data class Cover(val id: String, val url: String)

enum class LookupSource { GoogleBooks, OpenLibrary }

/** Book data found for an ISBN; the cover is only a link until the book is saved. */
data class BookLookup(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val totalPages: Int? = null,
    val publisher: String? = null,
    val publishedYear: Int? = null,
    val synopsis: String? = null,
    val coverUrl: String? = null,
    val genres: List<String> = emptyList(),
    val sources: List<LookupSource> = emptyList(),
    /** When the catalogue says; null otherwise. */
    val language: String? = null,
)
