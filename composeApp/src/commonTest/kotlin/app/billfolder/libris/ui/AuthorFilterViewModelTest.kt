package app.billfolder.libris.ui

import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.ui.common.AuthorRequests
import app.billfolder.libris.ui.feature.library.LibraryViewModel
import app.billfolder.libris.ui.feature.wishlist.WishlistViewModel
import kotlinx.coroutines.Dispatchers
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

class AuthorFilterViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun FakeApi.lastList() = requests.last { it.url.encodedPath == "/v1/books" }.url.parameters

    @Test
    fun library_authorRequest_showsOnlyThatAuthor_clearingTheOtherFilters() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val requests = AuthorRequests()
        val vm = LibraryViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()
        vm.setStatus(BookStatus.Reading)
        vm.setQuery("rosa")
        advanceUntilIdle()

        requests.show("Umberto Eco", wishlist = false)
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals("Umberto Eco", s.author)
        assertNull(s.status)
        assertEquals("", s.query)
        assertEquals("Umberto Eco", fake.lastList()["author"])
        assertNull(fake.lastList()["status"])
    }

    @Test
    fun library_clearAuthor_goesBackToTheWholeAcervo() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val requests = AuthorRequests()
        val vm = LibraryViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()
        requests.show("Umberto Eco", wishlist = false)
        advanceUntilIdle()

        vm.setAuthor(null)
        advanceUntilIdle()

        assertNull(vm.state.value.author)
        assertEquals(false, vm.state.value.hasFilters)
    }

    @Test
    fun library_ignoresARequestForTheWishlist() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val requests = AuthorRequests()
        val vm = LibraryViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()

        requests.show("Umberto Eco", wishlist = true)
        advanceUntilIdle()

        assertNull(vm.state.value.author)
    }

    @Test
    fun wishlist_authorRequest_filtersInMemory_clearingTheOtherFilters() = runTest(dispatcher) {
        val shelf = "[${bookJson("a", ownership = "wishlist", status = null)},${bookJson("b", ownership = "wishlist", status = null, authors = listOf("Mary Shelley"))}]"
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(shelf) }
        val requests = AuthorRequests()
        val vm = WishlistViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()
        vm.setQuery("rosa")

        requests.show("Mary Shelley", wishlist = true)
        advanceUntilIdle()

        assertEquals("", vm.state.value.query)
        assertEquals(listOf("b"), vm.state.value.visible.map { it.id })
    }
}
