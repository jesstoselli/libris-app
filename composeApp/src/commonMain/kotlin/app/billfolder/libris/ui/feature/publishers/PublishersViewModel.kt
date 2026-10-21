package app.billfolder.libris.ui.feature.publishers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.Publisher
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.loadOf
import app.billfolder.libris.ui.common.userMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

data class PublishersState(val publishers: Load<List<Publisher>> = Load.Loading, val renameError: String? = null, val refreshing: Boolean = false)

/** Each spelling of a publisher with its books; renaming one onto another merges them, which is how they get normalized. */
class PublishersViewModel(private val books: BookRepository) : ViewModel() {
    private val _state = MutableStateFlow(PublishersState())
    val state: StateFlow<PublishersState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    /** Emits once a rename has been saved, so the dialog can close. */
    private val _renamed = Channel<Unit>(Channel.BUFFERED)
    val renamed = _renamed.receiveAsFlow()

    init {
        load()
    }

    private fun load(): Job = viewModelScope.launch {
        val load = loadOf { books.publisherSummary() }
        _state.update { it.copy(publishers = load) }
    }

    fun pullToRefresh() {
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true) }
        val jobs = listOf(load())
        viewModelScope.launch {
            jobs.joinAll()
            _state.update { it.copy(refreshing = false) }
        }
    }

    fun clearRenameError() = _state.update { it.copy(renameError = null) }

    fun rename(publisher: Publisher, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed == publisher.name) return
        viewModelScope.launch {
            try {
                val updated = books.renamePublisher(publisher.name, trimmed)
                _state.update { it.copy(renameError = null) }
                _renamed.send(Unit)
                _messages.send("Editora renomeada em $updated ${if (updated == 1) "livro" else "livros"}.")
                // Merged spellings collapse into one row, so the list comes back from the API.
                load()
            } catch (e: Exception) {
                _state.update { it.copy(renameError = e.userMessage()) }
            }
        }
    }
}

/** A publisher whose books are all on the wishlist opens the Wishlist; any other, the Acervo. */
val Publisher.onlyOnWishlist get() = bookCount == 0 && wishlistCount > 0

fun Publisher.countLabel(): String = listOfNotNull(
    bookCount.takeIf { it > 0 }?.let { "$it ${if (it == 1) "livro" else "livros"}" },
    wishlistCount.takeIf { it > 0 }?.let { "$it na wishlist" },
).joinToString(" · ")
