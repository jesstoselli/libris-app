package app.billfolder.libris.data.remote

import app.billfolder.libris.data.remote.dto.BookListQuery
import app.billfolder.libris.data.remote.dto.BookStatusDto
import app.billfolder.libris.domain.error.ApiException
import app.billfolder.libris.domain.error.NoConnectionException
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.TextContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LibrisApiTest {

    private fun HttpRequestData.text() = (body as? TextContent)?.text ?: (body as? OutgoingContent.ByteArrayContent)?.bytes()?.decodeToString()

    @Test
    fun listBooks_sendsOnlySetFiltersInSnakeCase() = runTest {
        val server = FakeServer { json("[]") }

        server.api.listBooks("t", BookListQuery(status = BookStatusDto.NotStarted, q = "  "))

        val url = server.requests.single().url
        assertEquals("not_started", url.parameters["status"])
        assertNull(url.parameters["q"])
        assertEquals("Bearer t", server.requests.single().headers["Authorization"])
    }

    @Test
    fun book_parsesEnumsAndDates() = runTest {
        val server = FakeServer {
            json("""{"id":"b1","title":"Rosa","authors":["Eco"],"ownership":"owned","status":"not_started","addedAt":"2026-10-16","genres":[],"currentPage":0,"percent":0,"extra":1}""")
        }

        val book = server.api.getBook("t", "b1")

        assertEquals(BookStatusDto.NotStarted, book.status)
        assertEquals("2026-10-16", book.addedAt)
    }

    @Test
    fun changeStatus_sendsSnakeCaseAndToday() = runTest {
        val server = FakeServer {
            json("""{"id":"b1","title":"Rosa","authors":["Eco"],"ownership":"owned","status":"read","addedAt":"2026-10-16"}""")
        }

        server.api.changeStatus("t", "b1", BookStatusDto.Read, "2026-10-16")

        assertEquals("""{"status":"read","today":"2026-10-16"}""", server.requests.single().text())
    }

    @Test
    fun changeStatus_withoutDate_saysSo() = runTest {
        val server = FakeServer {
            json("""{"id":"b1","title":"Rosa","authors":["Eco"],"ownership":"owned","status":"read","addedAt":"2026-10-16"}""")
        }

        server.api.changeStatus("t", "b1", BookStatusDto.Read, "2026-10-16", withoutDate = true)

        assertEquals("""{"status":"read","today":"2026-10-16","withoutDate":true}""", server.requests.single().text())
    }

    @Test
    fun changeStatus_onAKnownDate_sendsIt() = runTest {
        val server = FakeServer {
            json("""{"id":"b1","title":"Rosa","authors":["Eco"],"ownership":"owned","status":"read","addedAt":"2026-10-16"}""")
        }

        server.api.changeStatus("t", "b1", BookStatusDto.Read, "2026-10-16", finishedAt = "2019-07-02")

        assertEquals("""{"status":"read","today":"2026-10-16","finishedAt":"2019-07-02"}""", server.requests.single().text())
    }

    @Test
    fun yearStats_readWithoutDate_defaultsToZeroFromAnOlderApi() = runTest {
        val months = (1..12).joinToString(",") { """{"month":$it,"pagesRead":0,"booksFinished":0}""" }
        val server = FakeServer { req ->
            if (req.url.encodedPath.endsWith("2026")) json("""{"year":2026,"booksFinished":1,"pagesRead":10,"readingNow":0,"months":[$months],"readWithoutDate":37}""")
            else json("""{"year":2025,"booksFinished":1,"pagesRead":10,"readingNow":0,"months":[$months]}""")
        }

        assertEquals(37, server.api.yearStats("t", 2026).readWithoutDate)
        assertEquals(0, server.api.yearStats("t", 2025).readWithoutDate)
    }

    @Test
    fun publishers_summaryAndRename_goToTheirRoutes() = runTest {
        val server = FakeServer { req ->
            if (req.method.value == "PUT") json("""{"updated":3}""")
            else json("""[{"name":"Rocco","bookCount":3,"wishlistCount":1}]""")
        }

        val summary = server.api.publisherSummary("t")
        val result = server.api.renamePublisher("t", "Editora Rocco", "Rocco")

        assertEquals("/v1/publishers/summary", server.requests[0].url.encodedPath)
        assertEquals(3, summary.single().bookCount)
        assertEquals("/v1/publishers", server.requests[1].url.encodedPath)
        assertEquals("""{"from":"Editora Rocco","to":"Rocco"}""", server.requests[1].text())
        assertEquals(3, result.updated)
    }

    @Test
    fun apiError_carriesPortugueseMessage() = runTest {
        val server = FakeServer {
            json("""{"error":"page_out_of_range","message":"A página precisa estar entre 1 e 512."}""", HttpStatusCode.BadRequest)
        }

        val error = assertFailsWith<ApiException> { server.api.createLog("t", "b1", "2026-10-16", 600) }

        assertEquals("page_out_of_range", error.code)
        assertEquals("A página precisa estar entre 1 e 512.", error.apiMessage)
    }

    @Test
    fun networkFailure_isNoConnection() = runTest {
        val server = FakeServer { throw kotlinx.io.IOException("unreachable") }

        assertFailsWith<NoConnectionException> { server.api.listGenres("t") }
    }

    @Test
    fun aBrokenRequest_isNotReportedAsNoConnection() = runTest {
        val server = FakeServer { throw IllegalStateException("bug") }

        assertFailsWith<IllegalStateException> { server.api.listGenres("t") }
    }

    @Test
    fun coverSrc_prefixesRelativePaths() {
        val api = FakeServer { json("{}") }.api

        assertEquals("$BASE/v1/covers/c1", api.coverSrc("/v1/covers/c1"))
        assertEquals("https://covers.openlibrary.org/x.jpg", api.coverSrc("https://covers.openlibrary.org/x.jpg"))
        assertNull(api.coverSrc(null))
    }

    @Test
    fun uploadCover_sendsMultipartFile() = runTest {
        val server = FakeServer { json("""{"coverId":"c1","url":"/v1/covers/c1"}""") }

        val cover = server.api.uploadCover("t", byteArrayOf(1, 2, 3), "capa.jpg", "image/jpeg")

        assertEquals("c1", cover.coverId)
        assertTrue(server.requests.single().body.contentType.toString().startsWith("multipart/form-data"))
    }
}
