package app.billfolder.libris.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
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
            .background(Brush.horizontalGradient(listOf(Color(0xFF661715), Libris.oxblood, Color(0xFF641614))))
            .drawBehind {
                drawLine(Libris.brass.copy(alpha = 0.42f), Offset(0f, 0f), Offset(size.width, 0f), strokeWidth = 1.dp.toPx())
                var x = 2.dp.toPx()
                while (x < size.width) {
                    drawLine(Color.White.copy(alpha = 0.012f), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1.dp.toPx())
                    x += 4.dp.toPx()
                }
            }
            .navigationBarsPadding()
            .padding(horizontal = 4.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEach { item ->
            val tint = if (item.selected) Libris.deskText else Libris.paperShade.copy(alpha = 0.7f)
            Column(
                Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp)
                    .semantics { selected = item.selected }
                    .clickable(role = Role.Tab, onClick = item.onClick)
                    .padding(vertical = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(rememberVectorPainter(item.icon), contentDescription = null, modifier = Modifier.size(22.dp), colorFilter = ColorFilter.tint(tint))
                Text(
                    item.label,
                    fontFamily = type.body,
                    fontSize = 10.sp,
                    color = tint,
                )
                Box(
                    Modifier
                        .padding(top = 3.dp)
                        .width(20.dp)
                        .height(2.dp)
                        .background(if (item.selected) Libris.brass else Color.Transparent),
                )
            }
        }
    }
}
