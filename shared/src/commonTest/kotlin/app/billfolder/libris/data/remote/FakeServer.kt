package app.billfolder.libris.data.remote

import app.billfolder.libris.data.remote.dto.User
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf

const val BASE = "http://api.test"

fun user() = User("u1", "jess@example.com", "Jess")

fun authJson(suffix: String) =
    """{"accessToken":"access-$suffix","refreshToken":"refresh-$suffix","accessTokenExpiresAt":"x","refreshTokenExpiresAt":"x","user":{"id":"u1","email":"jess@example.com","displayName":"Jess"}}"""

fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK): HttpResponseData =
    respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

/** A scripted libris-api: each test supplies the handler and inspects what was asked. */
class FakeServer(handler: suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
    val requests = mutableListOf<HttpRequestData>()
    val engine = MockEngine { request ->
        requests += request
        handler(request)
    }
    val api = LibrisApi(librisHttpClient(engine), BASE)

    fun count(path: String) = requests.count { it.url.encodedPath == path }
}
