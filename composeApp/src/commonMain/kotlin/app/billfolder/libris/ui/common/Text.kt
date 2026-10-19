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

private const val MAX_SUGGESTIONS = 5
