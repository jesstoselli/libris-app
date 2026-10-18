package app.billfolder.libris.domain.rules

/** A book's barcode is an EAN-13 in the "Bookland" ranges; anything else the camera sees is not an ISBN. */
fun looksLikeIsbn(code: String): Boolean = code.length == 13 && code.all { it.isDigit() } && (code.startsWith("978") || code.startsWith("979"))

/** Worth a lookup once it has the ten characters of the shortest ISBN (the check digit may be an X). */
fun isSearchableIsbn(text: String): Boolean = text.count { it.isDigit() || it == 'X' || it == 'x' } >= 10
