package app.billfolder.libris.data.remote

import app.billfolder.libris.data.remote.dto.BookDto
import app.billfolder.libris.data.remote.dto.BookFormatDto
import app.billfolder.libris.data.remote.dto.BookOwnershipDto
import app.billfolder.libris.data.remote.dto.BookStatusDto
import app.billfolder.libris.data.remote.dto.CoverDto
import app.billfolder.libris.data.remote.dto.CreateBookRequest
import app.billfolder.libris.data.remote.dto.GenreDto
import app.billfolder.libris.data.remote.dto.IsbnLookupDto
import app.billfolder.libris.data.remote.dto.MonthDetailDto
import app.billfolder.libris.data.remote.dto.ReadingLogDto
import app.billfolder.libris.data.remote.dto.UpdateBookRequest
import app.billfolder.libris.data.remote.dto.UserDto
import app.billfolder.libris.data.remote.dto.YearStatsDto
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookDraft
import app.billfolder.libris.domain.model.BookFormat
import app.billfolder.libris.domain.model.BookLookup
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.Cover
import app.billfolder.libris.domain.model.FinishedBook
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.model.GenreRef
import app.billfolder.libris.domain.model.LookupSource
import app.billfolder.libris.domain.model.MonthDetail
import app.billfolder.libris.domain.model.MonthStats
import app.billfolder.libris.domain.model.ProgressedBook
import app.billfolder.libris.domain.model.ReadingLog
import app.billfolder.libris.domain.model.User
import app.billfolder.libris.domain.model.YearStats
import kotlinx.datetime.LocalDate

// Wire ↔ domain. Dates travel as "YYYY-MM-DD" and become LocalDate without any time zone, so no day ever shifts.

internal fun UserDto.toDomain() = User(id, email, displayName)

internal fun BookOwnershipDto.toDomain() = when (this) {
    BookOwnershipDto.Owned -> BookOwnership.Owned
    BookOwnershipDto.Wishlist -> BookOwnership.Wishlist
}

internal fun BookOwnership.toDto() = when (this) {
    BookOwnership.Owned -> BookOwnershipDto.Owned
    BookOwnership.Wishlist -> BookOwnershipDto.Wishlist
}

internal fun BookFormatDto.toDomain() = when (this) {
    BookFormatDto.Physical -> BookFormat.Physical
    BookFormatDto.Digital -> BookFormat.Digital
}

internal fun BookFormat.toDto() = when (this) {
    BookFormat.Physical -> BookFormatDto.Physical
    BookFormat.Digital -> BookFormatDto.Digital
}

private fun Collection<BookFormat>.toWire() = sortedBy { it.ordinal }.map { it.toDto() }

internal fun BookStatusDto.toDomain() = when (this) {
    BookStatusDto.NotStarted -> BookStatus.NotStarted
    BookStatusDto.Reading -> BookStatus.Reading
    BookStatusDto.Read -> BookStatus.Read
    BookStatusDto.Abandoned -> BookStatus.Abandoned
}

internal fun BookStatus.toDto() = when (this) {
    BookStatus.NotStarted -> BookStatusDto.NotStarted
    BookStatus.Reading -> BookStatusDto.Reading
    BookStatus.Read -> BookStatusDto.Read
    BookStatus.Abandoned -> BookStatusDto.Abandoned
}

private fun String.toDate() = LocalDate.parse(this)

/** [coverSrc] turns the API's relative cover paths into URLs an image loader can fetch. */
internal fun BookDto.toDomain(coverSrc: (String?) -> String?) = Book(
    id = id,
    title = title,
    authors = authors,
    isbn = isbn,
    totalPages = totalPages,
    publisher = publisher,
    publishedYear = publishedYear,
    synopsis = synopsis,
    coverId = coverId,
    coverUrl = coverSrc(coverUrl),
    genres = genres.map { GenreRef(it.id, it.name) },
    ownership = ownership.toDomain(),
    status = status?.toDomain(),
    startedAt = startedAt?.toDate(),
    finishedAt = finishedAt?.toDate(),
    addedAt = addedAt.toDate(),
    currentPage = currentPage,
    percent = percent,
    formats = formats.map { it.toDomain() }.distinct().sortedBy { it.ordinal },
)

internal fun BookDraft.toCreate(today: LocalDate) = CreateBookRequest(
    title, authors, isbn, totalPages, publisher, publishedYear, synopsis, coverId, genres,
    ownership.toDto(), status?.toDto(), startedAt?.toString(), finishedAt?.toString(), today.toString(), formats.toWire(),
)

internal fun BookDraft.toUpdate() = UpdateBookRequest(
    title, authors, isbn, totalPages, publisher, publishedYear, synopsis, coverId, genres, startedAt?.toString(), finishedAt?.toString(), formats.toWire(),
)

internal fun ReadingLogDto.toDomain() = ReadingLog(id, date.toDate(), page, fromPage, pagesRead)

internal fun GenreDto.toDomain() = Genre(id, name, bookCount)

internal fun CoverDto.toDomain(coverSrc: (String?) -> String?) = Cover(coverId, coverSrc(url) ?: url)

internal fun IsbnLookupDto.toDomain() = BookLookup(
    isbn, title, authors, totalPages, publisher, publishedYear, synopsis, coverUrl, suggestedGenres,
    sources = source.split('+').mapNotNull {
        when (it) {
            "google_books" -> LookupSource.GoogleBooks
            "open_library" -> LookupSource.OpenLibrary
            else -> null
        }
    },
)

internal fun YearStatsDto.toDomain() = YearStats(year, booksFinished, pagesRead, readingNow, months.map { MonthStats(it.month, it.pagesRead, it.booksFinished) })

internal fun MonthDetailDto.toDomain() = MonthDetail(
    year, month, pagesRead,
    finished.map { FinishedBook(it.bookId, it.title, it.authors, it.finishedAt.toDate()) },
    progressed.map { ProgressedBook(it.bookId, it.title, it.pagesRead) },
)
