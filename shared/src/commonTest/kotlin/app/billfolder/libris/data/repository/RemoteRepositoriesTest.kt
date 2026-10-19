package app.billfolder.libris.data.repository

import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.data.auth.InMemoryTokenStore
import app.billfolder.libris.data.auth.Session
import app.billfolder.libris.data.remote.BASE
import app.billfolder.libris.data.remote.FakeServer
import app.billfolder.libris.data.remote.json
import app.billfolder.libris.data.remote.user
import app.billfolder.libris.domain.model.BookDraft
import app.billfolder.libris.domain.model.BookFilter
import app.billfolder.libris.domain.model.BookFormat
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.LookupSource
import io.ktor.http.HttpMethod
import io.ktor.http.content.TextContent
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class RemoteRepositoriesTest {
    private val book = """{"id":"b1","title":"Rebecca","authors":["Daphne du Maurier"],"ownership":"owned","status":"read","startedAt":"2026-10-01","finishedAt":"2026-10-16","addedAt":"2026-09-30","coverUrl":"/v1/covers/c1","genres":[{"id":"g1","name":"Gótico"}],"currentPage":0,"percent":100}"""

    private fun books(server: FakeServer, today: LocalDate = LocalDate(2026, 10, 16)) =
        RemoteBookRepository(server.api, AuthSession(server.api, InMemoryTokenStore(Session("t", "r", user()))), { today })

    @Test
    fun book_becomesDomain_withDatesAndAnAbsoluteCover() = runTest {
        val server = FakeServer { json(book) }

        val b = books(server).get("b1")

        assertEquals(LocalDate(2026, 10, 16), b.finishedAt)
        assertEquals(LocalDate(2026, 9, 30), b.addedAt)
        assertEquals(BookStatus.Read, b.status)
        assertEquals("$BASE/v1/covers/c1", b.coverUrl)
        assertEquals("Gótico", b.genres.single().name)
    }

    @Test
    fun list_sendsTheFilterOnTheWire() = runTest {
        val server = FakeServer { json("[$book]") }

        books(server).list(BookFilter(BookOwnership.Wishlist, BookStatus.NotStarted, "g1", "  rosa "))

        val params = server.requests.single().url.parameters
        assertEquals("wishlist", params["ownership"])
        assertEquals("not_started", params["status"])
        assertEquals("rosa", params["q"])
    }

    @Test
    fun create_sendsTheDeviceToday() = runTest {
        val server = FakeServer { json(book) }

        books(server, today = LocalDate(2026, 10, 17)).create(
            BookDraft("Rebecca", listOf("Daphne du Maurier"), status = BookStatus.Read, startedAt = LocalDate(2026, 10, 1)),
        )

        val body = (server.requests.single().body as TextContent).text
        assertEquals(true, body.contains(""""today":"2026-10-17""""), body)
        assertEquals(true, body.contains(""""startedAt":"2026-10-01""""), body)
        assertEquals(true, body.contains(""""status":"read""""), body)
    }

    @Test
    fun lookup_namesItsSources() = runTest {
        val server = FakeServer {
            json("""{"isbn":"9786556928784","title":"T","authors":["A"],"suggestedGenres":["Ficção"],"source":"google_books+open_library"}""")
        }

        val found = books(server).lookUp("9786556928784")

        assertEquals(listOf(LookupSource.GoogleBooks, LookupSource.OpenLibrary), found.sources)
        assertEquals(listOf("Ficção"), found.genres)
    }

    @Test
    fun formats_defaultToPhysical_whenTheApiDoesNotSendThem() = runTest {
        val server = FakeServer { json(book) }

        assertEquals(listOf(BookFormat.Physical), books(server).get("b1").formats)
    }

    @Test
    fun formats_travelBothWays() = runTest {
        val digital = book.replace("\"percent\":100", "\"percent\":100,\"formats\":[\"digital\"]")
        val server = FakeServer { req -> if (req.method == HttpMethod.Get && req.url.encodedPath == "/v1/books") json("[$digital]") else json(digital) }
        val repo = books(server)

        assertEquals(listOf(BookFormat.Digital), repo.get("b1").formats)
        repo.list(BookFilter(format = BookFormat.Digital))
        assertEquals("digital", server.requests.last().url.parameters["format"])
        repo.create(BookDraft("T", listOf("A"), formats = setOf(BookFormat.Digital, BookFormat.Physical)))
        val body = (server.requests.last().body as TextContent).text
        assertEquals(true, body.contains(""""formats":["physical","digital"]"""), body)
    }

    @Test
    fun language_defaultsToPortuguese_andTravelsBothWays() = runTest {
        val english = book.replace("\"percent\":100", "\"percent\":100,\"language\":\"en\"")
        val server = FakeServer { req -> if (req.method == HttpMethod.Get && req.url.encodedPath == "/v1/books") json("[$english]") else json(english) }
        val repo = books(server)

        assertEquals("pt", books(FakeServer { json(book) }).get("b1").language)
        assertEquals("en", repo.get("b1").language)
        repo.list(BookFilter(language = "en"))
        assertEquals("en", server.requests.last().url.parameters["language"])
        repo.create(BookDraft("T", listOf("A"), language = "it"))
        assertEquals(true, (server.requests.last().body as TextContent).text.contains(""""language":"it""""))
    }

    @Test
    fun authorFilter_travelsAsItIsWritten() = runTest {
        val server = FakeServer { json("[$book]") }

        books(server).list(BookFilter(author = "H. P. Lovecraft"))

        assertEquals("H. P. Lovecraft", server.requests.last().url.parameters["author"])
    }

    @Test
    fun authors_comeFromTheirOwnRoute() = runTest {
        val server = FakeServer { json("""["Brian Christian","Umberto Eco"]""") }

        val authors = books(server).authors()

        assertEquals(listOf("Brian Christian", "Umberto Eco"), authors)
        assertEquals("/v1/authors", server.requests.last().url.encodedPath)
    }
}
