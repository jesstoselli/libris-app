package app.billfolder.libris.ui.wishlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.ui.Load
import app.billfolder.libris.ui.acervo.GenreMenu
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.BookCard
import app.billfolder.libris.ui.components.Ficha
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WishlistScreen(onOpenBook: (String) -> Unit, onRegister: () -> Unit, viewModel: WishlistViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    var firstResume by remember { mutableStateOf(true) }
    LifecycleResumeEffect(Unit) {
        if (firstResume) firstResume = false else viewModel.refresh()
        onPauseOrDispose { }
    }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { PageHeading("Wishlist", state.subtitle) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GenreMenu(state.genres.map { it.id to it.name }, state.genreId, viewModel::setGenre)
                    Box(Modifier.weight(1f)) {
                        if (state.query.isEmpty()) Text("⌕ título ou autor…", fontFamily = type.body, fontSize = 14.sp, color = Libris.deskTextSoft)
                        BasicTextField(state.query, viewModel::setQuery, singleLine = true, textStyle = TextStyle(fontFamily = type.body, fontSize = 14.sp, color = Libris.deskText), cursorBrush = SolidColor(Libris.deskText), modifier = Modifier.fillMaxWidth())
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(Libris.deskTextSoft.copy(alpha = 0.5f)))
            }
            when (val load = state.books) {
                Load.Loading -> item { Text("Carregando…", fontFamily = type.body, color = Libris.deskTextSoft) }
                is Load.Failed -> item { Text(load.message, fontFamily = type.body, color = Libris.deskText) }
                is Load.Loaded -> when {
                    load.value.isEmpty() -> item {
                        Ficha(Modifier.fillMaxWidth()) {
                            Column {
                                Text("Nenhum livro na wishlist.", fontFamily = type.body)
                                Text("Registrar um livro que você quer", fontFamily = type.body, color = Libris.oxblood, modifier = Modifier.clickable(onClick = onRegister))
                            }
                        }
                    }
                    state.visible.isEmpty() -> item { Ficha(Modifier.fillMaxWidth()) { Text("Nenhum livro com esses filtros.", fontFamily = type.body) } }
                    else -> items(state.visible, key = { it.id }) { book -> BookCard(book, viewModel.coverSrc(book), onClick = { onOpenBook(book.id) }) }
                }
            }
        }
    }
}
