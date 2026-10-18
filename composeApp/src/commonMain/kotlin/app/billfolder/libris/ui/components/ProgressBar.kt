package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.theme.Libris

@Composable
fun ProgressBar(percent: Int, modifier: Modifier = Modifier, height: Dp = 4.dp) {
    val fraction = percent.coerceIn(0, 100) / 100f
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(Libris.inkSoft.copy(alpha = 0.2f))
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f) },
    ) {
        Box(Modifier.fillMaxHeight().fillMaxWidth(fraction).background(Libris.oxblood))
    }
}
