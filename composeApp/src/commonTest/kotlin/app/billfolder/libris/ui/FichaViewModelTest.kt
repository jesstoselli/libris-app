package app.billfolder.libris.ui

import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.time.today
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.feature.ficha.FichaViewModel
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class FichaViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val logs = """[{"id":"l1","date":"2026-10-06","page":51,"fromPage":20,"pagesRead":31}]"""

    @Test
    fun loadsBookAndLogs() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath.endsWith("/logs")) json(logs) else json(bookJson(status = "reading")) }
        val vm = FichaViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        assertEquals("O Nome da Rosa", (vm.state.value.book as Load.Loaded).value.title)
        assertEquals(51, vm.state.value.logs.single().page)
    }

    @Test
    fun addLog_sendsDateAndPage_updatesBookAndClearsField() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.method == HttpMethod.Post -> json("""{"book":${bookJson(status = "reading", currentPage = 140, percent = 27)},"logs":$logs}""")
                req.url.encodedPath.endsWith("/logs") -> json("[]")
                else -> json(bookJson())
            }
        }
        val vm = FichaViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        vm.onPage("140")
        vm.addLog()
        advanceUntilIdle()

        val post = fake.requests.last()
        assertEquals("""{"date":"${today().toString()}","page":140}""", fake.bodyOf(post))
        assertEquals(140, (vm.state.value.book as Load.Loaded).value.currentPage)
        assertEquals("", vm.state.value.pageInput)
        assertEquals("Leitura anotada.", vm.messages.first())
    }

    @Test
    fun pageOutOfRange_showsApiMessageAndKeepsInput() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.method == HttpMethod.Post -> json("""{"error":"page_out_of_range","message":"A página precisa estar entre 1 e 512."}""", HttpStatusCode.BadRequest)
                req.url.encodedPath.endsWith("/logs") -> json("[]")
                else -> json(bookJson())
            }
        }
        val vm = FichaViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        vm.onPage("600")
        vm.addLog()
        advanceUntilIdle()

        assertEquals("A página precisa estar entre 1 e 512.", vm.state.value.logError)
        assertEquals("600", vm.state.value.pageInput)
    }

    @Test
    fun decimalPage_isNotSent() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath.endsWith("/logs")) json("[]") else json(bookJson()) }
        val vm = FichaViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()
        val before = fake.requests.size

        vm.onPage("12.5")
        vm.addLog()
        advanceUntilIdle()

        assertEquals("Use um número inteiro.", vm.state.value.pageProblem)
        assertEquals(before, fake.requests.size)
    }

    @Test
    fun changeStatus_sendsToday() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.method == HttpMethod.Patch -> json(bookJson(status = "read", finishedAt = today().toString()))
                req.url.encodedPath.endsWith("/logs") -> json("[]")
                else -> json(bookJson(status = "reading"))
            }
        }
        val vm = FichaViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        vm.changeStatus(BookStatus.Read)
        advanceUntilIdle()

        assertEquals("""{"status":"read","today":"${today().toString()}"}""", fake.bodyOf(fake.requests.last()))
        assertEquals(BookStatus.Read, (vm.state.value.book as Load.Loaded).value.status)
    }

    @Test
    fun notFound_isFlagged() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { json("""{"error":"not_found","message":"Livro não encontrado."}""", HttpStatusCode.NotFound) }
        val vm = FichaViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        assertTrue(vm.state.value.notFound)
    }
}
