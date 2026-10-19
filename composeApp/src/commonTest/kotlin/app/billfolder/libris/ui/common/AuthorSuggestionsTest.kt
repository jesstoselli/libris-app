package app.billfolder.libris.ui.common

import kotlin.test.Test
import kotlin.test.assertEquals

class AuthorSuggestionsTest {
    private val known = listOf("Brian Christian", "Charlotte Brontë", "Émile Zola", "H. P. Lovecraft", "Tom Griffiths", "Umberto Eco")

    @Test
    fun matchesTheStartOfAnyWord_ignoringCaseAndAccents() {
        assertEquals(listOf("Umberto Eco"), authorSuggestions(known, "eco", emptyList()))
        assertEquals(listOf("H. P. Lovecraft"), authorSuggestions(known, "LOVE", emptyList()))
        assertEquals(listOf("Émile Zola"), authorSuggestions(known, "emi", emptyList()))
        assertEquals(listOf("Charlotte Brontë"), authorSuggestions(known, "bronte", emptyList()))
    }

    @Test
    fun ignoresTextInTheMiddleOfAWord() {
        assertEquals(emptyList(), authorSuggestions(known, "berto", emptyList()))
    }

    @Test
    fun nothingForBlankText_orAnAuthorAlreadyTypedInFull() {
        assertEquals(emptyList(), authorSuggestions(known, "  ", emptyList()))
        assertEquals(emptyList(), authorSuggestions(known, "umberto eco", emptyList()))
    }

    @Test
    fun skipsAuthorsAlreadyInTheOtherFields() {
        assertEquals(listOf("Tom Griffiths"), authorSuggestions(known, "t", listOf("Brian Christian")))
        assertEquals(emptyList(), authorSuggestions(known, "tom", listOf("tom griffiths")))
    }

    @Test
    fun atMostFive() {
        val many = (1..8).map { "Ana $it" }
        assertEquals(5, authorSuggestions(many, "ana", emptyList()).size)
    }
}
