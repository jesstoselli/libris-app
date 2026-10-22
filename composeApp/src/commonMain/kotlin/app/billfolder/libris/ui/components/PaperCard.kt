package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.theme.Libris

private val SHADOW = Color(0x59110D08)

/** A sheet of aged paper on the desk; the ribbon and the paper are the signature, so it carries no other ornament. */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .drawBehind { drawRect(SHADOW, topLeft = Offset(3.dp.toPx(), 4.dp.toPx()), size = size) }
            .background(Brush.linearGradient(listOf(Libris.paper, Color(0xFFEAD5AD)))),
    ) {
        Box(Modifier.padding(padding), content = content)
    }
}
