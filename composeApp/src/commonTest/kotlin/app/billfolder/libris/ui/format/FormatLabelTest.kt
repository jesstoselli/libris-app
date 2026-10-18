package app.billfolder.libris.ui.format

import app.billfolder.libris.domain.model.BookFormat.Digital
import app.billfolder.libris.domain.model.BookFormat.Physical
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FormatLabelTest {
    @Test
    fun tag_onlyWhenThereIsADigitalCopy() {
        assertNull(formatTag(listOf(Physical)))
        assertEquals("digital", formatTag(listOf(Digital)))
        assertEquals("físico + digital", formatTag(listOf(Physical, Digital)))
    }

    @Test
    fun name_spellsEveryCase() {
        assertEquals("Físico", formatName(listOf(Physical)))
        assertEquals("Digital", formatName(listOf(Digital)))
        assertEquals("Físico e digital", formatName(listOf(Physical, Digital)))
    }
}
