package app.billfolder.libris.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.LocalLibrisType
import coil3.compose.SubcomposeAsyncImage

private val PLACEHOLDER = listOf(0xFF741B18, 0xFF2B2B33, 0xFF4D5A3A, 0xFF5A3D2B, 0xFF3B3F5C, 0xFF6B4F1D).map { Color(it) }

/** A book's cover, or a cloth-bound placeholder with the title while there is none (or it fails to load). */
@Composable
fun BookCover(src: String?, title: String, width: Dp = 70.dp, height: Dp = 104.dp, fontSize: TextUnit = 10.sp) {
    val type = LocalLibrisType.current
    val placeholder = @Composable {
        Box(
            Modifier.fillMaxSize().background(PLACEHOLDER[title.sumOf { it.code } % PLACEHOLDER.size]).padding(6.dp),
            contentAlignment = Alignment.BottomStart,
        ) { Text(title, fontFamily = type.title, fontSize = fontSize, lineHeight = fontSize * 1.1f, color = Color(0xFFF5E3C6)) }
    }
    Box(Modifier.size(width, height).shadow(2.dp)) {
        if (src == null) {
            placeholder()
        } else {
            SubcomposeAsyncImage(
                model = src,
                contentDescription = "Capa de $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { placeholder() },
                error = { placeholder() },
            )
        }
    }
}
