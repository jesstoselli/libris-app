package app.billfolder.libris.ui.format

import app.billfolder.libris.domain.model.BookFormat

/** The languages offered in the form and the filters; a book in any other keeps its code. */
val LANGUAGES = listOf(
    "pt" to "Português",
    "en" to "Inglês",
    "es" to "Espanhol",
    "fr" to "Francês",
    "it" to "Italiano",
    "de" to "Alemão",
)

fun languageName(code: String): String = LANGUAGES.firstOrNull { it.first == code }?.second ?: code.uppercase()

/** For cards: every book says its language and how it is held ("português · físico"). */
fun bookTag(formats: List<BookFormat>, language: String): String = "${languageName(language).lowercase()} · ${formatTag(formats)}"
