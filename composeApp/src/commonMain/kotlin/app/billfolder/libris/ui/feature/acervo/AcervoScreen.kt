package app.billfolder.libris.ui.feature.acervo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.components.BookCard
import app.billfolder.libris.ui.components.DeskSearchField
import app.billfolder.libris.ui.components.Ficha
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.STATUS_LABELS
import app.billfolder.libris.ui.components.SessionAction
import app.billfolder.libris.ui.components.Stamp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AcervoScreen(
    onOpenBook: (String) -> Unit,
    onRegister: () -> Unit,
    onManageGenres: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AcervoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    var firstResume by remember { mutableStateOf(true) }
    // Coming back from a Ficha (status, logs, deletion) should show fresh data.
    LifecycleResumeEffect(Unit) {
        if (firstResume) firstResume = false else viewModel.refresh()
        onPauseOrDispose { }
    }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeading("Acervo de leituras", state.subtitle, action = { SessionAction(onLogout) }) }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Stamp("Todos", state.status == null, { viewModel.setStatus(null) }, quiet = true)
                    BookStatus.entries.forEach { s ->
                        Stamp(STATUS_LABELS.getValue(s), state.status == s, { viewModel.setStatus(s) }, quiet = true)
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        GenreMenu(state.genres.map { it.id to it.name }, state.genreId, viewModel::setGenre)
                        Text(
                            "Gerenciar gêneros",
                            fontFamily = type.body,
                            fontSize = 12.sp,
                            color = Libris.deskTextSoft,
                            modifier = Modifier.clickable(onClick = onManageGenres).padding(vertical = 8.dp),
                        )
                    }
                    DeskSearchField(state.query, viewModel::setQuery, "⌕ título ou autor…", Modifier.fillMaxWidth())
                }
            }
            when (val shown = state.shown) {
                Load.Loading -> item { Text("Carregando…", fontFamily = type.body, color = Libris.deskTextSoft) }
                is Load.Failed -> item { Text(shown.message, fontFamily = type.body, color = Libris.deskText) }
                is Load.Loaded -> if (shown.value.isEmpty()) {
                    item {
                        Ficha(Modifier.fillMaxWidth(), foldedCorner = true) {
                            Column {
                                Text(if (state.hasFilters) "Nenhum livro com esses filtros." else "Nenhum livro por aqui ainda.", fontFamily = type.body)
                                if (!state.hasFilters) {
                                    Text("Registrar o primeiro livro", fontFamily = type.body, color = Libris.oxblood, modifier = Modifier.clickable(onClick = onRegister).padding(top = 6.dp))
                                }
                            }
                        }
                    }
                } else {
                    items(shown.value, key = { it.id }) { book ->
                        BookCard(book, onClick = { onOpenBook(book.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun GenreMenu(genres: List<Pair<String, String>>, selected: String?, onSelect: (String?) -> Unit) {
    val type = LocalLibrisType.current
    var open by remember { mutableStateOf(false) }
    val label = genres.firstOrNull { it.first == selected }?.second ?: "todos"
    Box {
        Text(
            "Gênero: $label ▾",
            fontFamily = type.body,
            fontSize = 14.sp,
            color = Libris.deskText,
            modifier = Modifier
                .heightIn(min = 44.dp)
                .background(Color(0x61171810))
                .border(1.dp, Libris.deskTextSoft.copy(alpha = 0.24f), RectangleShape)
                .clickable { open = true }
                .padding(horizontal = 10.dp, vertical = 10.dp),
        )
        DropdownMenu(open, onDismissRequest = { open = false }, containerColor = Libris.paper) {
            DropdownMenuItem(text = { Text("Todos", fontFamily = type.body) }, onClick = { onSelect(null); open = false })
            genres.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name, fontFamily = type.body) }, onClick = { onSelect(id); open = false })
            }
        }
    }
}
