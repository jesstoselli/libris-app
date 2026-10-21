package app.billfolder.libris.ui.feature.bookdetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.error.ApiException
import app.billfolder.libris.domain.model.Book
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.domain.model.ReadingLog
import app.billfolder.libris.domain.model.ReadingProgress
import app.billfolder.libris.domain.model.toDraft
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.ReadingRepository
import app.billfolder.libris.domain.rules.readingDatesProblem
import app.billfolder.libris.domain.time.today
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.message
import app.billfolder.libris.ui.common.userMessage
import app.billfolder.libris.ui.common.valueOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class BookDetailState(
    val book: Load<Book> = Load.Loading,
    val refreshing: Boolean = false,
    val notFound: Boolean = false,
    val logs: List<ReadingLog> = emptyList(),
    val pageInput: String = "",
    val date: LocalDate = today(),
    val logError: String? = null,
    val pending: Boolean = false,
    val deleted: Boolean = false,
    /** "Lido" on a book with no finish yet: today, or no date at all (an old read stays out of Números). */
    val askingFinish: Boolean = false,
) {
    val pageProblem: String?
        get() = when {
            pageInput.isBlank() -> null
            pageInput.toIntOrNull() == null -> "Use um número inteiro."
            else -> null
        }
    val canLog get() = pageInput.toIntOrNull() != null && !pending
}

class BookDetailViewModel(private val bookId: String, private val books: BookRepository, private val reading: ReadingRepository) : ViewModel() {

    private val _state = MutableStateFlow(BookDetailState())
    val state: StateFlow<BookDetailState> = _state.asStateFlow()

    private val _messages = Channel<String>(Channel.BUFFERED)
    /** One-off confirmations and errors for the snackbar. */
    val messages = _messages.receiveAsFlow()

    init {
        reload()
    }

    fun reload(): Job =
        viewModelScope.launch {
            try {
                val book = books.get(bookId)
                _state.update { it.copy(book = Load.Loaded(book)) }
                if (book.ownership == BookOwnership.Owned) loadLogs()
            } catch (e: ApiException) {
                if (e.status == 404) _state.update { it.copy(notFound = true) } else _state.update { it.copy(book = Load.Failed(e.userMessage())) }
            } catch (e: Exception) {
                _state.update { it.copy(book = Load.Failed(e.userMessage())) }
            }
        }

    /** The pull-down gesture: same reload, with the indicator up until every request has answered. */
    fun pullToRefresh() {
        if (_state.value.refreshing) return
        _state.update { it.copy(refreshing = true) }
        val jobs = listOf(reload())
        viewModelScope.launch {
            jobs.joinAll()
            _state.update { it.copy(refreshing = false) }
        }
    }

    fun onPage(value: String) = _state.update { it.copy(pageInput = value.filter { c -> c.isDigit() || c == '.' || c == ',' }, logError = null) }

    fun onDate(value: LocalDate) = _state.update { it.copy(date = value) }

    fun changeStatus(status: BookStatus) {
        val book = _state.value.book.valueOrNull ?: return
        if (book.status == status) return
        if (status == BookStatus.Read && book.finishedAt == null) {
            _state.update { it.copy(askingFinish = true) }
            return
        }
        sendStatus(book, status, withoutDate = false)
    }

    fun finish(withoutDate: Boolean) {
        val book = _state.value.book.valueOrNull ?: return
        _state.update { it.copy(askingFinish = false) }
        sendStatus(book, BookStatus.Read, withoutDate)
    }

    fun cancelFinish() = _state.update { it.copy(askingFinish = false) }

    private fun sendStatus(book: Book, status: BookStatus, withoutDate: Boolean) {
        run("Situação atualizada.") {
            val updated = books.changeStatus(book.id, status, withoutDate)
            _state.update { it.copy(book = Load.Loaded(updated)) }
        }
    }

    fun saveDates(startedAt: LocalDate?, finishedAt: LocalDate?) {
        val book = _state.value.book.valueOrNull ?: return
        readingDatesProblem(startedAt, finishedAt, today())?.message()?.let { problem ->
            viewModelScope.launch { _messages.send(problem) }
            return
        }
        run("Datas atualizadas.") {
            val updated = books.update(book.id, book.toDraft().copy(startedAt = startedAt, finishedAt = finishedAt))
            _state.update { it.copy(book = Load.Loaded(updated)) }
        }
    }

    fun deleteBook() {
        val book = _state.value.book.valueOrNull ?: return
        run("Livro apagado.") {
            books.delete(book.id)
            _state.update { it.copy(deleted = true) }
        }
    }

    /** "Comprei": a wishlist book becomes owned (and gets a reading panel). */
    fun acquire(totalPages: Int?) {
        val book = _state.value.book.valueOrNull ?: return
        run("Livro movido para o acervo.") {
            val updated = books.acquire(book.id, totalPages)
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
                apply(reading.add(bookId, _state.value.date, page))
                _state.update { it.copy(pageInput = "", pending = false) }
                _messages.send("Leitura anotada.")
            } catch (e: Exception) {
                // Shown under the field and the typed page kept, so a typo is easy to fix.
                _state.update { it.copy(pending = false, logError = e.userMessage()) }
            }
        }
    }

    fun editLog(log: ReadingLog, date: LocalDate, page: Int) = run("Registro atualizado.") {
        apply(reading.update(bookId, log.id, date, page))
    }

    fun deleteLog(log: ReadingLog) = run("Registro apagado.") { apply(reading.delete(bookId, log.id)) }

    private suspend fun loadLogs() {
        runCatching { reading.logs(bookId) }.onSuccess { logs -> _state.update { it.copy(logs = logs) } }
    }

    private fun apply(result: ReadingProgress) = _state.update { it.copy(book = Load.Loaded(result.book), logs = result.logs) }

    private fun run(success: String, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                block()
                _messages.send(success)
            } catch (e: Exception) {
                _messages.send(e.userMessage())
            }
        }
    }
}
