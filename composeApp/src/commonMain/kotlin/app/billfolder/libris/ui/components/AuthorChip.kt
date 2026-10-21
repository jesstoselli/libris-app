package app.billfolder.libris.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

/** The list is showing one author's books, picked on a book page; the cross shows everything again. */
@Composable
fun AuthorChip(author: String, onClear: () -> Unit, modifier: Modifier = Modifier, label: String = "Autor") {
    val type = LocalLibrisType.current
    Row(
        modifier
            .heightIn(min = ControlHeight)
            .border(1.dp, Libris.brass.copy(alpha = 0.7f), RoundedCornerShape(2.dp))
            .padding(start = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("$label: $author", fontFamily = type.body, fontSize = 15.sp, color = Libris.deskText)
        Box(
            Modifier
                .size(ControlHeight)
                .clickable(role = Role.Button, onClick = onClear)
                .semantics { contentDescription = "Tirar o filtro de ${label.lowercase()}" },
            contentAlignment = Alignment.Center,
        ) {
            Image(
                rememberVectorPainter(LibrisIcons.Clear),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Libris.deskTextSoft),
                modifier = Modifier.size(16.dp),
            )
        }
    }
}
