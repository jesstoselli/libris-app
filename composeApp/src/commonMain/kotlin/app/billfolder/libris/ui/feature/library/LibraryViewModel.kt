package app.billfolder.libris.ui.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookFilter
import app.billfolder.libris.domain.model.BookFormat
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.ui.common.FilterRequest
import app.billfolder.libris.ui.common.FilterRequests
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.loadOf
import app.billfolder.libris.ui.common.valueOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

data class LibraryState(
    val all: Load<List<Book>> = Load.Loading,
    val refreshing: Boolean = false,
    val filtered: Load<List<Book>>? = null,
    val status: BookStatus? = null,
    val genreId: String? = null,
    val format: BookFormat? = null,
    val language: String? = null,
    val query: String = "",
    val genres: List<Genre> = emptyList(),
    val author: String? = null,
    val publisher: String? = null,
    val series: String? = null,
    /** 4, 3 or 2: books rated at least that. */
    val minRating: Double? = null,
    /** The best rated first instead of the shelf order. */
    val byRating: Boolean = false,
) {
    val hasFilters get() = status != null || genreId != null || format != null || language != null || author != null || publisher != null ||
        series != null || minRating != null || byRating || query.isNotBlank()

    /** What the list shows: the unfiltered load doubles as the "Todos" view, so no filter means one request. */
    val shown: Load<List<Book>> get() = if (hasFilters) filtered ?: Load.Loading else all

    val subtitle: String?
        get() = all.valueOrNull?.let { books ->
            val reading = books.count { it.status == BookStatus.Reading }
            val base = "${books.size} ${if (books.size == 1) "livro" else "livros"} no acervo · $reading em leitura"
            // Only worth saying once something is digital; a book with both counts on each side.
            if (books.none { BookFormat.Digital in it.formats }) {
                base
            } else {
                val physical = books.count { BookFormat.Physical in it.formats }
                val digital = books.count { BookFormat.Digital in it.formats }
                "$base · $physical ${if (physical == 1) "físico" else "físicos"} · $digital ${if (digital == 1) "digital" else "digitais"}"
            }
        }
}

class LibraryViewModel(
    private val books: BookRepository,
    private val genres: GenreRepository,
    private val filterRequests: FilterRequests = FilterRequests(),
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private var filterJob: Job? = null
    private var searchJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            filterRequests.pending.filterNotNull().filter { !it.wishlist }.collect { request ->
                showOnly(request)
                filterRequests.take(request)
            }
        }
    }

    /** From a book page: that author's (or that genre's) books alone, whatever was filtered before. */
    private fun showOnly(request: FilterRequest) {
        searchJob?.cancel()
        _state.update { it.copy(status = null, genreId = request.genreId, format = null, language = null, query = "", author = request.author, publisher = request.publisher, series = request.series, minRating = null, byRating = false) }
        loadFiltered()
    }

    /** Reloads everything (also when coming back from a book detail, where things may have changed). */
    fun refresh(): List<Job> = listOfNotNull(
        viewModelScope.launch {
            runCatching { genres.list() }.onSuccess { g -> _state.update { it.copy(genres = g) } }
        },
        viewModelScope.launch {
            val load = loadOf { books.list(BookFilter(ownership = BookOwnership.Owned)) }
            _state.update { it.copy(all = load) }
        },
        if (_state.value.hasFilters) loadFiltered() else null,
    )

    /** The pull-down gesture: same reload, with the indicator up until every request has answered. */
    fun pullToRefresh() {
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true) }
        val jobs = refresh()
        viewModelScope.launch {
            jobs.joinAll()
            _state.update { it.copy(refreshing = false) }
        }
    }

    fun setSeries(series: String?) {
        _state.update { it.copy(series = series) }
        loadFiltered()
    }

    fun setMinRating(minRating: Double?) {
        _state.update { it.copy(minRating = minRating) }
        loadFiltered()
    }

    fun setByRating(byRating: Boolean) {
        _state.update { it.copy(byRating = byRating) }
        loadFiltered()
    }

    fun setPublisher(publisher: String?) {
        _state.update { it.copy(publisher = publisher) }
        loadFiltered()
    }

    fun setAuthor(author: String?) {
        _state.update { it.copy(author = author) }
        loadFiltered()
    }

    fun setStatus(status: BookStatus?) {
        _state.update { it.copy(status = status) }
        loadFiltered()
    }

    fun setLanguage(language: String?) {
        _state.update { it.copy(language = language) }
        loadFiltered()
    }

    fun setFormat(format: BookFormat?) {
        _state.update { it.copy(format = format) }
        loadFiltered()
    }

    fun setGenre(genreId: String?) {
        _state.update { it.copy(genreId = genreId) }
        loadFiltered()
    }

    fun setQuery(query: String) {
        _state.update { it.copy(query = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            loadFiltered()
        }
    }

    private fun loadFiltered(): Job? {
        filterJob?.cancel()
        val s = _state.value
        if (!s.hasFilters) {
            _state.update { it.copy(filtered = null) }
            return null
        }
        _state.update { it.copy(filtered = Load.Loading) }
        filterJob = viewModelScope.launch {
            val load = loadOf { books.list(BookFilter(BookOwnership.Owned, s.status, s.genreId, s.query, s.format, s.language, s.author, s.publisher, s.series, s.minRating, s.byRating)) }
            _state.update { it.copy(filtered = load) }
        }
        return filterJob
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
