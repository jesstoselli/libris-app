package app.billfolder.libris.ui.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookFilter
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.loadOf
import app.billfolder.libris.ui.common.valueOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

data class LibraryState(
    val all: Load<List<Book>> = Load.Loading,
    val refreshing: Boolean = false,
    val filtered: Load<List<Book>>? = null,
    val status: BookStatus? = null,
    val genreId: String? = null,
    val query: String = "",
    val genres: List<Genre> = emptyList(),
) {
    val hasFilters get() = status != null || genreId != null || query.isNotBlank()

    /** What the list shows: the unfiltered load doubles as the "Todos" view, so no filter means one request. */
    val shown: Load<List<Book>> get() = if (hasFilters) filtered ?: Load.Loading else all

    val subtitle: String?
        get() = all.valueOrNull?.let { books ->
            val reading = books.count { it.status == BookStatus.Reading }
            "${books.size} ${if (books.size == 1) "livro" else "livros"} no acervo · $reading em leitura"
        }
}

class LibraryViewModel(private val books: BookRepository, private val genres: GenreRepository) : ViewModel() {

    private val _state = MutableStateFlow(LibraryState())
    val state: StateFlow<LibraryState> = _state.asStateFlow()

    private var filterJob: Job? = null
    private var searchJob: Job? = null

    init {
        refresh()
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

    fun setStatus(status: BookStatus?) {
        _state.update { it.copy(status = status) }
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
            val load = loadOf { books.list(BookFilter(BookOwnership.Owned, s.status, s.genreId, s.query)) }
            _state.update { it.copy(filtered = load) }
        }
        return filterJob
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
