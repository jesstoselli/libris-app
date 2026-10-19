package app.billfolder.libris.domain.repository

import app.billfolder.libris.domain.model.AccountState
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
import app.billfolder.libris.domain.model.User
import app.billfolder.libris.domain.model.YearStats
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.LocalDate

// Failures surface as the exceptions in domain.error (NoConnection, SignedOut, SessionExpired, Api).

interface AuthRepository {
    val state: StateFlow<AccountState>
    suspend fun login(email: String, password: String): User
    suspend fun logout()
}

interface BookRepository {
    suspend fun list(filter: BookFilter): List<Book>
    suspend fun get(id: String): Book
    suspend fun create(draft: BookDraft): Book
    suspend fun update(id: String, draft: BookDraft): Book
    suspend fun delete(id: String)
    suspend fun changeStatus(id: String, status: BookStatus): Book
    /** "Comprei": a wishlist book becomes owned. */
    suspend fun acquire(id: String, totalPages: Int?): Book
    suspend fun lookUp(isbn: String): BookLookup
    /** Every author on the user's books, for the form's suggestions. */
    suspend fun authors(): List<String>
}

interface ReadingRepository {
    suspend fun logs(bookId: String): List<ReadingLog>
    suspend fun add(bookId: String, date: LocalDate, page: Int): ReadingProgress
    suspend fun update(bookId: String, logId: String, date: LocalDate, page: Int): ReadingProgress
    suspend fun delete(bookId: String, logId: String): ReadingProgress
}

interface GenreRepository {
    suspend fun list(): List<Genre>
    suspend fun rename(id: String, name: String): Genre
    suspend fun delete(id: String)
}

interface CoverRepository {
    suspend fun upload(bytes: ByteArray, mimeType: String): Cover
    /** The API downloads the image itself, so the cover keeps working if the external link dies. */
    suspend fun fromUrl(url: String): Cover
}

interface StatsRepository {
    suspend fun year(year: Int): YearStats
    suspend fun month(year: Int, month: Int): MonthDetail
}
