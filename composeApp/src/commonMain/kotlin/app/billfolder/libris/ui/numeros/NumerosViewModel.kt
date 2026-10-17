package app.billfolder.libris.ui.numeros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.MonthDetail
import app.billfolder.libris.shared.YearStats
import app.billfolder.libris.shared.currentYearMonth
import app.billfolder.libris.shared.userMessage
import app.billfolder.libris.ui.Load
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NumerosState(
    val year: Int,
    val month: Int,
    val stats: Load<YearStats> = Load.Loading,
    val detail: Load<MonthDetail> = Load.Loading,
)

/** Current month this year; December for past years; January for future ones. */
fun defaultMonth(year: Int, now: Pair<Int, Int>): Int = when {
    year == now.first -> now.second
    year < now.first -> 12
    else -> 1
}

class NumerosViewModel(
    private val session: AuthSession,
    private val api: LibrisApi,
    private val now: () -> Pair<Int, Int> = { currentYearMonth() },
) : ViewModel() {

    private val _state = MutableStateFlow(now().let { (y, m) -> NumerosState(y, m) })
    val state: StateFlow<NumerosState> = _state.asStateFlow()

    private var yearJob: Job? = null
    private var monthJob: Job? = null

    init {
        loadYear()
        loadMonth()
    }

    fun refresh() {
        loadYear()
        loadMonth()
    }

    fun goToYear(year: Int) {
        _state.update { it.copy(year = year, month = defaultMonth(year, now())) }
        loadYear()
        loadMonth()
    }

    fun selectMonth(month: Int) {
        _state.update { it.copy(month = month) }
        loadMonth()
    }

    private fun loadYear() {
        yearJob?.cancel()
        val year = _state.value.year
        _state.update { it.copy(stats = Load.Loading) }
        yearJob = viewModelScope.launch {
            val load = try {
                Load.Loaded(session.authorized { api.yearStats(it, year) })
            } catch (e: Exception) {
                Load.Failed(e.userMessage())
            }
            _state.update { it.copy(stats = load) }
        }
    }

    private fun loadMonth() {
        monthJob?.cancel()
        val (year, month) = _state.value.let { it.year to it.month }
        _state.update { it.copy(detail = Load.Loading) }
        monthJob = viewModelScope.launch {
            val load = try {
                Load.Loaded(session.authorized { api.monthStats(it, year, month) })
            } catch (e: Exception) {
                Load.Failed(e.userMessage())
            }
            _state.update { it.copy(detail = load) }
        }
    }
}
