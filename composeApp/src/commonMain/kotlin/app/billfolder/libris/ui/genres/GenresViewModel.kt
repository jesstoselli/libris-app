package app.billfolder.libris.ui.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.Genre
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.userMessage
import app.billfolder.libris.ui.Load
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GenresState(val genres: Load<List<Genre>> = Load.Loading, val renameError: String? = null)

class GenresViewModel(private val session: AuthSession, private val api: LibrisApi) : ViewModel() {
    private val _state = MutableStateFlow(GenresState())
    val state: StateFlow<GenresState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    /** Emits once a rename has been saved, so the dialog can close. */
    private val _renamed = Channel<Unit>(Channel.BUFFERED)
    val renamed = _renamed.receiveAsFlow()

    init {
        viewModelScope.launch {
            val load = try {
                Load.Loaded(session.authorized { api.listGenres(it) })
            } catch (e: Exception) {
                Load.Failed(e.userMessage())
            }
            _state.update { it.copy(genres = load) }
        }
    }

    fun clearRenameError() = _state.update { it.copy(renameError = null) }

    fun rename(genre: Genre, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed == genre.name) return
        viewModelScope.launch {
            try {
                val renamed = session.authorized { api.renameGenre(it, genre.id, trimmed) }
                _state.update { s -> s.copy(genres = s.genres.map { list -> list.map { if (it.id == genre.id) renamed else it } }, renameError = null) }
                _renamed.send(Unit)
                _messages.send("Gênero renomeado.")
            } catch (e: Exception) {
                _state.update { it.copy(renameError = e.userMessage()) }
            }
        }
    }

    fun delete(genre: Genre) {
        viewModelScope.launch {
            try {
                session.authorized { api.deleteGenre(it, genre.id) }
                _state.update { s -> s.copy(genres = s.genres.map { list -> list.filterNot { it.id == genre.id } }) }
                _messages.send("Gênero apagado.")
            } catch (e: Exception) {
                _messages.send(e.userMessage())
            }
        }
    }
}

private fun <T, R> Load<T>.map(transform: (T) -> R): Load<R> = when (this) {
    is Load.Loaded -> Load.Loaded(transform(value))
    is Load.Failed -> this
    Load.Loading -> Load.Loading
}
