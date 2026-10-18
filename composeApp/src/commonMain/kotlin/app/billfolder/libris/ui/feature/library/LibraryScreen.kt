package app.billfolder.libris.ui.feature.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.minimumInteractiveComponentSize
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
import app.billfolder.libris.domain.model.BookFormat
import app.billfolder.libris.domain.model.BookStatus
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.components.BookCard
import app.billfolder.libris.ui.components.ButtonVariant
import app.billfolder.libris.ui.components.DeskSearchField
import app.billfolder.libris.ui.components.FilterSection
import app.billfolder.libris.ui.components.FiltersButton
import app.billfolder.libris.ui.components.FiltersSheet
import app.billfolder.libris.ui.components.LibrisButton
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.PaperCard
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.STATUS_LABELS
import app.billfolder.libris.ui.components.SessionAction
import app.billfolder.libris.ui.components.Stamp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    onOpenBook: (String) -> Unit,
    onRegister: () -> Unit,
    onManageGenres: () -> Unit,
    onLogout: () -> Unit,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    var firstResume by remember { mutableStateOf(true) }
    var showFilters by remember { mutableStateOf(false) }
    // Coming back from a book detail (status, logs, deletion) should show fresh data.
    LifecycleResumeEffect(Unit) {
        if (firstResume) firstResume = false else viewModel.refresh()
        onPauseOrDispose { }
    }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeading("Acervo de leituras", state.subtitle, action = { SessionAction(onLogout) }) }
            // Search leads; status, genre and format live in the Filtros sheet.
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    DeskSearchField(state.query, viewModel::setQuery, "Buscar por título ou autor", Modifier.weight(1f))
                    FiltersButton(listOfNotNull(state.status, state.genreId, state.format).size, { showFilters = true })
                }
            }
            when (val shown = state.shown) {
                Load.Loading -> item { Text("Carregando…", fontFamily = type.body, color = Libris.deskTextSoft) }
                is Load.Failed -> item { Text(shown.message, fontFamily = type.body, color = Libris.deskText) }
                is Load.Loaded -> if (shown.value.isEmpty()) {
                    item {
                        PaperCard(Modifier.fillMaxWidth(), foldedCorner = true) {
                            Column {
                                Text(if (state.hasFilters) "Nenhum livro com esses filtros." else "Nenhum livro por aqui ainda.", fontFamily = type.body)
                                if (!state.hasFilters) {
                                    Text("Registrar o primeiro livro", fontFamily = type.body, color = Libris.oxblood, modifier = Modifier.clickable(onClick = onRegister).minimumInteractiveComponentSize().padding(top = 6.dp))
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
    if (showFilters) {
        FiltersSheet(
            onDismiss = { showFilters = false },
            onClear = { viewModel.setStatus(null); viewModel.setGenre(null); viewModel.setFormat(null) },
        ) {
            FilterSection("Situação", BookStatus.entries.map { it to STATUS_LABELS.getValue(it) }, state.status, viewModel::setStatus, allLabel = "Todas")
            FilterSection("Formato", listOf(BookFormat.Physical to "Físico", BookFormat.Digital to "Digital"), state.format, viewModel::setFormat)
            FilterSection("Gênero", state.genres.map { it.id to it.name }, state.genreId, viewModel::setGenre)
            LibrisButton("Gerenciar gêneros", { showFilters = false; onManageGenres() }, variant = ButtonVariant.Quiet)
        }
    }
}
