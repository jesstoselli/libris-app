package app.billfolder.libris.ui.acervo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.Book
import app.billfolder.libris.shared.BookListQuery
import app.billfolder.libris.shared.BookOwnership
import app.billfolder.libris.shared.BookStatus
import app.billfolder.libris.shared.Genre
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.userMessage
import app.billfolder.libris.ui.Load
import app.billfolder.libris.ui.valueOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AcervoState(
    val all: Load<List<Book>> = Load.Loading,
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

class AcervoViewModel(private val session: AuthSession, private val api: LibrisApi) : ViewModel() {

    private val _state = MutableStateFlow(AcervoState())
    val state: StateFlow<AcervoState> = _state.asStateFlow()

    private var filterJob: Job? = null
    private var searchJob: Job? = null

    init {
        refresh()
    }

    fun coverSrc(book: Book) = api.coverSrc(book.coverUrl)

    /** Reloads everything (also when coming back from a Ficha, where things may have changed). */
    fun refresh() {
        viewModelScope.launch {
            runCatching { session.authorized { api.listGenres(it) } }.onSuccess { g -> _state.update { it.copy(genres = g) } }
        }
        viewModelScope.launch {
            val load = try {
                Load.Loaded(session.authorized { api.listBooks(it, BookListQuery(ownership = BookOwnership.Owned)) })
            } catch (e: Exception) {
                Load.Failed(e.userMessage())
            }
            _state.update { it.copy(all = load) }
        }
        if (_state.value.hasFilters) loadFiltered()
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

    private fun loadFiltered() {
        filterJob?.cancel()
        val s = _state.value
        if (!s.hasFilters) {
            _state.update { it.copy(filtered = null) }
            return
        }
        _state.update { it.copy(filtered = Load.Loading) }
        filterJob = viewModelScope.launch {
            val query = BookListQuery(BookOwnership.Owned, s.status, s.genreId, s.query.trim().ifBlank { null })
            val load = try {
                Load.Loaded(session.authorized { api.listBooks(it, query) })
            } catch (e: Exception) {
                Load.Failed(e.userMessage())
            }
            _state.update { it.copy(filtered = load) }
        }
    }

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 300L
    }
}
