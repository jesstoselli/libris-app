package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

data class RibbonItem(val label: String, val icon: ImageVector, val selected: Boolean, val onClick: () -> Unit)

/** The fabric ribbon of the Mac app, laid along the bottom edge on the phone. */
@Composable
fun RibbonBar(items: List<RibbonItem>, modifier: Modifier = Modifier) {
    val type = LocalLibrisType.current
    Row(
        modifier
            .fillMaxWidth()
            .background(Libris.paper)
            .drawBehind { drawLine(Libris.paperShade, Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 6f) }
            .navigationBarsPadding()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEach { item ->
            val tint = if (item.selected) Libris.oxblood else Libris.inkSoft
            Column(
                Modifier
                    .weight(1f)
                    .semantics { selected = item.selected }
                    .clickable(role = Role.Tab, onClick = item.onClick)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(rememberVectorPainter(item.icon), contentDescription = null, modifier = Modifier.size(24.dp), colorFilter = ColorFilter.tint(tint))
                Text(
                    item.label.uppercase(),
                    fontFamily = type.body,
                    fontSize = 10.sp,
                    letterSpacing = 0.4.sp,
                    color = if (item.selected) tint else tint.copy(alpha = 0.8f),
                )
            }
        }
    }
}

internal val Transparent = Color(0x00000000)
