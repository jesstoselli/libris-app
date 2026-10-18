package app.billfolder.libris.ui

import app.billfolder.libris.domain.model.BookFormat
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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BookFormatsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val shelf = "[" + listOf(
        bookJson("p", "Papel"),
        bookJson("d", "Tela", formats = listOf("digital")),
        bookJson("b", "Ambos", formats = listOf("physical", "digital")),
    ).joinToString(",") + "]"

    @Test
    fun library_countsFormats_onlyWhenThereIsADigitalBook() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(shelf) }
        val vm = LibraryViewModel(fake.books, fake.genres)
        advanceUntilIdle()

        assertEquals("3 livros no acervo · 0 em leitura · 2 físicos · 2 digitais", vm.state.value.subtitle)
    }

    @Test
    fun library_formatFilter_asksTheApi() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(shelf) }
        val vm = LibraryViewModel(fake.books, fake.genres)
        advanceUntilIdle()

        vm.setFormat(BookFormat.Digital)
        advanceUntilIdle()

        assertEquals("digital", fake.requests.last { it.url.encodedPath == "/v1/books" }.url.parameters["format"])
    }

    @Test
    fun wishlist_formatFilter_isInMemory() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(shelf) }
        val vm = WishlistViewModel(fake.books, fake.genres)
        advanceUntilIdle()

        vm.setFormat(BookFormat.Physical)

        assertEquals(listOf("p", "b"), vm.state.value.visible.map { it.id })
    }

    @Test
    fun form_startsPhysical_sendsBothWhenDigitalIsAdded_andKeepsAtLeastOne() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.url.encodedPath == "/v1/genres" -> json("[]")
                else -> json(bookJson(id = "new"))
            }
        }
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        assertEquals(setOf(BookFormat.Physical), vm.state.value.formats)

        vm.toggleFormat(BookFormat.Physical)
        assertEquals(setOf(BookFormat.Physical), vm.state.value.formats, "the last format can't be removed")

        vm.toggleFormat(BookFormat.Digital)
        vm.onTitle("Rosa"); vm.onAuthor(0, "Eco"); vm.onPages("512")
        vm.save()
        advanceUntilIdle()

        val body = Json.parseToJsonElement(fake.bodyOf(fake.requests.last { it.method == HttpMethod.Post })!!).jsonObject
        assertEquals(listOf("physical", "digital"), body["formats"]!!.jsonArray.map { it.jsonPrimitive.content })
    }

    @Test
    fun form_editing_loadsTheBookFormats() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            if (req.url.encodedPath == "/v1/genres") json("[]") else json(bookJson(formats = listOf("digital")))
        }
        val vm = BookFormViewModel("b1", fake.books, fake.genres, fake.covers)
        advanceUntilIdle()

        assertEquals(setOf(BookFormat.Digital), vm.state.value.formats)
    }
}
