package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.theme.Libris

/** A sheet of paper on the desk with the hard offset shadow of the mock (no blur). */
@Composable
fun Ficha(modifier: Modifier = Modifier, padding: Dp = 18.dp, content: @Composable BoxScope.() -> Unit) {
    Box(modifier) {
        Box(Modifier.matchParentSize().offset(3.dp, 4.dp).background(Color(0x59110D08)))
        Box(Modifier.background(Libris.paper).padding(padding), content = content)
    }
}
