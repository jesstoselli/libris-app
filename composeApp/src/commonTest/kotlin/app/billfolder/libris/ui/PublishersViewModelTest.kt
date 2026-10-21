package app.billfolder.libris.ui

import app.billfolder.libris.ui.common.FilterRequests
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.feature.library.LibraryViewModel
import app.billfolder.libris.ui.feature.publishers.PublishersViewModel
import app.billfolder.libris.ui.feature.publishers.countLabel
import app.billfolder.libris.ui.feature.publishers.onlyOnWishlist
import app.billfolder.libris.ui.feature.wishlist.WishlistViewModel
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PublishersViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val summary = """[{"name":"Editora Rocco Ltda.","bookCount":2,"wishlistCount":0},{"name":"Rocco","bookCount":3,"wishlistCount":1},{"name":"Ática","bookCount":0,"wishlistCount":2}]"""

    @Test
    fun load_listsEachSpellingWithItsCounts() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { json(summary) }
        val vm = PublishersViewModel(fake.books)
        advanceUntilIdle()

        val list = (vm.state.value.publishers as Load.Loaded).value
        assertEquals(listOf("Editora Rocco Ltda.", "Rocco", "Ática"), list.map { it.name })
        assertEquals(listOf("2 livros", "3 livros · 1 na wishlist", "2 na wishlist"), list.map { it.countLabel() })
        assertEquals(listOf(false, false, true), list.map { it.onlyOnWishlist })
    }

    @Test
    fun rename_sendsFromAndTo_saysHowManyBooks_andReloads() = runTest(dispatcher) {
        var reloaded = false
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.method == HttpMethod.Put -> json("""{"updated":2}""")
                else -> { if (reloaded) json(summary.replace("""{"name":"Editora Rocco Ltda.","bookCount":2,"wishlistCount":0},""", "")) else { reloaded = true; json(summary) } }
            }
        }
        val vm = PublishersViewModel(fake.books)
        advanceUntilIdle()
        val messages = mutableListOf<String>()
        backgroundScope.launch(dispatcher) { vm.messages.collect { messages += it } }
        val first = (vm.state.value.publishers as Load.Loaded).value.first()

        vm.rename(first, "  Rocco ")
        advanceUntilIdle()

        assertEquals("""{"from":"Editora Rocco Ltda.","to":"Rocco"}""", fake.bodyOf(fake.requests.first { it.method == HttpMethod.Put }))
        assertEquals(listOf("Editora renomeada em 2 livros."), messages)
        assertEquals(2, (vm.state.value.publishers as Load.Loaded).value.size)
    }

    @Test
    fun library_publisherRequest_asksTheApi_clearingTheOtherFilters() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val requests = FilterRequests()
        val vm = LibraryViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()
        vm.setQuery("rosa")
        requests.showAuthor("Umberto Eco", wishlist = false)
        advanceUntilIdle()

        requests.showPublisher("Rocco", wishlist = false)
        advanceUntilIdle()

        assertEquals("Rocco", vm.state.value.publisher)
        assertNull(vm.state.value.author)
        assertEquals("Rocco", fake.requests.last { it.url.encodedPath == "/v1/books" }.url.parameters["publisher"])

        vm.setPublisher(null)
        advanceUntilIdle()
        assertEquals(false, vm.state.value.hasFilters)
    }

    @Test
    fun wishlist_publisherRequest_filtersInMemory() = runTest(dispatcher) {
        val shelf = "[${bookJson("a", ownership = "wishlist", status = null).replace("\"ownership\"", "\"publisher\":\"Rocco\",\"ownership\"")},${bookJson("b", ownership = "wishlist", status = null)}]"
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(shelf) }
        val requests = FilterRequests()
        val vm = WishlistViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()

        requests.showPublisher("Rocco", wishlist = true)
        advanceUntilIdle()

        assertEquals(listOf("a"), vm.state.value.visible.map { it.id })
    }
}
