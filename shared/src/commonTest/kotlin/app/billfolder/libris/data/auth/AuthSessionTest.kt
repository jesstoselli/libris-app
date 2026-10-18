package app.billfolder.libris.data.auth

import app.billfolder.libris.domain.model.AccountState

import app.billfolder.libris.data.remote.FakeServer
import app.billfolder.libris.data.remote.authJson
import app.billfolder.libris.data.remote.json
import app.billfolder.libris.data.remote.user
import app.billfolder.libris.domain.error.NoConnectionException
import app.billfolder.libris.domain.error.SessionExpiredException
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest

class AuthSessionTest {

    private fun signedIn(suffix: String = "1") = InMemoryTokenStore(Session("access-$suffix", "refresh-$suffix", user()))

    @Test
    fun login_storesSessionAndSignsIn() = runTest {
        val server = FakeServer { json(authJson("1")) }
        val tokens = InMemoryTokenStore()
        val session = AuthSession(server.api, tokens)

        session.login(" jess@example.com ", "strong-pass-123")

        assertEquals("access-1", tokens.load()?.accessToken)
        assertIs<AccountState.SignedIn>(session.state.value)
    }

    @Test
    fun concurrent401s_refreshOnce() = runTest {
        val server = FakeServer { request ->
            when (request.url.encodedPath) {
                "/v1/auth/refresh" -> json(authJson("2"))
                else -> if (request.headers["Authorization"] == "Bearer access-2") json("[]") else json("{}", HttpStatusCode.Unauthorized)
            }
        }
        val session = AuthSession(server.api, signedIn())

        val results = List(3) { async { session.authorized { server.api.listGenres(it) } } }.awaitAll()

        assertEquals(3, results.size)
        assertEquals(1, server.count("/v1/auth/refresh"))
    }

    @Test
    fun late401ForOldToken_retriesWithoutRefreshing() = runTest {
        val tokens = signedIn()
        val server = FakeServer { request ->
            if (request.headers["Authorization"] == "Bearer access-2") json("[]") else json("{}", HttpStatusCode.Unauthorized)
        }
        val session = AuthSession(server.api, tokens)

        // Another caller already rotated the tokens while this request (sent with access-1) was in flight.
        val result = session.authorized { token ->
            if (token == "access-1") tokens.save(Session("access-2", "refresh-2", user()))
            server.api.listGenres(token)
        }

        assertEquals(emptyList(), result)
        assertEquals(0, server.count("/v1/auth/refresh"))
    }

    @Test
    fun refreshNetworkFailure_keepsSession() = runTest {
        val tokens = signedIn()
        val server = FakeServer { request ->
            if (request.url.encodedPath == "/v1/auth/refresh") throw IllegalStateException("offline")
            json("{}", HttpStatusCode.Unauthorized)
        }
        val session = AuthSession(server.api, tokens)

        assertFailsWith<NoConnectionException> { session.authorized { server.api.listGenres(it) } }

        assertNotNull(tokens.load())
        assertIs<AccountState.SignedIn>(session.state.value)
    }

    @Test
    fun refreshRejected_signsOut() = runTest {
        val tokens = signedIn()
        val server = FakeServer { json("""{"error":"invalid_refresh_token","message":"x"}""", HttpStatusCode.Unauthorized) }
        val session = AuthSession(server.api, tokens)

        assertFailsWith<SessionExpiredException> { session.authorized { server.api.listGenres(it) } }

        assertNull(tokens.load())
        assertEquals(AccountState.SignedOut, session.state.value)
    }

    @Test
    fun logout_clearsEvenWhenServerFails() = runTest {
        val tokens = signedIn()
        val server = FakeServer { throw IllegalStateException("offline") }
        val session = AuthSession(server.api, tokens)

        session.logout()

        assertNull(tokens.load())
        assertEquals(AccountState.SignedOut, session.state.value)
    }
}
