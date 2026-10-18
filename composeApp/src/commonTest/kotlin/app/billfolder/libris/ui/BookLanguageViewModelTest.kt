package app.billfolder.libris.ui

import app.billfolder.libris.ui.feature.form.BookFormViewModel
import app.billfolder.libris.ui.feature.library.LibraryViewModel
import app.billfolder.libris.ui.feature.wishlist.WishlistViewModel
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BookLanguageViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val lookup = """{"isbn":"9780156001311","title":"The Name of the Rose","authors":["Umberto Eco"],"totalPages":536,"suggestedGenres":[],"source":"open_library","language":"en"}"""

    private fun server() = FakeApi(dispatcher) { req ->
        when {
            req.url.encodedPath == "/v1/genres" -> json("[]")
            req.url.encodedPath.startsWith("/v1/isbn/") -> json(lookup)
            else -> json(bookJson(id = "new"))
        }
    }

    private fun FakeApi.sentLanguage() =
        Json.parseToJsonElement(bodyOf(requests.last { it.method == HttpMethod.Post || it.method == HttpMethod.Put })!!).jsonObject["language"]!!.jsonPrimitive.content

    @Test
    fun form_startsPortuguese_andSendsTheChosenLanguage() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        assertEquals("pt", vm.state.value.language)

        vm.onLanguage("fr")
        vm.onTitle("Rosa"); vm.onAuthor(0, "Eco"); vm.onPages("512")
        vm.save()
        advanceUntilIdle()

        assertEquals("fr", fake.sentLanguage())
    }

    @Test
    fun lookup_fillsTheLanguage_unlessItWasChosen() = runTest(dispatcher) {
        val fake = server()
        val untouched = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        untouched.onScanned("9780156001311")
        advanceUntilIdle()
        assertEquals("en", untouched.state.value.language)

        val chosen = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        chosen.onLanguage("it")
        chosen.onScanned("9780156001311")
        advanceUntilIdle()
        assertEquals("it", chosen.state.value.language)
    }

    @Test
    fun editing_loadsTheBookLanguage() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(bookJson(language = "es")) }
        val vm = BookFormViewModel("b1", fake.books, fake.genres, fake.covers)
        advanceUntilIdle()

        assertEquals("es", vm.state.value.language)
    }

    @Test
    fun editing_changesTheLanguage() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(bookJson(language = "pt")) }
        val vm = BookFormViewModel("b1", fake.books, fake.genres, fake.covers)
        advanceUntilIdle()

        vm.onLanguage("en")
        vm.save()
        advanceUntilIdle()

        assertEquals("en", fake.sentLanguage())
    }

    @Test
    fun library_languageFilter_asksTheApi() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val vm = LibraryViewModel(fake.books, fake.genres)
        advanceUntilIdle()

        vm.setLanguage("en")
        advanceUntilIdle()

        assertEquals("en", fake.requests.last { it.url.encodedPath == "/v1/books" }.url.parameters["language"])
    }

    @Test
    fun wishlist_languageFilter_isInMemory() = runTest(dispatcher) {
        val shelf = "[${bookJson("a", ownership = "wishlist", status = null)},${bookJson("b", ownership = "wishlist", status = null, language = "en")}]"
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(shelf) }
        val vm = WishlistViewModel(fake.books, fake.genres)
        advanceUntilIdle()

        vm.setLanguage("en")

        assertEquals(listOf("b"), vm.state.value.visible.map { it.id })
    }
}
