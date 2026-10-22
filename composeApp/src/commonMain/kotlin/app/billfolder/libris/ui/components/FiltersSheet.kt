package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

/** "Filtros" next to the search; the count says how many are on, so a filtered list never looks like the whole one. */
@Composable
fun FiltersButton(activeCount: Int, onClick: () -> Unit, modifier: Modifier = Modifier) =
    LibrisCountButton("Filtros", activeCount, onClick, modifier, onDesk = true)

/** The filters live in a sheet on the phone: every option fits, nothing is cut at the screen edge. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(onDismiss: () -> Unit, onClear: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val type = LocalLibrisType.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        // Never under the status bar, even when the options fill the screen.
        modifier = Modifier.statusBarsPadding(),
        containerColor = Libris.paper,
        contentColor = Libris.ink,
    ) {
        // Only the options scroll; Limpar and Ver livros stay in reach at the bottom however long the list of genres is.
        Column(
            Modifier.weight(1f, fill = false).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Filtros", fontFamily = type.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Libris.oxblood)
            content()
        }
        Row(
            Modifier.fillMaxWidth().background(Libris.paper).padding(horizontal = 20.dp, vertical = 12.dp).navigationBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            LibrisButton("Limpar", onClear, variant = ButtonVariant.Secondary)
            LibrisButton("Ver livros", onDismiss, Modifier.weight(1f))
        }
    }
}

/** One filter: a section title and its options as choice stamps, "Todos" first. */
@Composable
fun <T> FilterSection(title: String, options: List<Pair<T, String>>, selected: T?, onSelect: (T?) -> Unit, allLabel: String = "Todos") {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontFamily = LocalLibrisType.current.title, fontSize = 16.sp, color = Libris.ink)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Stamp(allLabel, selected == null, { onSelect(null) })
            options.forEach { (value, label) -> Stamp(label, selected == value, { onSelect(value) }) }
        }
    }
}
