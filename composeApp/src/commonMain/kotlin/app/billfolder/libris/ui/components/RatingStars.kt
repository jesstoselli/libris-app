package app.billfolder.libris.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import kotlin.math.roundToInt

/** "4,5" for 4.5 and "4" for 4, as a reader writes a rating. */
fun ratingText(value: Double): String = if (value % 1.0 == 0.0) value.toInt().toString() else value.toString().replace('.', ',')

/**
 * Five stars in halves. Read-only shows the rating; with [onChange] each half of a star is a target (the same value
 * again clears it), and screen readers get it as an adjustable value.
 */
@Composable
fun RatingStars(value: Double?, onChange: ((Double?) -> Unit)? = null, size: TextUnit = 15.sp, modifier: Modifier = Modifier) {
    val description = value?.let { "${ratingText(it)} de 5 estrelas" } ?: "Sem avaliação"
    val semantics = if (onChange == null) {
        Modifier.semantics { contentDescription = description }
    } else {
        Modifier.semantics {
            contentDescription = "Avaliação: $description"
            progressBarRangeInfo = ProgressBarRangeInfo(value?.toFloat() ?: 0f, 0f..5f, steps = 9)
            setProgress { target -> onChange((target * 2).roundToInt().coerceIn(1, 10) / 2.0); true }
        }
    }
    Row(modifier.then(semantics), verticalAlignment = Alignment.CenterVertically) {
        for (star in 1..5) {
            val fill = when {
                (value ?: 0.0) >= star -> 1f
                (value ?: 0.0) >= star - 0.5 -> 0.5f
                else -> 0f
            }
            Box {
                Text("★", fontSize = size, color = Libris.inkSoft.copy(alpha = 0.3f))
                Box(Modifier.matchParentSize().clipToBounds()) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(fill).clipToBounds()) {
                        Text("★", fontSize = size, color = Libris.oxblood, softWrap = false)
                    }
                }
                if (onChange != null) {
                    Row(Modifier.matchParentSize()) {
                        Box(Modifier.weight(1f).fillMaxHeight().clickable { onChange(if (value == star - 0.5) null else star - 0.5) })
                        Box(Modifier.weight(1f).fillMaxHeight().clickable { onChange(if (value == star.toDouble()) null else star.toDouble()) })
                    }
                }
            }
            if (star < 5) Box(Modifier.width(2.dp))
        }
    }
}
