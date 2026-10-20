package app.billfolder.libris.ui.common

import kotlin.test.Test
import kotlin.test.assertEquals

class PublisherSuggestionsTest {
    private val known = listOf("Ática", "Companhia das Letras", "Editora Arqueiro", "Rocco", "Seguinte")

    @Test
    fun matchesTheStartOfAnyWord_ignoringCaseAndAccents() {
        assertEquals(listOf("Ática"), publisherSuggestions(known, "atica"))
        assertEquals(listOf("Companhia das Letras"), publisherSuggestions(known, "letr"))
        assertEquals(listOf("Rocco"), publisherSuggestions(known, "ROC"))
    }

    @Test
    fun offersTheCataloguedSpelling_whenOnlyEditoraOrLtdaDiffer() {
        assertEquals(listOf("Rocco"), publisherSuggestions(known, "Editora Rocco"))
        assertEquals(listOf("Rocco"), publisherSuggestions(known, "Rocco Editora Ltda."))
        assertEquals(listOf("Rocco"), publisherSuggestions(known, "Ed. Rocco S.A."))
        assertEquals(listOf("Editora Arqueiro"), publisherSuggestions(known, "Arqueiro"))
    }

    @Test
    fun offersTheCataloguedSpelling_whenOnlyCaseOrAccentsDiffer() {
        assertEquals(listOf("Rocco"), publisherSuggestions(known, "rocco"))
        assertEquals(listOf("Ática"), publisherSuggestions(known, "Atica"))
    }

    @Test
    fun keepsImprintsApart() {
        assertEquals(emptyList(), publisherSuggestions(known, "Companhia das Letras"))
        assertEquals(listOf("Seguinte"), publisherSuggestions(known, "seg"))
    }

    @Test
    fun nothingForBlankText_noiseAlone_orThePublisherAlreadyTypedInFull() {
        assertEquals(emptyList(), publisherSuggestions(known, "  "))
        assertEquals(emptyList(), publisherSuggestions(known, "Editora "))
        assertEquals(emptyList(), publisherSuggestions(known, "Rocco"))
    }

    @Test
    fun atMostFive() {
        val many = (1..8).map { "Casa $it" }
        assertEquals(5, publisherSuggestions(many, "casa").size)
    }
}
