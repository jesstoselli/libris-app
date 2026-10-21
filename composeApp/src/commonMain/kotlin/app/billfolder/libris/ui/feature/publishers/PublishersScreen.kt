package app.billfolder.libris.ui.feature.publishers

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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.domain.model.Publisher
import app.billfolder.libris.ui.common.Load
import app.billfolder.libris.ui.components.ButtonVariant
import app.billfolder.libris.ui.components.LibrisButton
import app.billfolder.libris.ui.components.LibrisIconButton
import app.billfolder.libris.ui.components.LocalSnackbar
import app.billfolder.libris.ui.components.PageHeading
import app.billfolder.libris.ui.components.PaperCard
import app.billfolder.libris.ui.components.Refreshable
import app.billfolder.libris.ui.components.RuledField
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

/** [onOpen] gets the spelling and whether it lives only on the wishlist, to open the right list filtered by it. */
@Composable
fun PublishersScreen(onBack: () -> Unit, onOpen: (publisher: String, wishlist: Boolean) -> Unit, viewModel: PublishersViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current
    val snackbar = LocalSnackbar.current
    var renaming by remember { mutableStateOf<Publisher?>(null) }
    LaunchedEffect(viewModel) { viewModel.messages.collect { snackbar.showSnackbar(it) } }
    LaunchedEffect(viewModel) { viewModel.renamed.collect { renaming = null } }

    Refreshable(state.refreshing, viewModel::pullToRefresh) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            LibrisButton("‹ Voltar", onBack, variant = ButtonVariant.Quiet, onDesk = true)
            PageHeading("Editoras")
            PaperCard(Modifier.fillMaxWidth()) {
                when (val load = state.publishers) {
                    Load.Loading -> Text("Carregando…", fontFamily = type.body)
                    is Load.Failed -> Text(load.message, fontFamily = type.body)
                    is Load.Loaded -> if (load.value.isEmpty()) {
                        Text("Nenhuma editora ainda. Elas aparecem quando você cadastra livros.", fontFamily = type.body, color = Libris.inkSoft)
                    } else {
                        Column {
                            Text(
                                "Renomeie uma grafia para outra que já existe e as duas viram uma só.",
                                fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft,
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                            load.value.forEachIndexed { index, p ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(
                                        Modifier
                                            .weight(1f)
                                            .clickable(role = Role.Button) { onOpen(p.name, p.onlyOnWishlist) }
                                            .minimumInteractiveComponentSize(),
                                    ) {
                                        Text(p.name, fontFamily = type.body, color = Libris.oxblood, textDecoration = TextDecoration.Underline)
                                        Text(p.countLabel(), fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
                                    }
                                    LibrisIconButton("✎", "Renomear ${p.name}", { viewModel.clearRenameError(); renaming = p })
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

    renaming?.let { p ->
        var name by remember(p) { mutableStateOf(p.name) }
        AlertDialog(
            onDismissRequest = { renaming = null },
            containerColor = Libris.paper,
            title = { Text("Renomear editora", fontFamily = type.title, color = Libris.oxblood) },
            text = { RuledField("Nome", name, { name = it }, error = state.renameError) },
            confirmButton = { LibrisButton("Salvar", { viewModel.rename(p, name) }) },
            dismissButton = { LibrisButton("Cancelar", { renaming = null }, variant = ButtonVariant.Secondary) },
        )
    }
}
