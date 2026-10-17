package app.billfolder.libris.ui.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.Book
import app.billfolder.libris.shared.BookListQuery
import app.billfolder.libris.shared.BookOwnership
import app.billfolder.libris.shared.Genre
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.userMessage
import app.billfolder.libris.ui.Load
import app.billfolder.libris.ui.fold
import app.billfolder.libris.ui.valueOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WishlistState(
    val books: Load<List<Book>> = Load.Loading,
    val genres: List<Genre> = emptyList(),
    val genreId: String? = null,
    val query: String = "",
) {
    val subtitle get() = books.valueOrNull?.let { "${it.size} ${if (it.size == 1) "livro" else "livros"} pra procurar" }

    /** The wishlist is short, so it is filtered in memory. */
    val visible: List<Book>
        get() {
            val term = fold(query.trim())
            return books.valueOrNull.orEmpty().filter { b ->
                (genreId == null || b.genres.any { it.id == genreId }) &&
                    (term.isEmpty() || fold(b.title).contains(term) || b.authors.any { fold(it).contains(term) })
            }
        }
}

class WishlistViewModel(private val session: AuthSession, private val api: LibrisApi) : ViewModel() {
    private val _state = MutableStateFlow(WishlistState())
    val state: StateFlow<WishlistState> = _state.asStateFlow()

    init {
        refresh()
    }

    fun coverSrc(book: Book) = api.coverSrc(book.coverUrl)

    fun setGenre(id: String?) = _state.update { it.copy(genreId = id) }

    fun setQuery(q: String) = _state.update { it.copy(query = q) }

    fun refresh() {
        viewModelScope.launch {
            runCatching { session.authorized { api.listGenres(it) } }.onSuccess { g -> _state.update { it.copy(genres = g) } }
        }
        viewModelScope.launch {
            val load = try {
                Load.Loaded(session.authorized { api.listBooks(it, BookListQuery(ownership = BookOwnership.Wishlist)) })
            } catch (e: Exception) {
                Load.Failed(e.userMessage())
            }
            _state.update { it.copy(books = load) }
        }
    }
}
