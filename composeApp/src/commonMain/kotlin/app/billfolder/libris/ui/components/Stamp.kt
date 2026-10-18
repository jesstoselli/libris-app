package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.domain.model.BookStatus
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
    tone: Color = Libris.inkSoft,
) {
    val type = LocalLibrisType.current
    val shape = RoundedCornerShape(2.dp)
    val visual =
        Modifier
            .heightIn(min = ControlHeight)
            .background(if (active) Libris.oxblood else Libris.paper.copy(alpha = 0.92f), shape)
            .border(1.5.dp, if (active) Libris.oxblood else tone, shape)
            .padding(horizontal = 14.dp)
            .wrapContentHeight(Alignment.CenterVertically)
    // The 48 dp box takes the touch; the stamp is drawn at the shared control height.
    Box(
        modifier
            .heightIn(min = 48.dp)
            .semantics { selected = active }
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            // A control, so it speaks Yantramanav in sentence case.
            label,
            fontFamily = type.body,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            color = if (active) Libris.paper else tone,
            modifier = visual,
        )
    }
}
