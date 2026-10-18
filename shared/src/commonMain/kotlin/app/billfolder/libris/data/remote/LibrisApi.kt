package app.billfolder.libris.data.remote

import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.data.remote.dto.AcquireBody
import app.billfolder.libris.data.remote.dto.ApiJson
import app.billfolder.libris.data.remote.dto.AuthBody
import app.billfolder.libris.data.remote.dto.BookDto
import app.billfolder.libris.data.remote.dto.BookListQuery
import app.billfolder.libris.data.remote.dto.BookOwnershipDto
import app.billfolder.libris.data.remote.dto.BookStatusDto
import app.billfolder.libris.data.remote.dto.CoverDto
import app.billfolder.libris.data.remote.dto.CreateBookRequest
import app.billfolder.libris.data.remote.dto.ErrorBody
import app.billfolder.libris.data.remote.dto.GenreDto
import app.billfolder.libris.data.remote.dto.IsbnLookupDto
import app.billfolder.libris.data.remote.dto.LogBody
import app.billfolder.libris.data.remote.dto.LoginBody
import app.billfolder.libris.data.remote.dto.MonthDetailDto
import app.billfolder.libris.data.remote.dto.NameBody
import app.billfolder.libris.data.remote.dto.ReadingLogDto
import app.billfolder.libris.data.remote.dto.ReadingLogResultDto
import app.billfolder.libris.data.remote.dto.RefreshBody
import app.billfolder.libris.data.remote.dto.StatusBody
import app.billfolder.libris.data.remote.dto.UpdateBookRequest
import app.billfolder.libris.data.remote.dto.UrlBody
import app.billfolder.libris.data.remote.dto.YearStatsDto
import app.billfolder.libris.domain.error.ApiException
import app.billfolder.libris.domain.error.NoConnectionException
import app.billfolder.libris.domain.error.UnauthorizedException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.encodeURLPathPart
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CancellationException

fun librisHttpClient(engine: HttpClientEngine): HttpClient = HttpClient(engine) {
    install(ContentNegotiation) { json(ApiJson) }
    expectSuccess = false
}

/** Thin typed client of libris-api. Every call that needs a user takes the access token explicitly (see [AuthSession]). */
class LibrisApi(private val http: HttpClient, val baseUrl: String) {

    internal suspend fun login(email: String, password: String): AuthBody =
        send { http.post("$baseUrl/v1/auth/login") { json(LoginBody(email, password)) } }.body()

    internal suspend fun refresh(refreshToken: String): AuthBody =
        send { http.post("$baseUrl/v1/auth/refresh") { json(RefreshBody(refreshToken)) } }.body()

    internal suspend fun logout(refreshToken: String) {
        send { http.post("$baseUrl/v1/auth/logout") { json(RefreshBody(refreshToken)) } }
    }

    suspend fun listBooks(token: String, query: BookListQuery = BookListQuery()): List<BookDto> =
        send {
            http.get("$baseUrl/v1/books") {
                bearerAuth(token)
                query.ownership?.let { parameter("ownership", if (it == BookOwnershipDto.Owned) "owned" else "wishlist") }
                query.status?.let { parameter("status", it.wire) }
                query.genreId?.takeIf { it.isNotBlank() }?.let { parameter("genreId", it) }
                query.q?.takeIf { it.isNotBlank() }?.let { parameter("q", it) }
            }
        }.body()

    suspend fun getBook(token: String, id: String): BookDto = send { http.get("$baseUrl/v1/books/$id") { bearerAuth(token) } }.body()

    suspend fun createBook(token: String, request: CreateBookRequest): BookDto =
        send { http.post("$baseUrl/v1/books") { bearerAuth(token); json(request) } }.body()

    suspend fun updateBook(token: String, id: String, request: UpdateBookRequest): BookDto =
        send { http.put("$baseUrl/v1/books/$id") { bearerAuth(token); json(request) } }.body()

    suspend fun deleteBook(token: String, id: String) {
        send { http.delete("$baseUrl/v1/books/$id") { bearerAuth(token) } }
    }

    suspend fun changeStatus(token: String, id: String, status: BookStatusDto, today: String): BookDto =
        send { http.patch("$baseUrl/v1/books/$id/status") { bearerAuth(token); json(StatusBody(status, today)) } }.body()

    suspend fun acquire(token: String, id: String, today: String, totalPages: Int?): BookDto =
        send { http.post("$baseUrl/v1/books/$id/acquire") { bearerAuth(token); json(AcquireBody(today, totalPages)) } }.body()

    suspend fun listLogs(token: String, bookId: String): List<ReadingLogDto> =
        send { http.get("$baseUrl/v1/books/$bookId/logs") { bearerAuth(token) } }.body()

    suspend fun createLog(token: String, bookId: String, date: String, page: Int): ReadingLogResultDto =
        send { http.post("$baseUrl/v1/books/$bookId/logs") { bearerAuth(token); json(LogBody(date, page)) } }.body()

    suspend fun updateLog(token: String, bookId: String, logId: String, date: String, page: Int): ReadingLogResultDto =
        send { http.put("$baseUrl/v1/books/$bookId/logs/$logId") { bearerAuth(token); json(LogBody(date, page)) } }.body()

    suspend fun deleteLog(token: String, bookId: String, logId: String): ReadingLogResultDto =
        send { http.delete("$baseUrl/v1/books/$bookId/logs/$logId") { bearerAuth(token) } }.body()

    suspend fun listGenres(token: String): List<GenreDto> = send { http.get("$baseUrl/v1/genres") { bearerAuth(token) } }.body()

    suspend fun renameGenre(token: String, id: String, name: String): GenreDto =
        send { http.put("$baseUrl/v1/genres/$id") { bearerAuth(token); json(NameBody(name)) } }.body()

    suspend fun deleteGenre(token: String, id: String) {
        send { http.delete("$baseUrl/v1/genres/$id") { bearerAuth(token) } }
    }

    suspend fun lookupIsbn(token: String, isbn: String): IsbnLookupDto =
        send { http.get("$baseUrl/v1/isbn/${isbn.trim().encodeURLPathPart()}") { bearerAuth(token) } }.body()

    suspend fun uploadCover(token: String, bytes: ByteArray, fileName: String, mimeType: String): CoverDto =
        send {
            http.submitFormWithBinaryData(
                url = "$baseUrl/v1/covers",
                formData = formData {
                    append("file", bytes, Headers.build {
                        append(HttpHeaders.ContentType, mimeType)
                        append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                    })
                },
            ) { bearerAuth(token) }
        }.body()

    /** The API downloads the image itself, so the cover keeps working if the external link dies. */
    suspend fun coverFromUrl(token: String, url: String): CoverDto =
        send { http.post("$baseUrl/v1/covers/from-url") { bearerAuth(token); json(UrlBody(url)) } }.body()

    suspend fun yearStats(token: String, year: Int): YearStatsDto = send { http.get("$baseUrl/v1/stats/$year") { bearerAuth(token) } }.body()

    suspend fun monthStats(token: String, year: Int, month: Int): MonthDetailDto =
        send { http.get("$baseUrl/v1/stats/$year/$month") { bearerAuth(token) } }.body()

    /** The API returns covers as relative paths ("/v1/covers/{id}"). */
    fun coverSrc(coverUrl: String?): String? = when {
        coverUrl == null -> null
        coverUrl.startsWith("http://") || coverUrl.startsWith("https://") -> coverUrl
        else -> baseUrl.trimEnd('/') + coverUrl
    }

    private inline fun <reified T> HttpRequestBuilder.json(body: T) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private suspend fun send(call: suspend () -> HttpResponse): HttpResponse {
        val response = try {
            call()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // The user only sees "Sem conexão"; the cause is what tells a dead network from a broken request.
            println("Libris: request failed: ${e::class.simpleName}: ${e.message} (cause: ${e.cause})")
            throw NoConnectionException(e)
        }
        if (response.status.isSuccess()) return response
        val error = try {
            response.body<ErrorBody>()
        } catch (_: Exception) {
            null
        }
        if (response.status == HttpStatusCode.Unauthorized) throw UnauthorizedException(error?.error, error?.message)
        throw ApiException(response.status.value, error?.error, error?.message)
    }

    companion object {
        const val PRODUCTION_URL = "https://libris-api.billfolder.app"
    }
}
