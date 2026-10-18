package app.billfolder.libris.ui.feature.genres

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.Genre
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.loadOf
import app.billfolder.libris.ui.common.map
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

data class GenresState(val genres: Load<List<Genre>> = Load.Loading, val renameError: String? = null, val refreshing: Boolean = false)

class GenresViewModel(private val genres: GenreRepository) : ViewModel() {
    private val _state = MutableStateFlow(GenresState())
    val state: StateFlow<GenresState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages = _messages.receiveAsFlow()

    /** Emits once a rename has been saved, so the dialog can close. */
    private val _renamed = Channel<Unit>(Channel.BUFFERED)
    val renamed = _renamed.receiveAsFlow()

    init {
        load()
    }

    private fun load(): Job = viewModelScope.launch {
        val load = loadOf { genres.list() }
        _state.update { it.copy(genres = load) }
    }

    /** The pull-down gesture: same reload, with the indicator up until every request has answered. */
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

    fun rename(genre: Genre, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty() || trimmed == genre.name) return
        viewModelScope.launch {
            try {
                val renamed = genres.rename(genre.id, trimmed)
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
                genres.delete(genre.id)
                _state.update { s -> s.copy(genres = s.genres.map { list -> list.filterNot { it.id == genre.id } }) }
                _messages.send("Gênero apagado.")
            } catch (e: Exception) {
                _messages.send(e.userMessage())
            }
        }
    }
}
