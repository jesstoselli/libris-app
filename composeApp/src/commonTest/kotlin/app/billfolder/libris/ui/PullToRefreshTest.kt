package app.billfolder.libris.ui

import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.feature.library.LibraryViewModel
import app.billfolder.libris.ui.feature.bookdetail.BookDetailViewModel
import app.billfolder.libris.ui.feature.genres.GenresViewModel
import app.billfolder.libris.ui.feature.stats.StatsViewModel
import app.billfolder.libris.ui.feature.wishlist.WishlistViewModel
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpStatusCode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

/** Every screen recovers from a server error when the list is pulled down. */
class PullToRefreshTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val year = """{"year":2026,"booksFinished":1,"pagesRead":10,"readingNow":0,"months":[${(1..12).joinToString(",") { """{"month":$it,"pagesRead":0,"booksFinished":0}""" }}]}"""
    private val month = """{"year":2026,"month":10,"pagesRead":0,"finished":[],"progressed":[]}"""

    /** Answers 500 until [up] is set, then [ok]. */
    private class FlakyServer(dispatcher: TestDispatcher, ok: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData) {
        var up = false
        val fake = FakeApi(dispatcher) { req -> if (up) ok(req) else json("""{"error":"boom","message":"Erro no servidor."}""", HttpStatusCode.InternalServerError) }
    }

    private fun TestScope.pullAndCheck(server: FlakyServer, pull: () -> Unit, refreshing: () -> Boolean) {
        advanceUntilIdle()
        server.up = true
        pull()
        assertTrue(refreshing(), "shows the indicator while reloading")
        advanceUntilIdle()
        assertFalse(refreshing(), "hides the indicator when done")
    }

    @Test
    fun library() = runTest(dispatcher) {
        val server = FlakyServer(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val vm = LibraryViewModel(server.fake.books, server.fake.genres)
        pullAndCheck(server, vm::pullToRefresh) { vm.state.value.refreshing }
        assertIs<Load.Loaded<*>>(vm.state.value.shown)
    }

    @Test
    fun wishlist() = runTest(dispatcher) {
        val server = FlakyServer(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[]") }
        val vm = WishlistViewModel(server.fake.books, server.fake.genres)
        pullAndCheck(server, vm::pullToRefresh) { vm.state.value.refreshing }
        assertIs<Load.Loaded<*>>(vm.state.value.books)
    }

    @Test
    fun bookDetail() = runTest(dispatcher) {
        val server = FlakyServer(dispatcher) { req -> if (req.url.encodedPath.endsWith("/logs")) json("[]") else json(bookJson(status = "reading")) }
        val vm = BookDetailViewModel("b1", server.fake.books, server.fake.reading)
        pullAndCheck(server, vm::pullToRefresh) { vm.state.value.refreshing }
        assertIs<Load.Loaded<*>>(vm.state.value.book)
    }

    @Test
    fun stats() = runTest(dispatcher) {
        val server = FlakyServer(dispatcher) { req -> if (req.url.encodedPath.count { it == '/' } == 4) json(month) else json(year) }
        val vm = StatsViewModel(server.fake.stats) { 2026 to 10 }
        pullAndCheck(server, vm::pullToRefresh) { vm.state.value.refreshing }
        assertIs<Load.Loaded<*>>(vm.state.value.stats)
        assertIs<Load.Loaded<*>>(vm.state.value.detail)
    }

    @Test
    fun genres() = runTest(dispatcher) {
        val server = FlakyServer(dispatcher) { json("""[{"id":"g1","name":"Terror","bookCount":1}]""") }
        val vm = GenresViewModel(server.fake.genres)
        pullAndCheck(server, vm::pullToRefresh) { vm.state.value.refreshing }
        assertIs<Load.Loaded<*>>(vm.state.value.genres)
    }
}
