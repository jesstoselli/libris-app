package app.billfolder.libris.ui

import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.time.today
import app.billfolder.libris.ui.feature.form.BookFormViewModel
import app.billfolder.libris.ui.feature.form.FormResult
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlinx.datetime.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BookFormViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private val lookup = """{"isbn":"9780156001311","title":"The Name of the Rose","authors":["Umberto Eco"],"totalPages":536,"publisher":"Harcourt","publishedYear":1994,"synopsis":"An abbey.","coverUrl":"https://covers.openlibrary.org/b/id/1-L.jpg","suggestedGenres":["Mystery","History"],"source":"open_library"}"""

    private fun server(fromUrlFails: Boolean = false) = FakeApi(dispatcher) { req ->
        when {
            req.url.encodedPath == "/v1/genres" -> json("""[{"id":"g1","name":"Terror","bookCount":1}]""")
            req.url.encodedPath.startsWith("/v1/isbn/") -> json(lookup)
            req.url.encodedPath == "/v1/covers/from-url" ->
                if (fromUrlFails) json("""{"error":"download_failed","message":"x"}""", HttpStatusCode.BadGateway) else json("""{"coverId":"c9","url":"/v1/covers/c9"}""")
            req.method == HttpMethod.Post && req.url.encodedPath == "/v1/books" -> json(bookJson(id = "new"))
            req.method == HttpMethod.Put -> json(bookJson())
            else -> json(bookJson(status = "reading", startedAt = "2026-10-02"))
        }
    }

    private fun bodyJson(fake: FakeApi, path: String) =
        Json.parseToJsonElement(fake.bodyOf(fake.requests.last { it.url.encodedPath == path && it.method != HttpMethod.Get })!!).jsonObject

    @Test
    fun create_sendsTodayAndDefaults() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("O Nome da Rosa"); vm.onAuthor(0, "Umberto Eco"); vm.onPages("512")

        vm.save()
        advanceUntilIdle()

        val body = bodyJson(fake, "/v1/books")
        assertEquals("owned", body["ownership"]!!.jsonPrimitive.content)
        assertEquals("not_started", body["status"]!!.jsonPrimitive.content)
        assertEquals(today().toString(), body["today"]!!.jsonPrimitive.content)
        assertEquals(JsonNull, body["startedAt"])
        assertIs<FormResult.Saved>(vm.results.first())
    }

    @Test
    fun blankTitleAndDecimalPages_explainWhy() = runTest(dispatcher) {
        val vm = server().let { BookFormViewModel(null, it.books, it.genres, it.covers) }
        vm.onTitle("   "); vm.onAuthor(0, "Eco"); vm.onPages("12.5"); vm.onYear("3000")

        val s = vm.state.value
        assertEquals("Informe o título.", s.titleError)
        assertEquals("Use um número inteiro.", s.pagesError)
        assertEquals("Use um ano entre 1400 e 2100.", s.yearError)
        assertFalse(s.canSubmit)
    }

    @Test
    fun wishlist_doesNotNeedPages_andHidesDates() = runTest(dispatcher) {
        val vm = server().let { BookFormViewModel(null, it.books, it.genres, it.covers) }
        vm.onTitle("Rebecca"); vm.onAuthor(0, "Daphne du Maurier"); vm.onStatus(BookStatus.Read)
        assertTrue(vm.state.value.showFinish)

        vm.onOwnership(BookOwnership.Wishlist)

        assertTrue(vm.state.value.canSubmit)
        assertFalse(vm.state.value.showStart)
    }

    @Test
    fun readWithPastDates_areSent_andFutureRejected() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("Rosa"); vm.onAuthor(0, "Eco"); vm.onPages("512"); vm.onStatus(BookStatus.Read)
        vm.onStartedAt(LocalDate.parse("2024-03-20")); vm.onFinishedAt(LocalDate.parse("2024-03-01"))
        assertEquals("O término não pode ser antes do início.", vm.state.value.datesError)

        vm.onStartedAt(LocalDate.parse("2024-03-01")); vm.onFinishedAt(LocalDate.parse("2024-03-20"))
        vm.save()
        advanceUntilIdle()

        val body = bodyJson(fake, "/v1/books")
        assertEquals("2024-03-20", body["finishedAt"]!!.jsonPrimitive.content)
    }

    @Test
    fun isbnLookup_fillsOnlyEmptyFields() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("Meu título")

        vm.onScanned("9780156001311")
        advanceUntilIdle()

        val s = vm.state.value
        assertEquals("Meu título", s.title)
        assertEquals(listOf("Umberto Eco"), s.authors)
        assertEquals("536", s.totalPages)
        assertEquals("Dados encontrados em Open Library.", s.lookupNote)
    }

    @Test
    fun isbnLookup_addsItsGenresToTheBook_withoutDuplicates_andSavesThem() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("Rosa"); vm.onPages("512")
        vm.addGenre("history")

        vm.onScanned("9780156001311")
        advanceUntilIdle()

        assertEquals(listOf("history", "Mystery"), vm.state.value.genres)
        vm.save()
        advanceUntilIdle()
        val sent = bodyJson(fake, "/v1/books")["genres"]!!.jsonArray.map { it.jsonPrimitive.content }
        assertEquals(listOf("history", "Mystery"), sent)
    }

    @Test
    fun save_withSuggestedCover_downloadsThenCreates() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onScanned("9780156001311")
        advanceUntilIdle()

        vm.save()
        advanceUntilIdle()

        val paths = fake.requests.map { it.url.encodedPath }
        assertTrue(paths.indexOf("/v1/covers/from-url") < paths.lastIndexOf("/v1/books"))
        assertEquals("c9", bodyJson(fake, "/v1/books")["coverId"]!!.jsonPrimitive.content)
    }

    @Test
    fun save_coverDownloadFails_savesWithoutCoverAndWarns() = runTest(dispatcher) {
        val fake = server(fromUrlFails = true)
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onScanned("9780156001311")
        advanceUntilIdle()

        vm.save()
        advanceUntilIdle()

        assertEquals(JsonNull, bodyJson(fake, "/v1/books")["coverId"])
        assertEquals("Livro salvo, mas não consegui baixar a capa.", vm.messages.first())
    }

    @Test
    fun genre_reusesStoredSpelling_andIgnoresDuplicates() = runTest(dispatcher) {
        val vm = server().let { BookFormViewModel(null, it.books, it.genres, it.covers) }
        advanceUntilIdle()

        vm.addGenre("terror"); vm.addGenre("TERROR"); vm.addGenre("Gótico,")

        assertEquals(listOf("Terror", "Gótico"), vm.state.value.genres)
    }

    @Test
    fun edit_loadsBookAndPutsFullRequest() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel("b1", fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        assertEquals("O Nome da Rosa", vm.state.value.title)

        vm.onTitle("Il nome della rosa")
        vm.save()
        advanceUntilIdle()

        val body = bodyJson(fake, "/v1/books/b1")
        assertEquals("Il nome della rosa", body["title"]!!.jsonPrimitive.content)
        assertEquals("2026-10-02", body["startedAt"]!!.jsonPrimitive.content)
    }

    @Test
    fun knownAuthors_loadForTheSuggestions() = runTest(dispatcher) {
        val fake = FakeApi(dispatcher) { req ->
            when (req.url.encodedPath) {
                "/v1/genres" -> json("[]")
                "/v1/authors" -> json("""["Umberto Eco"]""")
                else -> json(bookJson())
            }
        }
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()

        assertEquals(listOf("Umberto Eco"), vm.knownAuthors.value)
    }
}
