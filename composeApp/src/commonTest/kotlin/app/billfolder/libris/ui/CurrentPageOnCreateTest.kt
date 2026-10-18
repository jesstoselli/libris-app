package app.billfolder.libris.ui

import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.ui.feature.form.BookFormViewModel
import io.ktor.http.HttpMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CurrentPageOnCreateTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest fun tearDown() = Dispatchers.resetMain()

    private fun server() = FakeApi(dispatcher) { req -> if (req.url.encodedPath == "/v1/genres") json("[]") else json(bookJson(id = "new")) }

    private fun FakeApi.created() = Json.parseToJsonElement(bodyOf(requests.last { it.method == HttpMethod.Post })!!).jsonObject

    @Test
    fun reading_sendsThePageAlreadyReached() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("Rosa"); vm.onAuthor(0, "Eco"); vm.onPages("512")
        assertFalse(vm.state.value.showCurrentPage, "not started has no progress")

        vm.onStatus(BookStatus.Reading)
        assertTrue(vm.state.value.showCurrentPage)
        vm.onCurrentPage("128")
        vm.save()
        advanceUntilIdle()

        assertEquals(128, fake.created()["currentPage"]!!.jsonPrimitive.content.toInt())
    }

    @Test
    fun pageBeyondTheBook_isExplained_andBlocksSaving() = runTest(dispatcher) {
        val vm = server().let { BookFormViewModel(null, it.books, it.genres, it.covers) }
        vm.onTitle("Rosa"); vm.onAuthor(0, "Eco"); vm.onPages("512"); vm.onStatus(BookStatus.Abandoned)

        vm.onCurrentPage("600")

        assertEquals("Use de 1 a 512.", vm.state.value.currentPageError)
        assertFalse(vm.state.value.canSubmit)
    }

    @Test
    fun notStarted_doesNotSendAPage_evenIfOneWasTyped() = runTest(dispatcher) {
        val fake = server()
        val vm = BookFormViewModel(null, fake.books, fake.genres, fake.covers)
        advanceUntilIdle()
        vm.onTitle("Rosa"); vm.onAuthor(0, "Eco"); vm.onPages("512")
        vm.onStatus(BookStatus.Reading); vm.onCurrentPage("50"); vm.onStatus(BookStatus.NotStarted)

        assertNull(vm.state.value.currentPageError)
        vm.save()
        advanceUntilIdle()

        assertEquals(JsonNull, fake.created()["currentPage"])
    }
}
