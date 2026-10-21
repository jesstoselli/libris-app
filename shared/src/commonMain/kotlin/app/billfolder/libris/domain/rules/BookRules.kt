package app.billfolder.libris.domain.rules

import kotlinx.datetime.LocalDate

/** The limits libris-api enforces, so the app can say why before the server refuses. */
object BookRules {
    const val MAX_TITLE = 300
    const val MAX_AUTHOR = 200
    const val MAX_AUTHORS = 10
    const val MAX_GENRES = 10
    val PAGES = 1..20000
    val YEARS = 1400..2100
}

enum class DateProblem { FinishBeforeStart, InFuture }

/** Reading dates: the finish can't precede the start, and neither can be after [today] (the device's local day). */
fun readingDatesProblem(startedAt: LocalDate?, finishedAt: LocalDate?, today: LocalDate): DateProblem? = when {
    startedAt != null && finishedAt != null && finishedAt < startedAt -> DateProblem.FinishBeforeStart
    (startedAt != null && startedAt > today) || (finishedAt != null && finishedAt > today) -> DateProblem.InFuture
    else -> null
}

/**
 * The start the app no longer shows still lives on older books; it rides along on saves so it is not wiped, unless a
 * new finish would come before it, which the API would refuse for a reason nobody can see.
 */
fun keptStart(start: LocalDate?, finish: LocalDate?): LocalDate? = if (start != null && finish != null && finish < start) null else start

/**
 * Adds a genre once, ignoring case and stray commas, up to [BookRules.MAX_GENRES]. An existing genre keeps the
 * spelling it is stored with, so "terror" typed today joins the "Terror" created last year.
 */
fun List<String>.withGenre(raw: String, known: List<String>): List<String> {
    val name = raw.replace(",", "").trim()
    if (name.isEmpty() || size >= BookRules.MAX_GENRES || any { it.equals(name, ignoreCase = true) }) return this
    return this + (known.firstOrNull { it.equals(name, ignoreCase = true) } ?: name)
}
