package app.billfolder.libris.ui.feature.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookFilter
import app.billfolder.libris.domain.model.BookFormat
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.fold
import app.billfolder.libris.ui.common.loadOf
import app.billfolder.libris.ui.common.valueOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

data class WishlistState(
    val books: Load<List<Book>> = Load.Loading,
    val refreshing: Boolean = false,
    val genres: List<Genre> = emptyList(),
    val genreId: String? = null,
    val format: BookFormat? = null,
    val language: String? = null,
    val query: String = "",
) {
    val subtitle get() = books.valueOrNull?.let { "${it.size} ${if (it.size == 1) "livro" else "livros"} pra procurar" }

    /** The wishlist is short, so it is filtered in memory. */
    val visible: List<Book>
        get() {
            val term = fold(query.trim())
            return books.valueOrNull.orEmpty().filter { b ->
                (genreId == null || b.genres.any { it.id == genreId }) &&
                    (format == null || format in b.formats) &&
                    (language == null || b.language == language) &&
                    (term.isEmpty() || fold(b.title).contains(term) || b.authors.any { fold(it).contains(term) })
            }
        }
}

class WishlistViewModel(private val books: BookRepository, private val genres: GenreRepository) : ViewModel() {
    private val _state = MutableStateFlow(WishlistState())
    val state: StateFlow<WishlistState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun setGenre(id: String?) = _state.update { it.copy(genreId = id) }

    fun setFormat(format: BookFormat?) = _state.update { it.copy(format = format) }

    fun setLanguage(language: String?) = _state.update { it.copy(language = language) }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun refresh(): List<Job> = listOf(
        viewModelScope.launch {
            runCatching { genres.list() }.onSuccess { g -> _state.update { it.copy(genres = g) } }
        },
        viewModelScope.launch {
            val load = loadOf { books.list(BookFilter(ownership = BookOwnership.Wishlist)) }
            _state.update { it.copy(books = load) }
        },
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
}
