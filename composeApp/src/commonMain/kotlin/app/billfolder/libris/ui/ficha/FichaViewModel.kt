package app.billfolder.libris.ui.ficha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.shared.ApiException
import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.Book
import app.billfolder.libris.shared.BookOwnership
import app.billfolder.libris.shared.BookStatus
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.ReadingLog
import app.billfolder.libris.shared.ReadingLogResult
import app.billfolder.libris.shared.UpdateBookRequest
import app.billfolder.libris.shared.todayIso
import app.billfolder.libris.shared.userMessage
import app.billfolder.libris.ui.Load
import app.billfolder.libris.ui.valueOrNull
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FichaState(
    val book: Load<Book> = Load.Loading,
    val notFound: Boolean = false,
    val logs: List<ReadingLog> = emptyList(),
    val pageInput: String = "",
    val date: String = todayIso(),
    val logError: String? = null,
    val pending: Boolean = false,
    val deleted: Boolean = false,
) {
    val pageProblem: String?
        get() = when {
            pageInput.isBlank() -> null
            pageInput.toIntOrNull() == null -> "Use um número inteiro."
            else -> null
        }
    val canLog get() = pageInput.toIntOrNull() != null && !pending
}

fun Book.toUpdate(startedAt: String? = this.startedAt, finishedAt: String? = this.finishedAt) = UpdateBookRequest(
    title, authors, isbn, totalPages, publisher, publishedYear, synopsis, coverId, genres.map { it.name }, startedAt, finishedAt,
)

/** Checks the "editar datas" pair the same way the book form does. */
fun datesProblem(startedAt: String?, finishedAt: String?, today: String = todayIso()): String? = when {
    startedAt != null && finishedAt != null && finishedAt < startedAt -> "O término não pode ser antes do início."
    (startedAt != null && startedAt > today) || (finishedAt != null && finishedAt > today) -> "As datas não podem estar no futuro."
    else -> null
}

class FichaViewModel(private val bookId: String, private val session: AuthSession, private val api: LibrisApi) : ViewModel() {

    private val _state = MutableStateFlow(FichaState())
    val state: StateFlow<FichaState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** One-off confirmations and errors for the snackbar. */
    val messages = _messages.receiveAsFlow()

    init {
        viewModelScope.launch {
            try {
                val book = session.authorized { api.getBook(it, bookId) }
                _state.update { it.copy(book = Load.Loaded(book)) }
                if (book.ownership == BookOwnership.Owned) loadLogs()
            } catch (e: ApiException) {
                if (e.status == 404) _state.update { it.copy(notFound = true) } else _state.update { it.copy(book = Load.Failed(e.userMessage())) }
            } catch (e: Exception) {
                _state.update { it.copy(book = Load.Failed(e.userMessage())) }
            }
        }
    }

    fun coverSrc(book: Book) = api.coverSrc(book.coverUrl)

    fun onPage(value: String) = _state.update { it.copy(pageInput = value.filter { c -> c.isDigit() || c == '.' || c == ',' }, logError = null) }

    fun onDate(value: String) = _state.update { it.copy(date = value) }

    fun changeStatus(status: BookStatus) {
        val book = _state.value.book.valueOrNull ?: return
        if (book.status == status) return
        run("Situação atualizada.") { token ->
            val updated = api.changeStatus(token, book.id, status, todayIso())
            _state.update { it.copy(book = Load.Loaded(updated)) }
        }
    }

    fun saveDates(startedAt: String?, finishedAt: String?) {
        val book = _state.value.book.valueOrNull ?: return
        datesProblem(startedAt, finishedAt)?.let { problem ->
            viewModelScope.launch { _messages.send(problem) }
            return
        }
        run("Datas atualizadas.") { token ->
            val updated = api.updateBook(token, book.id, book.toUpdate(startedAt, finishedAt))
            _state.update { it.copy(book = Load.Loaded(updated)) }
        }
    }

    fun deleteBook() {
        val book = _state.value.book.valueOrNull ?: return
        run("Livro apagado.") { token ->
            api.deleteBook(token, book.id)
            _state.update { it.copy(deleted = true) }
        }
    }

    /** "Comprei": a wishlist book becomes owned (and gets a reading panel). */
    fun acquire(totalPages: Int?) {
        val book = _state.value.book.valueOrNull ?: return
        run("Livro movido para o acervo.") { token ->
            val updated = api.acquire(token, book.id, todayIso(), totalPages)
            _state.update { it.copy(book = Load.Loaded(updated)) }
            loadLogs()
        }
    }

    fun addLog() {
        val page = _state.value.pageInput.toIntOrNull() ?: return
        if (_state.value.pending) return
        _state.update { it.copy(pending = true, logError = null) }
        viewModelScope.launch {
            try {
                apply(session.authorized { api.createLog(it, bookId, _state.value.date, page) })
                _state.update { it.copy(pageInput = "", pending = false) }
                _messages.send("Leitura anotada.")
            } catch (e: Exception) {
                // Shown under the field and the typed page kept, so a typo is easy to fix.
                _state.update { it.copy(pending = false, logError = e.userMessage()) }
            }
        }
    }

    fun editLog(log: ReadingLog, date: String, page: Int) = run("Registro atualizado.") { token ->
        apply(api.updateLog(token, bookId, log.id, date, page))
    }

    fun deleteLog(log: ReadingLog) = run("Registro apagado.") { token -> apply(api.deleteLog(token, bookId, log.id)) }

    private suspend fun loadLogs() {
        runCatching { session.authorized { api.listLogs(it, bookId) } }.onSuccess { logs -> _state.update { it.copy(logs = logs) } }
    }

    private fun apply(result: ReadingLogResult) = _state.update { it.copy(book = Load.Loaded(result.book), logs = result.logs) }

    private fun run(success: String, block: suspend (String) -> Unit) {
        viewModelScope.launch {
            try {
                session.authorized { block(it) }
                _messages.send(success)
            } catch (e: Exception) {
                _messages.send(e.userMessage())
            }
        }
    }
}
