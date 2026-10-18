package app.billfolder.libris.ui.feature.wishlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.components.BookCard
import app.billfolder.libris.ui.components.DeskSearchField
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.PaperCard
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.SessionAction
import app.billfolder.libris.ui.feature.library.GenreMenu
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WishlistScreen(
    onOpenBook: (String) -> Unit,
    onRegister: () -> Unit,
    onLogout: () -> Unit,
    viewModel: WishlistViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    var firstResume by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (firstResume) firstResume = false else viewModel.refresh()
        onPauseOrDispose { }
    }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeading("Wishlist", state.subtitle, action = { SessionAction(onLogout) }) }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GenreMenu(state.genres.map { it.id to it.name }, state.genreId, viewModel::setGenre)
                    DeskSearchField(state.query, viewModel::setQuery, "⌕ título ou autor…", Modifier.fillMaxWidth())
                }
            }
            when (val load = state.books) {
                Load.Loading -> item { Text("Carregando…", fontFamily = type.body, color = Libris.deskTextSoft) }
                is Load.Failed -> item { Text(load.message, fontFamily = type.body, color = Libris.deskText) }
                is Load.Loaded -> when {
                    load.value.isEmpty() -> item {
                        PaperCard(Modifier.fillMaxWidth(), foldedCorner = true) {
                            Column {
                                Text("Nenhum livro na wishlist.", fontFamily = type.body)
                                Text("Registrar um livro que você quer", fontFamily = type.body, color = Libris.oxblood, modifier = Modifier.clickable(onClick = onRegister).minimumInteractiveComponentSize())
                            }
                        }
                    }
                    state.visible.isEmpty() -> item { PaperCard(Modifier.fillMaxWidth(), foldedCorner = true) { Text("Nenhum livro com esses filtros.", fontFamily = type.body) } }
                    else -> items(state.visible, key = { it.id }) { book -> BookCard(book, onClick = { onOpenBook(book.id) }) }
                }
            }
        }
    }
}
