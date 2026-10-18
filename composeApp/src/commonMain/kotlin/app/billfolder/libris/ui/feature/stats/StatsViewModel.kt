package app.billfolder.libris.ui.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.billfolder.libris.domain.model.MonthDetail
import app.billfolder.libris.domain.model.YearStats
import app.billfolder.libris.domain.repository.StatsRepository
import app.billfolder.libris.domain.time.currentYearMonth
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.common.loadOf
import app.billfolder.libris.ui.common.userMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

data class StatsState(
    val year: Int,
    val month: Int,
    val stats: Load<YearStats> = Load.Loading,
    val detail: Load<MonthDetail> = Load.Loading,
    val refreshing: Boolean = false,
)

/** Current month this year; December for past years; January for future ones. */
fun defaultMonth(year: Int, now: Pair<Int, Int>): Int = when {
    year == now.first -> now.second
    year < now.first -> 12
    else -> 1
}

class StatsViewModel(
    private val stats: StatsRepository,
    private val now: () -> Pair<Int, Int> = { currentYearMonth() },
) : ViewModel() {

    private val _state = MutableStateFlow(now().let { (y, m) -> StatsState(y, m) })
    val state: StateFlow<StatsState> = _state.asStateFlow()

    private var yearJob: Job? = null
    private var monthJob: Job? = null

    init {
        loadYear()
        loadMonth()
    }

    fun refresh(): List<Job> = listOf(loadYear(), loadMonth())

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

    fun goToYear(year: Int) {
        _state.update { it.copy(year = year, month = defaultMonth(year, now())) }
        loadYear()
        loadMonth()
    }

    fun selectMonth(month: Int) {
        _state.update { it.copy(month = month) }
        loadMonth()
    }

    private fun loadYear(): Job {
        yearJob?.cancel()
        val year = _state.value.year
        _state.update { it.copy(stats = Load.Loading) }
        yearJob = viewModelScope.launch {
            val load = loadOf { stats.year(year) }
            _state.update { it.copy(stats = load) }
        }
        return yearJob!!
    }

    private fun loadMonth(): Job {
        monthJob?.cancel()
        val (year, month) = _state.value.let { it.year to it.month }
        _state.update { it.copy(detail = Load.Loading) }
        monthJob = viewModelScope.launch {
            val load = loadOf { stats.month(year, month) }
            _state.update { it.copy(detail = load) }
        }
        return monthJob!!
    }
}
