package app.billfolder.libris.ui.format

import app.billfolder.libris.domain.model.BookFormat.Digital
import app.billfolder.libris.domain.model.BookFormat.Physical
import kotlin.test.Test
import kotlin.test.assertEquals

class FormatLabelTest {
    @Test
    fun tag_namesEveryFormat() {
        assertEquals("físico", formatTag(listOf(Physical)))
        assertEquals("digital", formatTag(listOf(Digital)))
        assertEquals("físico + digital", formatTag(listOf(Physical, Digital)))
    }

    @Test
    fun name_spellsEveryCase() {
        assertEquals("Físico", formatName(listOf(Physical)))
        assertEquals("Digital", formatName(listOf(Digital)))
        assertEquals("Físico e digital", formatName(listOf(Physical, Digital)))
    }

    @Test
    fun bookTag_alwaysNamesTheLanguageAndTheFormat() {
        assertEquals("português · físico", bookTag(listOf(Physical), "pt"))
        assertEquals("inglês · físico", bookTag(listOf(Physical), "en"))
        assertEquals("francês · digital", bookTag(listOf(Digital), "fr"))
        assertEquals("português · físico + digital", bookTag(listOf(Physical, Digital), "pt"))
    }

    @Test
    fun languageName_fallsBackToTheCode() {
        assertEquals("Português", languageName("pt"))
        assertEquals("NL", languageName("nl"))
    }
}
