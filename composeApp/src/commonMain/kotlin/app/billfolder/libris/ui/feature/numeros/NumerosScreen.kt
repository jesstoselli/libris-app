package app.billfolder.libris.ui.feature.numeros

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.domain.model.MonthStats
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.components.Ficha
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.SessionAction
import app.billfolder.libris.ui.format.formatShortDate
import app.billfolder.libris.ui.format.monthName
import app.billfolder.libris.ui.format.monthShort
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

/** "5842" → "5.842", the pt-BR way. */
fun thousands(n: Int): String = n.toString().reversed().chunked(3).joinToString(".").reversed()

@Composable
fun NumerosScreen(
    onLogout: () -> Unit,
    viewModel: NumerosViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    var firstResume by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (firstResume) firstResume = false else viewModel.refresh()
        onPauseOrDispose { }
    }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                PageHeading("Números de ${state.year}", action = { SessionAction(onLogout) })
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("‹ ${state.year - 1}", fontFamily = type.body, color = Libris.deskTextSoft, modifier = Modifier.clickable { viewModel.goToYear(state.year - 1) }.padding(vertical = 6.dp))
                    Text("${state.year + 1} ›", fontFamily = type.body, color = Libris.deskTextSoft, modifier = Modifier.clickable { viewModel.goToYear(state.year + 1) }.padding(vertical = 6.dp))
                }
            }
            when (val load = state.stats) {
                Load.Loading -> Text("Carregando…", fontFamily = type.body, color = Libris.deskTextSoft)
                is Load.Failed -> Text(load.message, fontFamily = type.body, color = Libris.deskText)
                is Load.Loaded -> {
                    val s = load.value
                    Ficha(Modifier.fillMaxWidth(), padding = 20.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                Kpi(thousands(s.booksFinished), "terminados no ano", Modifier.weight(1f))
                                Kpi(thousands(s.pagesRead), "páginas no ano", Modifier.weight(1f))
                                Kpi(thousands(s.readingNow), "em leitura agora", Modifier.weight(1f))
                            }
                            Text("Páginas lidas por mês · toque num mês", fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
                            MonthBars(s.months, state.month, viewModel::selectMonth)
                        }
                    }
                }
            }
            when (val d = state.detail) {
                Load.Loading -> Unit
                is Load.Failed -> Text(d.message, fontFamily = type.body, color = Libris.deskText)
                is Load.Loaded -> {
                    val m = monthName(state.month)
                    Ficha(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Section("Terminados em $m · ${d.value.finished.size}")
                            if (d.value.finished.isEmpty()) Text("Nenhum livro terminado.", fontFamily = type.body, fontWeight = FontWeight.Light, color = Libris.inkSoft)
                            d.value.finished.forEach { f -> DetailRow(f.title, f.authors.joinToString(", "), formatShortDate(f.finishedAt)) }
                        }
                    }
                    Ficha(Modifier.fillMaxWidth()) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Section("Andaram em $m · ${thousands(d.value.pagesRead)} págs.")
                            if (d.value.progressed.isEmpty()) Text("Nenhuma leitura anotada.", fontFamily = type.body, fontWeight = FontWeight.Light, color = Libris.inkSoft)
                            d.value.progressed.forEach { p -> DetailRow(p.title, null, "+${thousands(p.pagesRead)}") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthBars(months: List<MonthStats>, selected: Int, onSelect: (Int) -> Unit) {
    val type = LocalLibrisType.current
    val max = months.maxOfOrNull { it.pagesRead } ?: 0
    Row(Modifier.fillMaxWidth().height(170.dp).padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        months.forEach { m ->
            val on = m.month == selected
            Column(
                Modifier.weight(1f).fillMaxHeight().clickable { onSelect(m.month) }.semantics { contentDescription = "${monthName(m.month)}: ${m.pagesRead} páginas" },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(if (m.pagesRead > 0) m.pagesRead.toString() else "", fontFamily = type.body, fontSize = 9.sp, color = Libris.inkSoft, maxLines = 1)
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    val fraction = if (max == 0) 0f else m.pagesRead.toFloat() / max
                    Box(Modifier.fillMaxWidth().fillMaxHeight(fraction).background(if (on) Libris.oxblood else Libris.brass))
                }
                Box(Modifier.fillMaxWidth().height(1.5.dp).background(Libris.inkSoft))
                Text(monthShort(m.month), fontFamily = type.body, fontSize = 11.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Normal, color = if (on) Libris.oxblood else Libris.inkSoft, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun Kpi(value: String, label: String, modifier: Modifier) {
    val type = LocalLibrisType.current
    Column(modifier) {
        Text(value, fontFamily = type.title, fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Libris.oxblood)
        Text(label, fontFamily = type.body, fontSize = 10.sp, lineHeight = 12.sp, color = Libris.inkSoft)
    }
}

@Composable
private fun Section(text: String) = Text(text, fontFamily = LocalLibrisType.current.title, fontSize = 14.sp, color = Libris.oxblood)

@Composable
private fun DetailRow(title: String, sub: String?, trailing: String) {
    val type = LocalLibrisType.current
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontFamily = type.body, fontSize = 14.sp)
            sub?.let { Text(it, fontFamily = type.body, fontSize = 12.sp, color = Libris.inkSoft) }
        }
        Text(trailing, fontFamily = type.body, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = Libris.inkSoft)
    }
}
