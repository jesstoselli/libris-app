package app.billfolder.libris.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
    LibrisButton(
        if (activeCount == 0) "Filtros" else "Filtros · $activeCount",
        onClick,
        modifier,
        variant = if (activeCount == 0) ButtonVariant.Secondary else ButtonVariant.Primary,
        onDesk = true,
    )

/** The filters live in a sheet on the phone: every option fits, nothing is cut at the screen edge. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(onDismiss: () -> Unit, onClear: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    val type = LocalLibrisType.current
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Libris.paper,
        contentColor = Libris.ink,
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Filtros", fontFamily = type.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Libris.oxblood)
            content()
            Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                LibrisButton("Limpar", onClear, variant = ButtonVariant.Secondary)
                LibrisButton("Ver livros", onDismiss, Modifier.weight(1f))
            }
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
