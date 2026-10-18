package app.billfolder.libris.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.theme.Libris

private val SHADOW = Color(0x59110D08)

/**
 * A sheet of aged paper on the desk. The fold is optional because long form/detail sheets read better as one
 * continuous page, while collection cards benefit from the physical index-card cue.
 */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    padding: Dp = 18.dp,
    foldedCorner: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .drawBehind { drawRect(SHADOW, topLeft = Offset(3.dp.toPx(), 4.dp.toPx()), size = size) }
            .background(Brush.linearGradient(listOf(Libris.paper, Color(0xFFEAD5AD)))),
    ) {
        Box(Modifier.padding(padding), content = content)
        if (foldedCorner) {
            Canvas(Modifier.size(22.dp).align(Alignment.TopEnd)) {
                val fold = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(fold, Libris.paperShade)
                drawLine(Libris.rule.copy(alpha = 0.7f), Offset(0f, 0f), Offset(size.width, size.height), strokeWidth = 1.dp.toPx())
            }
        }
    }
}
