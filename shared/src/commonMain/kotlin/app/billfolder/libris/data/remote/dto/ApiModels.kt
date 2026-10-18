package app.billfolder.libris.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Mirrors the libris-api DTOs (Libris.Application/Dtos). Enums travel as snake_case, dates as "YYYY-MM-DD" strings.

val ApiJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
enum class BookOwnership {
    @SerialName("owned") Owned,
    @SerialName("wishlist") Wishlist,
}

@Serializable
enum class BookStatus(val wire: String) {
    @SerialName("not_started") NotStarted("not_started"),
    @SerialName("reading") Reading("reading"),
    @SerialName("read") Read("read"),
    @SerialName("abandoned") Abandoned("abandoned"),
}

@Serializable data class GenreRef(val id: String, val name: String)

@Serializable data class Genre(val id: String, val name: String, val bookCount: Int)

@Serializable
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
    val status: BookStatus? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val addedAt: String,
    val currentPage: Int = 0,
    val percent: Int = 0,
)

@Serializable
data class CreateBookRequest(
    val title: String,
    val authors: List<String>,
    val isbn: String?,
    val totalPages: Int?,
    val publisher: String?,
    val publishedYear: Int?,
    val synopsis: String?,
    val coverId: String?,
    val genres: List<String>,
    val ownership: BookOwnership,
    val status: BookStatus?,
    val startedAt: String?,
    val finishedAt: String?,
    val today: String,
)

@Serializable
data class UpdateBookRequest(
    val title: String,
    val authors: List<String>,
    val isbn: String?,
    val totalPages: Int?,
    val publisher: String?,
    val publishedYear: Int?,
    val synopsis: String?,
    val coverId: String?,
    val genres: List<String>,
    val startedAt: String?,
    val finishedAt: String?,
)

data class BookListQuery(
    val ownership: BookOwnership? = null,
    val status: BookStatus? = null,
    val genreId: String? = null,
    val q: String? = null,
)

@Serializable data class ReadingLog(val id: String, val date: String, val page: Int, val fromPage: Int, val pagesRead: Int)

@Serializable data class ReadingLogResult(val book: Book, val logs: List<ReadingLog>)

@Serializable data class Cover(val coverId: String, val url: String)

@Serializable
data class IsbnLookup(
    val isbn: String,
    val title: String,
    val authors: List<String>,
    val totalPages: Int? = null,
    val publisher: String? = null,
    val publishedYear: Int? = null,
    val synopsis: String? = null,
    val coverUrl: String? = null,
    val suggestedGenres: List<String> = emptyList(),
    val source: String,
)

@Serializable data class MonthStats(val month: Int, val pagesRead: Int, val booksFinished: Int)

@Serializable
data class YearStats(val year: Int, val booksFinished: Int, val pagesRead: Int, val readingNow: Int, val months: List<MonthStats>)

@Serializable data class FinishedBook(val bookId: String, val title: String, val authors: List<String>, val finishedAt: String)

@Serializable data class ProgressedBook(val bookId: String, val title: String, val pagesRead: Int)

@Serializable
data class MonthDetail(val year: Int, val month: Int, val pagesRead: Int, val finished: List<FinishedBook>, val progressed: List<ProgressedBook>)

@Serializable data class User(val id: String, val email: String, val displayName: String)

@Serializable internal data class AuthBody(val accessToken: String, val refreshToken: String, val user: User)

@Serializable internal data class LoginBody(val email: String, val password: String)

@Serializable internal data class RefreshBody(val refreshToken: String)

@Serializable internal data class ErrorBody(val error: String? = null, val message: String? = null)

@Serializable internal data class StatusBody(val status: BookStatus, val today: String)

@Serializable internal data class AcquireBody(val today: String, val totalPages: Int?)

@Serializable internal data class LogBody(val date: String, val page: Int)

@Serializable internal data class NameBody(val name: String)

@Serializable internal data class UrlBody(val url: String)
