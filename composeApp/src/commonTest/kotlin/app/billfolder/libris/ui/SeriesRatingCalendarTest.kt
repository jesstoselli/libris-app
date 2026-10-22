package app.billfolder.libris.ui

import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.ui.common.FilterRequests
import app.billfolder.libris.ui.feature.bookdetail.BookDetailViewModel
import app.billfolder.libris.ui.feature.form.BookFormViewModel
import app.billfolder.libris.ui.feature.form.seriesNumberText
import app.billfolder.libris.ui.feature.library.LibraryViewModel
import app.billfolder.libris.ui.feature.stats.levelOf
import app.billfolder.libris.ui.feature.stats.weeksOf
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SeriesRatingCalendarTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun form_sendsSeriesAndNumber_acceptingAComma() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when (req.url.encodedPath) {
                "/v1/series" -> json("""[{"name":"Bórgia","bookCount":3,"wishlistCount":1}]""")
                "/v1/books" -> if (req.method == HttpMethod.Post) json(bookJson()) else json("[]")
                else -> json("[]")
            }
        }
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        assertEquals(listOf("Bórgia"), vm.knownSeries.value)
        vm.onTitle("Sangue para o Papa"); vm.onAuthor(0, "Milo Manara"); vm.onPages("64")

        vm.onSeries("Bórgia")
        vm.onSeriesNumber("1,5")
        assertEquals(1.5, vm.state.value.seriesNumberValue)
        vm.save()
        advanceUntilIdle()

        val series = Json.parseToJsonElement(fake.bodyOf(fake.requests.last { it.method == HttpMethod.Post })!!).jsonObject["series"]!!.jsonObject
        assertEquals("Bórgia", series["name"]!!.jsonPrimitive.content)
        assertEquals("1.5", series["number"]!!.jsonPrimitive.content)
    }

    @Test
    fun form_badNumber_blocksTheSave() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { json("[]") }
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("X"); vm.onAuthor(0, "Y"); vm.onPages("10"); vm.onSeries("S")

        vm.onSeriesNumber("1.2.3")

        assertTrue(vm.state.value.seriesNumberError != null)
        assertEquals(false, vm.state.value.canSubmit)
    }

    @Test
    fun seriesNumberText_readsLikeAVolume() {
        assertEquals("2", seriesNumberText(2.0))
        assertEquals("1,5", seriesNumberText(1.5))
        assertEquals("0", seriesNumberText(0.0))
    }

    @Test
    fun detail_showsItsPlaceInTheSeries_andTheNextBook() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.url.encodedPath == "/v1/series/books" -> json(
                    "[${bookJson("b0", "Sangue para o Papa", series = "Bórgia", seriesNumber = 1.0)}," +
                        "${bookJson(series = "Bórgia", seriesNumber = 2.0)}," +
                        "${bookJson("b3", "Tudo É Vaidade", status = null, ownership = "wishlist", series = "Bórgia", seriesNumber = 4.0)}]",
                )
                req.url.encodedPath.endsWith("/logs") -> json("[]")
                else -> json(bookJson(series = "Bórgia", seriesNumber = 2.0))
            }
        }
        val vm = BookDetailViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        assertEquals("Bórgia", fake.requests.first { it.url.encodedPath == "/v1/series/books" }.url.parameters["name"])
        assertEquals("nº 2 · 3 livros da série com você", vm.state.value.seriesPlace)
        assertEquals("b3", vm.state.value.nextInSeries?.id)
    }

    @Test
    fun detail_rate_sendsTheValue_andNullClears() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when {
                req.method == HttpMethod.Patch -> json(bookJson(status = "read", rating = 4.5))
                req.url.encodedPath.endsWith("/logs") -> json("[]")
                else -> json(bookJson(status = "read"))
            }
        }
        val vm = BookDetailViewModel("b1", fake.books, fake.reading)
        advanceUntilIdle()

        vm.rate(4.5)
        advanceUntilIdle()
        assertEquals("""{"rating":4.5}""", fake.bodyOf(fake.requests.last()))
        assertEquals(4.5, vm.state.value.book.valueOrNullBook()?.rating)

        vm.rate(null)
        advanceUntilIdle()
        assertEquals("""{"rating":null}""", fake.bodyOf(fake.requests.last()))
    }

    @Test
    fun library_seriesRequest_andRatingFilters_reachTheApi() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json("[${bookJson()}]") }
        val requests = FilterRequests()
        val vm = LibraryViewModel(fake.books, fake.genres, requests)
        advanceUntilIdle()

        requests.showSeries("Bórgia", wishlist = false)
        advanceUntilIdle()
        assertEquals("Bórgia", fake.requests.last { it.url.encodedPath == "/v1/books" }.url.parameters["series"])

        vm.setMinRating(4.0)
        vm.setByRating(true)
        advanceUntilIdle()
        val params = fake.requests.last { it.url.encodedPath == "/v1/books" }.url.parameters
        assertEquals("4.0", params["minRating"])
        assertEquals("rating", params["sort"])
        assertNull(vm.state.value.status)
    }

    @Test
    fun calendar_weeksStartOnSunday_andShadesFollowFixedSteps() {
        val weeks = weeksOf(2026)
        // 1 January 2026 is a Thursday: four blanks before it, 365 days in all.
        assertEquals(listOf(null, null, null, null), weeks.first().take(4))
        assertEquals(LocalDate(2026, 1, 1), weeks.first()[4])
        assertEquals(365, weeks.flatten().count { it != null })
        assertEquals(listOf(1, 2, 3, 4), listOf(10, 25, 75, 150).map(::levelOf))
    }
}

private fun <T> app.billfolder.libris.ui.common.Load<T>.valueOrNullBook(): T? = (this as? app.billfolder.libris.ui.common.Load.Loaded<T>)?.value
