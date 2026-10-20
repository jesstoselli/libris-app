package app.billfolder.libris.ui

import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.data.auth.InMemoryTokenStore
import app.billfolder.libris.data.auth.Session
import app.billfolder.libris.data.remote.LibrisApi
import app.billfolder.libris.data.remote.librisHttpClient
import app.billfolder.libris.data.remote.dto.UserDto
import app.billfolder.libris.data.repository.RemoteBookRepository
import app.billfolder.libris.data.repository.RemoteCoverRepository
import app.billfolder.libris.data.repository.RemoteGenreRepository
import app.billfolder.libris.data.repository.RemoteReadingRepository
import app.billfolder.libris.data.repository.RemoteStatsRepository
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf
import kotlinx.coroutines.CoroutineDispatcher

const val BASE = "http://api.test"

fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

fun bookJson(
    id: String = "b1",
    title: String = "O Nome da Rosa",
    status: String? = "not_started",
    ownership: String = "owned",
    totalPages: Int? = 512,
    currentPage: Int = 0,
    percent: Int = 0,
    startedAt: String? = null,
    finishedAt: String? = null,
    formats: List<String>? = null,
    language: String? = null,
    authors: List<String> = listOf("Umberto Eco"),
    genres: List<Pair<String, String>> = emptyList(),
): String {
    fun q(v: String?) = v?.let { "\"$it\"" } ?: "null"
    val formatsField = formats?.let { f -> ""","formats":[${f.joinToString(",") { "\"$it\"" }}]""" }.orEmpty()
    val languageField = language?.let { ""","language":"$it"""" }.orEmpty()
    return """{"id":"$id","title":"$title","authors":[${authors.joinToString(",") { "\"$it\"" }}],"ownership":"$ownership","status":${q(status)},"totalPages":${totalPages ?: "null"},"addedAt":"2026-10-01","genres":[${genres.joinToString(",") { (gid, name) -> """{"id":"$gid","name":"$name"}""" }}],"currentPage":$currentPage,"percent":$percent,"startedAt":${q(startedAt)},"finishedAt":${q(finishedAt)}$formatsField$languageField}"""
}

/** [dispatcher] must be the test's, so advanceUntilIdle() also waits for the fake HTTP calls. */
class FakeApi(dispatcher: CoroutineDispatcher, handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
    val requests = mutableListOf<HttpRequestData>()
    private val engine = MockEngine(
        MockEngineConfig().apply {
            this.dispatcher = dispatcher
            addHandler { request ->
                requests += request
                handler(request)
            }
        },
    )
    val api = LibrisApi(librisHttpClient(engine), BASE)
    val session = AuthSession(api, InMemoryTokenStore(Session("t", "r", UserDto("u", "e@x.com", "Jess"))))
    val books = RemoteBookRepository(api, session)
    val reading = RemoteReadingRepository(api, session)
    val genres = RemoteGenreRepository(api, session)
    val covers = RemoteCoverRepository(api, session)
    val stats = RemoteStatsRepository(api, session)

    fun bodyOf(request: HttpRequestData) = (request.body as? TextContent)?.text
}
