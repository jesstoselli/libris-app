package app.billfolder.libris.ui

import app.billfolder.libris.data.remote.dto.BookStatus
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.feature.acervo.AcervoViewModel
import io.ktor.http.HttpStatusCode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class AcervoViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val books = "[${bookJson("b1", status = "reading")},${bookJson("b2", "Drácula", status = "read")},${bookJson("b3", "Frankenstein", status = "reading")}]"

    private fun server() = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(books) }

    @Test
    fun loadsOwnedBooksAndCounts() = runTest(dispatcher) {
        val fake = server()
        val vm = AcervoViewModel(fake.session, fake.api)
        advanceUntilIdle()

        assertEquals(3, (vm.state.value.shown as Load.Loaded).value.size)
        assertEquals("3 livros no acervo · 2 em leitura", vm.state.value.subtitle)
        assertEquals("owned", fake.requests.first { it.url.encodedPath == "/v1/books" }.url.parameters["ownership"])
    }

    @Test
    fun statusFilter_requestsSnakeCaseStatus() = runTest(dispatcher) {
        val fake = server()
        val vm = AcervoViewModel(fake.session, fake.api)
        advanceUntilIdle()

        vm.setStatus(BookStatus.NotStarted)
        advanceUntilIdle()

        assertEquals("not_started", fake.requests.last().url.parameters["status"])
    }

    @Test
    fun search_isDebounced() = runTest(dispatcher) {
        val fake = server()
        val vm = AcervoViewModel(fake.session, fake.api)
        advanceUntilIdle()
        val before = fake.requests.size

        vm.setQuery("dra")
        advanceTimeBy(100)
        vm.setQuery("drac")
        advanceUntilIdle()

        val searches = fake.requests.drop(before)
        assertEquals(1, searches.size)
        assertEquals("drac", searches.single().url.parameters["q"])
    }

    @Test
    fun offline_showsNoConnection() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { throw IllegalStateException("offline") }
        val vm = AcervoViewModel(fake.session, fake.api)
        advanceUntilIdle()

        assertEquals(Load.Failed("Sem conexão com o servidor."), vm.state.value.shown)
    }

    @Test
    fun clearingFilters_showsAllAgainWithoutRequest() = runTest(dispatcher) {
        val fake = server()
        val vm = AcervoViewModel(fake.session, fake.api)
        advanceUntilIdle()
        vm.setStatus(BookStatus.Read)
        advanceUntilIdle()
        val before = fake.requests.size

        vm.setStatus(null)
        advanceUntilIdle()

        assertEquals(before, fake.requests.size)
        assertIs<Load.Loaded<*>>(vm.state.value.shown)
    }
}
