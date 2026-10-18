package app.billfolder.libris.ui

import app.billfolder.libris.data.remote.dto.Genre
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.feature.genres.GenresViewModel
import app.billfolder.libris.ui.feature.numeros.NumerosViewModel
import app.billfolder.libris.ui.feature.numeros.defaultMonth
import app.billfolder.libris.ui.feature.wishlist.WishlistViewModel
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class Cp4ViewModelsTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun wishlist_loadsWishlistOnly_andFiltersWithoutAccents() = runTest(dispatcher) {
        val books = "[${bookJson("w1", "Rebecca", status = null, ownership = "wishlist")},${bookJson("w2", "Jane Eyre — Brontë", status = null, ownership = "wishlist")}]"
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(books) }
        val vm = WishlistViewModel(fake.session, fake.api)
        advanceUntilIdle()

        assertEquals("wishlist", fake.requests.first { it.url.encodedPath == "/v1/books" }.url.parameters["ownership"])
        assertEquals("2 livros pra procurar", vm.state.value.subtitle)
        vm.setQuery("bronte")
        assertEquals(listOf("w2"), vm.state.value.visible.map { it.id })
    }

    @Test
    fun genres_renameConflict_showsApiMessage() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            if (req.method == HttpMethod.Put) json("""{"error":"genre_already_exists","message":"Já existe um gênero chamado \"Terror\"."}""", HttpStatusCode.Conflict)
            else json("""[{"id":"g1","name":"Terror","bookCount":2},{"id":"g2","name":"Gotico","bookCount":1}]""")
        }
        val vm = GenresViewModel(fake.session, fake.api)
        advanceUntilIdle()

        vm.rename(Genre("g2", "Gotico", 1), "Terror")
        advanceUntilIdle()

        assertEquals("Já existe um gênero chamado \"Terror\".", vm.state.value.renameError)
    }

    @Test
    fun genres_delete_removesFromList() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            if (req.method == HttpMethod.Delete) json("") else json("""[{"id":"g1","name":"Terror","bookCount":2}]""")
        }
        val vm = GenresViewModel(fake.session, fake.api)
        advanceUntilIdle()

        vm.delete(Genre("g1", "Terror", 2))
        advanceUntilIdle()

        assertEquals(emptyList(), (vm.state.value.genres as Load.Loaded).value)
    }

    @Test
    fun numeros_defaultMonthRules() {
        assertEquals(10, defaultMonth(2026, 2026 to 10))
        assertEquals(12, defaultMonth(2025, 2026 to 10))
        assertEquals(1, defaultMonth(2027, 2026 to 10))
    }

    private val year = """{"year":2026,"booksFinished":17,"pagesRead":5842,"readingNow":3,"months":[${(1..12).joinToString(",") { """{"month":$it,"pagesRead":${it * 10},"booksFinished":0}""" }}]}"""
    private val month = """{"year":2026,"month":10,"pagesRead":566,"finished":[],"progressed":[]}"""

    @Test
    fun numeros_loadsCurrentYearAndMonth_thenNavigates() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath.count { it == '/' } == 4) json(month) else json(year) }
        val vm = NumerosViewModel(fake.session, fake.api) { 2026 to 10 }
        advanceUntilIdle()

        assertEquals(setOf("/v1/stats/2026", "/v1/stats/2026/10"), fake.requests.map { it.url.encodedPath }.toSet())
        assertIs<Load.Loaded<*>>(vm.state.value.stats)

        vm.goToYear(2025)
        advanceUntilIdle()

        assertEquals(listOf("/v1/stats/2025", "/v1/stats/2025/12"), fake.requests.takeLast(2).map { it.url.encodedPath }.sorted())
    }

    @Test
    fun numeros_failedMonth_showsMessage_keepsYear() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath.count { it == '/' } == 4) throw IllegalStateException("offline") else json(year) }
        val vm = NumerosViewModel(fake.session, fake.api) { 2026 to 10 }
        advanceUntilIdle()

        assertEquals(Load.Failed("Sem conexão com o servidor."), vm.state.value.detail)
        assertIs<Load.Loaded<*>>(vm.state.value.stats)
    }
}
