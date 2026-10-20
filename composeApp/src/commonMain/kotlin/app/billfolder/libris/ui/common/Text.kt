package app.billfolder.libris.ui.common

private val ACCENTS = mapOf(
    'á' to 'a', 'à' to 'a', 'â' to 'a', 'ã' to 'a', 'ä' to 'a',
    'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
    'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
    'ó' to 'o', 'ò' to 'o', 'ô' to 'o', 'õ' to 'o', 'ö' to 'o',
    'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
    'ç' to 'c', 'ñ' to 'n',
)

/** Lowercase without accents, so "bronte" matches "Brontë" in client-side filters. */
fun fold(text: String): String = text.lowercase().map { ACCENTS[it] ?: it }.joinToString("")

/** Known authors whose name has a word starting with [typed]; the form shows them under the author field. */
fun authorSuggestions(known: List<String>, typed: String, chosen: List<String>): List<String> {
    val term = fold(typed.trim())
    if (term.isEmpty()) return emptyList()
    val taken = chosen.map { fold(it.trim()) }.toSet()
    return known.filter { name ->
        val folded = fold(name)
        folded != term && folded !in taken &&
            (folded.startsWith(term) || folded.split(' ').any { it.startsWith(term) })
    }.take(MAX_SUGGESTIONS)
}

/**
 * Known publishers matching what is typed, compared without words like "Editora" or "Ltda." so that
 * "Editora Rocco" from an ISBN lookup still offers the catalogue's "Rocco". Only the catalogue's exact spelling hides
 * it, so "rocco" or "Atica" offer "Rocco" and "Ática". Imprints stay apart: only those words go.
 */
fun publisherSuggestions(known: List<String>, typed: String): List<String> {
    val term = publisherCore(typed)
    if (term.isEmpty()) return emptyList()
    val exact = typed.trim()
    return known.filter { name ->
        val core = publisherCore(name)
        name != exact && (core.startsWith(term) || core.split(' ').any { it.startsWith(term) })
    }.take(MAX_SUGGESTIONS)
}

private const val MAX_SUGGESTIONS = 5

private val PUBLISHER_NOISE = setOf("editora", "ed", "ltda", "sa", "s/a", "s.a", "eireli")

private fun publisherCore(name: String): String =
    fold(name).split(Regex("\\s+"))
        .map { it.trim(',', '.', '-', '–') }
        .filter { it.isNotEmpty() && it !in PUBLISHER_NOISE }
        .joinToString(" ")
