package app.billfolder.libris.data.repository

import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.data.remote.LibrisApi
import app.billfolder.libris.data.remote.dto.BookListQuery
import app.billfolder.libris.data.remote.toCreate
import app.billfolder.libris.data.remote.toDomain
import app.billfolder.libris.data.remote.toDto
import app.billfolder.libris.data.remote.toUpdate
import app.billfolder.libris.data.remote.dto.ReadingLogResultDto
import app.billfolder.libris.domain.model.Publisher
import app.billfolder.libris.domain.model.DayStats
import app.billfolder.libris.domain.model.Series
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookDraft
import app.billfolder.libris.domain.model.BookFilter
import app.billfolder.libris.domain.model.BookLookup
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.Cover
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.model.MonthDetail
import app.billfolder.libris.domain.model.ReadingLog
import app.billfolder.libris.domain.model.ReadingProgress
import app.billfolder.libris.domain.model.YearStats
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.CoverRepository
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.domain.repository.ReadingRepository
import app.billfolder.libris.domain.repository.StatsRepository
import app.billfolder.libris.domain.time.today as localToday
import kotlinx.datetime.LocalDate

// Each repository hides the access token (and its renewal) behind AuthSession.authorized and speaks domain models.

class RemoteBookRepository(
    private val api: LibrisApi,
    private val session: AuthSession,
    private val today: () -> LocalDate = { localToday() },
) : BookRepository {
    override suspend fun list(filter: BookFilter): List<Book> {
        val query = BookListQuery(filter.ownership?.toDto(), filter.status?.toDto(), filter.genreId, filter.query?.trim()?.ifBlank { null }, filter.format?.toDto(), filter.language, filter.author, filter.publisher, filter.series, filter.minRating, "rating".takeIf { filter.byRating })
        return session.authorized { api.listBooks(it, query) }.map { it.toDomain(api::coverSrc) }
    }

    override suspend fun get(id: String): Book = session.authorized { api.getBook(it, id) }.toDomain(api::coverSrc)

    override suspend fun authors(): List<String> = session.authorized { api.listAuthors(it) }

    override suspend fun publishers(): List<String> = session.authorized { api.listPublishers(it) }

    override suspend fun seriesSummary(): List<Series> =
        session.authorized { api.seriesSummary(it) }.map { Series(it.name, it.bookCount, it.wishlistCount) }

    override suspend fun seriesBooks(name: String): List<Book> = session.authorized { api.seriesBooks(it, name) }.map { it.toDomain(api::coverSrc) }

    override suspend fun renameSeries(from: String, to: String): Int = session.authorized { api.renameSeries(it, from, to) }.updated

    override suspend fun rate(id: String, rating: Double?): Book = session.authorized { api.rate(it, id, rating) }.toDomain(api::coverSrc)

    override suspend fun publisherSummary(): List<Publisher> =
        session.authorized { api.publisherSummary(it) }.map { Publisher(it.name, it.bookCount, it.wishlistCount) }

    override suspend fun renamePublisher(from: String, to: String): Int = session.authorized { api.renamePublisher(it, from, to) }.updated

    override suspend fun create(draft: BookDraft): Book =
        session.authorized { api.createBook(it, draft.toCreate(today())) }.toDomain(api::coverSrc)

    override suspend fun update(id: String, draft: BookDraft): Book =
        session.authorized { api.updateBook(it, id, draft.toUpdate()) }.toDomain(api::coverSrc)

    override suspend fun delete(id: String) = session.authorized { api.deleteBook(it, id) }

    override suspend fun changeStatus(id: String, status: BookStatus, withoutDate: Boolean, finishedAt: LocalDate?): Book =
        session.authorized { api.changeStatus(it, id, status.toDto(), today().toString(), withoutDate, finishedAt?.toString()) }.toDomain(api::coverSrc)

    override suspend fun acquire(id: String, totalPages: Int?): Book =
        session.authorized { api.acquire(it, id, today().toString(), totalPages) }.toDomain(api::coverSrc)

    override suspend fun lookUp(isbn: String): BookLookup = session.authorized { api.lookupIsbn(it, isbn) }.toDomain()
}

class RemoteReadingRepository(private val api: LibrisApi, private val session: AuthSession) : ReadingRepository {
    override suspend fun logs(bookId: String): List<ReadingLog> = session.authorized { api.listLogs(it, bookId) }.map { it.toDomain() }

    override suspend fun add(bookId: String, date: LocalDate, page: Int): ReadingProgress =
        session.authorized { api.createLog(it, bookId, date.toString(), page) }.toDomain()

    override suspend fun update(bookId: String, logId: String, date: LocalDate, page: Int): ReadingProgress =
        session.authorized { api.updateLog(it, bookId, logId, date.toString(), page) }.toDomain()

    override suspend fun delete(bookId: String, logId: String): ReadingProgress =
        session.authorized { api.deleteLog(it, bookId, logId) }.toDomain()

    private fun ReadingLogResultDto.toDomain() = ReadingProgress(book.toDomain(api::coverSrc), logs.map { it.toDomain() })
}

class RemoteGenreRepository(private val api: LibrisApi, private val session: AuthSession) : GenreRepository {
    override suspend fun list(): List<Genre> = session.authorized { api.listGenres(it) }.map { it.toDomain() }

    override suspend fun rename(id: String, name: String): Genre = session.authorized { api.renameGenre(it, id, name) }.toDomain()

    override suspend fun delete(id: String) = session.authorized { api.deleteGenre(it, id) }
}

class RemoteCoverRepository(private val api: LibrisApi, private val session: AuthSession) : CoverRepository {
    override suspend fun upload(bytes: ByteArray, mimeType: String): Cover =
        session.authorized { api.uploadCover(it, bytes, "capa.jpg", mimeType) }.toDomain(api::coverSrc)

    override suspend fun fromUrl(url: String): Cover = session.authorized { api.coverFromUrl(it, url) }.toDomain(api::coverSrc)
}

class RemoteStatsRepository(private val api: LibrisApi, private val session: AuthSession) : StatsRepository {
    override suspend fun year(year: Int): YearStats = session.authorized { api.yearStats(it, year) }.toDomain()

    override suspend fun month(year: Int, month: Int): MonthDetail = session.authorized { api.monthStats(it, year, month) }.toDomain()

    override suspend fun days(year: Int): List<DayStats> = session.authorized { api.statsDays(it, year) }.map { it.toDomain() }
}
