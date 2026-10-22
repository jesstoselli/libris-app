package app.billfolder.libris.data.remote.dto

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// Mirrors the libris-api DTOs (Libris.Application/Dtos). Enums travel as snake_case, dates as "YYYY-MM-DD" strings.

val ApiJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

@Serializable
enum class BookOwnershipDto {
    @SerialName("owned") Owned,
    @SerialName("wishlist") Wishlist,
}

@Serializable
enum class BookStatusDto(val wire: String) {
    @SerialName("not_started") NotStarted("not_started"),
    @SerialName("reading") Reading("reading"),
    @SerialName("read") Read("read"),
    @SerialName("abandoned") Abandoned("abandoned"),
}

@Serializable
enum class BookFormatDto {
    @SerialName("physical") Physical,
    @SerialName("digital") Digital,
}

@Serializable data class GenreRefDto(val id: String, val name: String)

@Serializable data class GenreDto(val id: String, val name: String, val bookCount: Int)

@Serializable
data class BookDto(
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
    val genres: List<GenreRefDto> = emptyList(),
    val ownership: BookOwnershipDto,
    val status: BookStatusDto? = null,
    val startedAt: String? = null,
    val finishedAt: String? = null,
    val addedAt: String,
    val currentPage: Int = 0,
    val percent: Int = 0,
    /** Absent from APIs older than formats; every book was physical then. */
    val formats: List<BookFormatDto> = listOf(BookFormatDto.Physical),
    /** ISO 639-1; absent from APIs older than languages, when every book was Portuguese. */
    val language: String = "pt",
    /** Absent from APIs older than series and ratings. */
    val series: String? = null,
    val seriesNumber: Double? = null,
    val rating: Double? = null,
)

/** Always sent by this app; a blank name takes the book out of its series. */
@Serializable data class BookSeriesInputDto(val name: String?, val number: Double?)

@Serializable data class SeriesDto(val name: String, val bookCount: Int, val wishlistCount: Int)

@Serializable internal data class RenameSeriesBody(val from: String, val to: String)

@Serializable internal data class RatingBody(val rating: Double?)

@Serializable data class DayStatsDto(val date: String, val pagesRead: Int, val books: List<ProgressedBookDto>)

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
    val ownership: BookOwnershipDto,
    val status: BookStatusDto?,
    val startedAt: String?,
    val finishedAt: String?,
    val today: String,
    val formats: List<BookFormatDto>,
    /** A book registered mid-read: the API turns it into the first reading log. */
    val currentPage: Int? = null,
    val language: String = "pt",
    val series: BookSeriesInputDto = BookSeriesInputDto(null, null),
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
    val formats: List<BookFormatDto>,
    val language: String = "pt",
    val series: BookSeriesInputDto = BookSeriesInputDto(null, null),
)

data class BookListQuery(
    val ownership: BookOwnershipDto? = null,
    val status: BookStatusDto? = null,
    val genreId: String? = null,
    val q: String? = null,
    val format: BookFormatDto? = null,
    val language: String? = null,
    val author: String? = null,
    val publisher: String? = null,
    val series: String? = null,
    val minRating: Double? = null,
    /** "rating" for the best rated first; null keeps the shelf order. */
    val sort: String? = null,
)

@Serializable data class PublisherDto(val name: String, val bookCount: Int, val wishlistCount: Int)

@Serializable internal data class RenamePublisherBody(val from: String, val to: String)

@Serializable data class RenamePublisherResultDto(val updated: Int)

@Serializable data class ReadingLogDto(val id: String, val date: String, val page: Int, val fromPage: Int, val pagesRead: Int)

@Serializable data class ReadingLogResultDto(val book: BookDto, val logs: List<ReadingLogDto>)

@Serializable data class CoverDto(val coverId: String, val url: String)

@Serializable
data class IsbnLookupDto(
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
    val language: String? = null,
)

@Serializable data class MonthStatsDto(val month: Int, val pagesRead: Int, val booksFinished: Int)

@Serializable
data class YearStatsDto(
    val year: Int,
    val booksFinished: Int,
    val pagesRead: Int,
    val readingNow: Int,
    val months: List<MonthStatsDto>,
    // Absent from an older API.
    val readWithoutDate: Int = 0,
)

@Serializable data class FinishedBookDto(val bookId: String, val title: String, val authors: List<String>, val finishedAt: String)

@Serializable data class ProgressedBookDto(val bookId: String, val title: String, val pagesRead: Int)

@Serializable
data class MonthDetailDto(val year: Int, val month: Int, val pagesRead: Int, val finished: List<FinishedBookDto>, val progressed: List<ProgressedBookDto>)

@Serializable data class UserDto(val id: String, val email: String, val displayName: String)

@Serializable internal data class AuthBody(val accessToken: String, val refreshToken: String, val user: UserDto)

@Serializable internal data class LoginBody(val email: String, val password: String)

@Serializable internal data class RefreshBody(val refreshToken: String)

@Serializable internal data class ErrorBody(val error: String? = null, val message: String? = null)

// withoutDate only travels when set: absent, the API finishes the book today.
@OptIn(ExperimentalSerializationApi::class)
@Serializable internal data class StatusBody(
    val status: BookStatusDto,
    val today: String,
    @EncodeDefault(EncodeDefault.Mode.NEVER) val withoutDate: Boolean? = null,
    // "Lido" on a remembered day.
    @EncodeDefault(EncodeDefault.Mode.NEVER) val finishedAt: String? = null,
)

@Serializable internal data class AcquireBody(val today: String, val totalPages: Int?)

@Serializable internal data class LogBody(val date: String, val page: Int)

@Serializable internal data class NameBody(val name: String)

@Serializable internal data class UrlBody(val url: String)
