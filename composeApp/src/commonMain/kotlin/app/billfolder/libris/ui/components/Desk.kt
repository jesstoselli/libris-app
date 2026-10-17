package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import app.billfolder.libris.ui.generated.resources.Res
import app.billfolder.libris.ui.generated.resources.ledger_bg
import app.billfolder.libris.ui.theme.Libris
import org.jetbrains.compose.resources.imageResource

/** The library desk: ledger texture tiled over the desk colour, under a dark veil so what sits on it stays legible. */
@Composable
fun Desk(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val texture = imageResource(Res.drawable.ledger_bg)
    val brush = remember(texture) { ShaderBrush(ImageShader(texture, TileMode.Repeated, TileMode.Repeated)) }
    Box(
        modifier
            .fillMaxSize()
            .background(Libris.desk)
            .drawBehind {
                drawRect(brush, alpha = 0.55f)
                drawRect(Libris.deskOverlay)
            },
        content = content,
    )
}
