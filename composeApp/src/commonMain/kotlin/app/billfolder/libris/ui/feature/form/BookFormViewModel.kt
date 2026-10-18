package app.billfolder.libris.ui.feature.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookDraft
import app.billfolder.libris.domain.model.BookLookup
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.model.LookupSource
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.CoverRepository
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.domain.rules.BookRules
import app.billfolder.libris.domain.rules.isSearchableIsbn
import app.billfolder.libris.domain.rules.readingDatesProblem
import app.billfolder.libris.domain.rules.withGenre
import app.billfolder.libris.domain.time.today
import app.billfolder.libris.ui.common.apiCode
import app.billfolder.libris.ui.common.message
import app.billfolder.libris.ui.common.userMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class BookFormState(
    val editing: Book? = null,
    val loadError: String? = null,
    val isbn: String = "",
    val title: String = "",
    val authors: List<String> = listOf(""),
    val totalPages: String = "",
    val publishedYear: String = "",
    val publisher: String = "",
    val synopsis: String = "",
    val genres: List<String> = emptyList(),
    val ownership: BookOwnership = BookOwnership.Owned,
    val status: BookStatus = BookStatus.NotStarted,
    val startedAt: LocalDate? = null,
    val finishedAt: LocalDate? = null,
    val coverId: String? = null,
    /** Where [coverId] can be seen; set together with it. */
    val coverUrl: String? = null,
    val pendingCoverUrl: String? = null,
    val lookingUp: Boolean = false,
    val lookupNote: String? = null,
    val lookupError: String? = null,
    val uploading: Boolean = false,
    val coverError: String? = null,
    val saving: Boolean = false,
    val saveError: String? = null,
    val touched: Set<String> = emptySet(),
) {
    val isEdit get() = editing != null
    private val owned get() = (editing?.ownership ?: ownership) == BookOwnership.Owned

    val showStart: Boolean
        get() = editing?.let { it.ownership == BookOwnership.Owned && it.status != BookStatus.NotStarted }
            ?: (ownership == BookOwnership.Owned && status != BookStatus.NotStarted)
    val showFinish: Boolean get() = if (editing != null) showStart else showStart && status == BookStatus.Read

    // Field messages mirror the API validators, so a disabled button always says why.
    val titleError get() = when {
        title.isBlank() -> "Informe o título."
        title.trim().length > BookRules.MAX_TITLE -> "O título pode ter no máximo ${BookRules.MAX_TITLE} caracteres."
        else -> null
    }
    val authorsError get() = when {
        authors.none { it.isNotBlank() } -> "Informe pelo menos um autor."
        authors.any { it.trim().length > BookRules.MAX_AUTHOR } -> "Nome de autor muito longo."
        else -> null
    }
    val pagesError: String? get() {
        if (totalPages.isBlank()) return if (owned) "Informe o número de páginas." else null
        val n = totalPages.toIntOrNull() ?: return "Use um número inteiro."
        return if (n !in BookRules.PAGES) "Use de ${BookRules.PAGES.first} a ${BookRules.PAGES.last} páginas." else null
    }
    val yearError: String? get() {
        if (publishedYear.isBlank()) return null
        val n = publishedYear.toIntOrNull() ?: return "Use um número inteiro."
        return if (n !in BookRules.YEARS) "Use um ano entre ${BookRules.YEARS.first} e ${BookRules.YEARS.last}." else null
    }
    val datesError: String? get() = readingDatesProblem(startedAt.takeIf { showStart }, finishedAt.takeIf { showFinish }, today())?.message()
    val canSubmit get() = listOf(titleError, authorsError, pagesError, yearError, datesError).all { it == null } && !uploading && !saving
    val canLookUp get() = isSearchableIsbn(isbn) && !lookingUp
    val coverPreview get() = pendingCoverUrl.takeIf { coverId == null }

    fun shows(field: String, error: String?) = error.takeIf { field in touched }
}

sealed interface FormResult {
    data class Saved(val book: Book) : FormResult
}

class BookFormViewModel(
    private val bookId: String?,
    private val books: BookRepository,
    private val genres: GenreRepository,
    private val covers: CoverRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookFormState())
    val state: StateFlow<BookFormState> = _state.asStateFlow()

    private val _results = Channel<FormResult>(Channel.BUFFERED)
    val results = _results.receiveAsFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    private val _existingGenres = MutableStateFlow<List<Genre>>(emptyList())
    val existingGenres: StateFlow<List<Genre>> = _existingGenres.asStateFlow()

    init {
        viewModelScope.launch {
            runCatching { genres.list() }.onSuccess { _existingGenres.value = it }
        }
        bookId?.let { id ->
            viewModelScope.launch {
                try {
                    load(books.get(id))
                } catch (e: Exception) {
                    _state.update { it.copy(loadError = e.userMessage()) }
                }
            }
        }
    }

    fun coverSrc(): String? = _state.value.let { if (it.coverId != null) it.coverUrl else it.coverPreview }

    fun touch(field: String) = _state.update { it.copy(touched = it.touched + field) }

    fun onIsbn(v: String) = _state.update { it.copy(isbn = v, lookupError = null) }
    fun onTitle(v: String) = _state.update { it.copy(title = v, touched = it.touched + "title") }
    fun onAuthor(index: Int, v: String) = _state.update { s -> s.copy(authors = s.authors.mapIndexed { i, a -> if (i == index) v else a }, touched = s.touched + "authors") }
    fun addAuthor() = _state.update { if (it.authors.size < BookRules.MAX_AUTHORS) it.copy(authors = it.authors + "") else it }
    fun removeAuthor(index: Int) = _state.update { s -> if (s.authors.size > 1) s.copy(authors = s.authors.filterIndexed { i, _ -> i != index }) else s }
    fun onPages(v: String) = _state.update { it.copy(totalPages = v.trim(), touched = it.touched + "pages") }
    fun onYear(v: String) = _state.update { it.copy(publishedYear = v.trim(), touched = it.touched + "year") }
    fun onPublisher(v: String) = _state.update { it.copy(publisher = v) }
    fun onSynopsis(v: String) = _state.update { it.copy(synopsis = v) }
    fun onOwnership(v: BookOwnership) = _state.update { it.copy(ownership = v) }
    fun onStatus(v: BookStatus) = _state.update { it.copy(status = v) }
    fun onStartedAt(v: LocalDate?) = _state.update { it.copy(startedAt = v) }
    fun onFinishedAt(v: LocalDate?) = _state.update { it.copy(finishedAt = v) }

    fun addGenre(raw: String) = _state.update { it.copy(genres = it.genres.withGenre(raw, knownGenres())) }

    private fun knownGenres() = _existingGenres.value.map { it.name }

    fun removeGenre(name: String) = _state.update { it.copy(genres = it.genres - name) }

    /** From the barcode scanner: fill the ISBN and look it up right away. */
    fun onScanned(isbn: String) {
        onIsbn(isbn)
        lookUp()
    }

    fun lookUp() {
        val s = _state.value
        if (!s.canLookUp) return
        _state.update { it.copy(lookingUp = true, lookupNote = null, lookupError = null) }
        viewModelScope.launch {
            try {
                apply(books.lookUp(s.isbn))
            } catch (e: Exception) {
                val message = if (e.apiCode == "isbn_not_found") "Não achei esse ISBN. Pode preencher à mão." else e.userMessage()
                _state.update { it.copy(lookingUp = false, lookupError = message) }
            }
        }
    }

    fun uploadCover(bytes: ByteArray, mimeType: String) {
        _state.update { it.copy(uploading = true, coverError = null) }
        viewModelScope.launch {
            try {
                val cover = covers.upload(bytes, mimeType)
                _state.update { it.copy(coverId = cover.id, coverUrl = cover.url, pendingCoverUrl = null, uploading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(uploading = false, coverError = e.userMessage()) }
            }
        }
    }

    fun removeCover() = _state.update { it.copy(coverId = null, coverUrl = null, pendingCoverUrl = null) }

    fun save() {
        val s = _state.value
        if (!s.canSubmit) {
            _state.update { it.copy(touched = it.touched + setOf("title", "authors", "pages", "year")) }
            return
        }
        _state.update { it.copy(saving = true, saveError = null) }
        viewModelScope.launch {
            try {
                var coverFailed = false
                // A cover suggested by the ISBN lookup is only downloaded when the book is actually saved.
                val coverId = if (s.pendingCoverUrl != null && s.coverId == null) {
                    try {
                        covers.fromUrl(s.pendingCoverUrl).id
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Exception) {
                        coverFailed = true
                        null
                    }
                } else s.coverId
                val draft = s.toDraft(coverId)
                val book = s.editing?.let { books.update(it.id, draft) } ?: books.create(draft)
                _messages.send(
                    when {
                        coverFailed -> "Livro salvo, mas não consegui baixar a capa."
                        s.isEdit -> "Livro atualizado."
                        else -> "Livro cadastrado."
                    },
                )
                _results.send(FormResult.Saved(book))
            } catch (e: Exception) {
                _state.update { it.copy(saving = false, saveError = e.userMessage()) }
            }
        }
    }

    /** Fills only what is still empty, so a lookup never overwrites what was typed; its genres are added to the chosen ones. */
    private fun apply(lookup: BookLookup) = _state.update { s ->
        val source = lookup.sources.joinToString(" e ") { if (it == LookupSource.GoogleBooks) "Google Books" else "Open Library" }
        s.copy(
            lookingUp = false,
            lookupNote = "Dados encontrados em $source.",
            title = s.title.ifBlank { lookup.title },
            authors = if (s.authors.all { it.isBlank() } && lookup.authors.isNotEmpty()) lookup.authors else s.authors,
            totalPages = s.totalPages.ifBlank { lookup.totalPages?.toString().orEmpty() },
            publisher = s.publisher.ifBlank { lookup.publisher.orEmpty() },
            publishedYear = s.publishedYear.ifBlank { lookup.publishedYear?.toString().orEmpty() },
            synopsis = s.synopsis.ifBlank { lookup.synopsis.orEmpty() },
            genres = lookup.genres.fold(s.genres) { acc, g -> acc.withGenre(g, knownGenres()) },
            pendingCoverUrl = if (s.coverId == null) lookup.coverUrl ?: s.pendingCoverUrl else s.pendingCoverUrl,
        )
    }

    private fun load(book: Book) = _state.update {
        it.copy(
            editing = book,
            isbn = book.isbn.orEmpty(),
            title = book.title,
            authors = book.authors.ifEmpty { listOf("") },
            totalPages = book.totalPages?.toString().orEmpty(),
            publishedYear = book.publishedYear?.toString().orEmpty(),
            publisher = book.publisher.orEmpty(),
            synopsis = book.synopsis.orEmpty(),
            genres = book.genres.map { g -> g.name },
            startedAt = book.startedAt,
            finishedAt = book.finishedAt,
            coverId = book.coverId,
            coverUrl = book.coverUrl,
        )
    }
}

private fun String.orNull() = trim().ifBlank { null }

/** Only what the visible fields say: dates the form hides are not sent, and a wishlist book has no status. */
private fun BookFormState.toDraft(coverId: String?) = BookDraft(
    title = title.trim(),
    authors = authors.map { it.trim() }.filter { it.isNotEmpty() },
    isbn = isbn.orNull(),
    totalPages = totalPages.toIntOrNull(),
    publisher = publisher.orNull(),
    publishedYear = publishedYear.toIntOrNull(),
    synopsis = synopsis.orNull(),
    coverId = coverId,
    genres = genres,
    ownership = ownership,
    status = if (ownership == BookOwnership.Owned) status else null,
    startedAt = startedAt.takeIf { showStart },
    finishedAt = finishedAt.takeIf { showFinish },
)
