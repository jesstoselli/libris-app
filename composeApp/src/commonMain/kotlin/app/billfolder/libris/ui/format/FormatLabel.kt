package app.billfolder.libris.ui.format

import app.billfolder.libris.domain.model.BookFormat

/** For lists: physical is the default, so only a digital copy is worth a tag. */
fun formatTag(formats: List<BookFormat>): String? = when {
    BookFormat.Digital !in formats -> null
    BookFormat.Physical in formats -> "físico + digital"
    else -> "digital"
}

/** For the book sheet, where every field is spelled out. */
fun formatName(formats: List<BookFormat>): String = when {
    BookFormat.Physical in formats && BookFormat.Digital in formats -> "Físico e digital"
    BookFormat.Digital in formats -> "Digital"
    else -> "Físico"
}

fun BookFormat.label(): String = when (this) {
    BookFormat.Physical -> "Físico"
    BookFormat.Digital -> "Digital"
}
