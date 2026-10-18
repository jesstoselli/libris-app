package app.billfolder.libris.ui.feature.genres

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.data.remote.dto.Genre
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.components.ConfirmDialog
import app.billfolder.libris.ui.components.Ficha
import app.billfolder.libris.ui.components.LocalSnackbar
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.RuledField
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GenresScreen(onBack: () -> Unit, viewModel: GenresViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    val snackbar = LocalSnackbar.current
    var renaming by remember { mutableStateOf<Genre?>(null) }
    var deleting by remember { mutableStateOf<Genre?>(null) }
    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(viewModel) { viewModel.renamed.collect { renaming = null } }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("‹ Voltar", fontFamily = type.body, color = Libris.deskText, modifier = Modifier.clickable(onClick = onBack))
            PageHeading("Gêneros")
            Ficha(Modifier.fillMaxWidth()) {
                when (val load = state.genres) {
                    Load.Loading -> Text("Carregando…", fontFamily = type.body)
                    is Load.Failed -> Text(load.message, fontFamily = type.body)
                    is Load.Loaded -> if (load.value.isEmpty()) {
                        Text("Nenhum gênero ainda. Eles aparecem quando você cadastra livros.", fontFamily = type.body, color = Libris.inkSoft)
                    } else {
                        Column {
                            load.value.forEachIndexed { index, g ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(g.name, fontFamily = type.body, modifier = Modifier.weight(1f))
                                    Text("${g.bookCount} ${if (g.bookCount == 1) "livro" else "livros"}", fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
                                    Text("✎", color = Libris.inkSoft, modifier = Modifier.clickable { viewModel.clearRenameError(); renaming = g }.padding(10.dp))
                                    Text("×", color = Libris.inkSoft, modifier = Modifier.clickable { deleting = g }.padding(10.dp))
                                }
                                if (index < load.value.lastIndex) {
                                    HorizontalDivider(color = Libris.paperShade.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    renaming?.let { g ->
        var name by remember(g) { mutableStateOf(g.name) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            containerColor = Libris.paper,
            title = { Text("Renomear gênero", fontFamily = type.title, color = Libris.oxblood) },
            text = { RuledField("Nome", name, { name = it }, error = state.renameError) },
            confirmButton = { TextButton(onClick = { viewModel.rename(g, name) }) { Text("Salvar", fontFamily = type.title, color = Libris.oxblood) } },
            dismissButton = { TextButton(onClick = { renaming = null }) { Text("Cancelar", color = Libris.inkSoft) } },
        )
    }
    deleting?.let { g ->
        ConfirmDialog("Apagar o gênero ${g.name}? Os livros continuam no acervo.", "Apagar", onConfirm = { viewModel.delete(g); deleting = null }, onDismiss = { deleting = null })
    }
}
