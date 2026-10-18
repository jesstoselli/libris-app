package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.shared.BookStatus
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

val STATUS_LABELS = mapOf(
    BookStatus.NotStarted to "Não iniciado",
    BookStatus.Reading to "Lendo",
    BookStatus.Read to "Lido",
    BookStatus.Abandoned to "Abandonado",
)

/** Rubber-stamp button: status filters and a book's status. */
@Composable
fun Stamp(
    label: String,
    active: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tilt: Float = -1f,
    tone: Color = Libris.inkSoft,
    quiet: Boolean = false,
) {
    val type = LocalLibrisType.current
    val shape = RoundedCornerShape(3.dp)
    val visual = if (quiet) {
        Modifier
            .background(Color.Transparent)
            .drawBehind {
                if (active) drawLine(Libris.brass, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
            }
            .padding(horizontal = 2.dp, vertical = 4.dp)
    } else {
        Modifier
            .rotate(tilt)
            .background(if (active) Libris.oxblood else Libris.paper.copy(alpha = 0.92f), shape)
            .border(2.dp, if (active) Libris.deskText else tone, shape)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    }
    Text(
        label.uppercase(),
        fontFamily = type.title,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        letterSpacing = 0.8.sp,
        color = if (quiet) {
            if (active) Libris.deskText else Libris.deskTextSoft.copy(alpha = 0.78f)
        } else if (active) Libris.paper else tone,
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics { selected = active }
            .clickable(role = Role.Button, onClick = onClick)
            .then(visual),
    )
}
