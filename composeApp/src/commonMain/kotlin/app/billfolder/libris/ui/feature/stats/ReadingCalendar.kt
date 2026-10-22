package app.billfolder.libris.ui.feature.stats

import app.billfolder.libris.ui.format.monthShort
import androidx.compose.foundation.layout.wrapContentSize
import app.billfolder.libris.domain.time.today
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.domain.model.DayStats
import app.billfolder.libris.ui.format.monthName
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus

private val CELL = 11.dp
private val GAP = 3.dp
private val SHAPE = RoundedCornerShape(2.dp)

/** 1 to 4 by the day's pages; fixed steps, so a given day keeps its shade from one year to the next. */
internal fun levelOf(pages: Int): Int = when {
    pages >= 150 -> 4
    pages >= 75 -> 3
    pages >= 25 -> 2
    else -> 1
}

private fun shade(level: Int): Color = when (level) {
    0 -> Libris.inkSoft.copy(alpha = 0.13f)
    1 -> Color(0xFFDCC07E)
    2 -> Libris.brass
    3 -> Color(0xFF9A4A2A)
    else -> Libris.oxblood
}

/** The year in columns of seven days, Sunday on top; blanks pad the first and last weeks. */
internal fun weeksOf(year: Int): List<List<LocalDate?>> {
    val first = LocalDate(year, 1, 1)
    val lead = first.dayOfWeek.let { if (it == DayOfWeek.SUNDAY) 0 else it.ordinal + 1 }
    val cells = MutableList<LocalDate?>(lead) { null }
    var day = first
    while (day.year == year) {
        cells += day
        day = day.plus(DatePeriod(days = 1))
    }
    while (cells.size % 7 != 0) cells += null
    return cells.chunked(7)
}

/** The year as a ledger of days, darker the more pages were read; a reading day opens its books below. */
@Composable
fun ReadingCalendar(year: Int, days: List<DayStats>, selected: DayStats?, onSelect: (DayStats?) -> Unit) {
    val type = LocalLibrisType.current
    val byDate = remember(days) { days.associateBy { it.date } }
    val weeks = remember(year) { weeksOf(year) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val scroll = rememberScrollState()
        // The year is wider than a phone: the current one opens at its end, so the recent months are in view.
        LaunchedEffect(year, scroll.maxValue) {
            if (year == today().year) scroll.scrollTo(scroll.maxValue)
        }
        Row(Modifier.horizontalScroll(scroll), horizontalArrangement = Arrangement.spacedBy(GAP)) {
            weeks.forEach { week ->
                Column(verticalArrangement = Arrangement.spacedBy(GAP)) {
                    // The month's short name over the week its first day falls in; it may run over the next columns.
                    Box(Modifier.size(CELL, 14.dp).wrapContentSize(Alignment.BottomStart, unbounded = true)) {
                        week.firstOrNull { it?.day == 1 }?.let { first ->
                            Text(monthShort(first.month.ordinal + 1), fontFamily = type.body, fontSize = 10.sp, color = Libris.inkSoft, softWrap = false)
                        }
                    }
                    week.forEach { date ->
                        val stats = date?.let { byDate[it] }
                        val base = Modifier.size(CELL)
                        when {
                            date == null -> Box(base)
                            stats == null -> Box(base.background(shade(0), SHAPE))
                            else -> Box(
                                base
                                    .background(shade(levelOf(stats.pagesRead)), SHAPE)
                                    .then(if (selected?.date == date) Modifier.border(1.5.dp, Libris.ink, SHAPE) else Modifier)
                                    .clickable { onSelect(if (selected?.date == date) null else stats) }
                                    .semantics { contentDescription = "${date.day} de ${monthName(date.month.ordinal + 1)}: ${stats.pagesRead} páginas" },
                            )
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("menos", fontFamily = type.body, fontSize = 11.sp, color = Libris.inkSoft)
            (0..4).forEach { Box(Modifier.size(CELL).background(shade(it), SHAPE)) }
            Text("mais páginas", fontFamily = type.body, fontSize = 11.sp, color = Libris.inkSoft)
        }
        when {
            selected != null -> Text(
                "${selected.date.day} de ${monthName(selected.date.month.ordinal + 1)} · ${selected.pagesRead} págs. " +
                    selected.books.joinToString(", ") { "${it.title} +${it.pagesRead}" },
                fontFamily = type.body, fontSize = 14.sp, fontWeight = FontWeight.Normal, color = Libris.ink,
            )
            days.isEmpty() -> Text("Nenhuma leitura anotada neste ano.", fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
            else -> Text("Toque num dia marcado pra ver o que você leu.", fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
        }
    }
}
