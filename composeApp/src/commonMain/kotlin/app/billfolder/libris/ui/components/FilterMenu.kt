package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

@Composable
/** "Título: valor ▾" over the desk, with "Todos" first; used for the genre and format filters. */
fun <T> FilterMenu(title: String, options: List<Pair<T, String>>, selected: T?, onSelect: (T?) -> Unit) {
    val type = LocalLibrisType.current
    var open by remember { mutableStateOf(false) }
    val label = options.firstOrNull { it.first == selected }?.second?.lowercase() ?: "todos"
    Box {
        Text(
            "$title: $label ▾",
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
            options.forEach { (id, name) ->
                DropdownMenuItem(text = { Text(name, fontFamily = type.body) }, onClick = { onSelect(id); open = false })
            }
        }
    }
}
