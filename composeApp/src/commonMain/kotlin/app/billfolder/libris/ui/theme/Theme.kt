package app.billfolder.libris.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.generated.resources.Res
import app.billfolder.libris.ui.generated.resources.aleo
import app.billfolder.libris.ui.generated.resources.yantramanav_bold
import app.billfolder.libris.ui.generated.resources.yantramanav_light
import app.billfolder.libris.ui.generated.resources.yantramanav_medium
import app.billfolder.libris.ui.generated.resources.yantramanav_regular
import org.jetbrains.compose.resources.Font

/** "Arquivo pessoal": quiet ledger, aged paper, oxblood ink and restrained brass (same tokens as the Mac app). */
object Libris {
    val desk = Color(0xFF292A1E)
    val ledgerLine = Color(0xFF9A8A59)
    val paper = Color(0xFFF2E5C9)
    val paperShade = Color(0xFFD8C39E)
    val ink = Color(0xFF30231B)
    val inkSoft = Color(0xFF6C5946)
    val oxblood = Color(0xFF741B18)
    val brass = Color(0xFFB6873F)
    val deskText = Color(0xFFF0D2A7)
    val deskTextSoft = Color(0xFFBBAA8C)
    val rule = Color(0xFFB9A174)
    val deskOverlay = Color(0x6B0F100B)
}

@Composable
private fun aleoFamily() = FontFamily(
    listOf(400, 600, 700, 800).map { w ->
        // Aleo ships as one variable font; each weight is an instance of its wght axis.
        Font(Res.font.aleo, FontWeight(w), variationSettings = FontVariation.Settings(FontVariation.weight(w)))
    },
)

@Composable
private fun yantramanavFamily() = FontFamily(
    Font(Res.font.yantramanav_light, FontWeight.Light),
    Font(Res.font.yantramanav_regular, FontWeight.Normal),
    Font(Res.font.yantramanav_medium, FontWeight.Medium),
    Font(Res.font.yantramanav_bold, FontWeight.Bold),
)

data class LibrisType(val title: FontFamily, val body: FontFamily)

@Composable
fun LibrisTheme(content: @Composable (LibrisType) -> Unit) {
    val type = LibrisType(aleoFamily(), yantramanavFamily())
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Libris.oxblood,
            onPrimary = Libris.paper,
            secondary = Libris.brass,
            background = Libris.desk,
            surface = Libris.paper,
            onSurface = Libris.ink,
            onBackground = Libris.deskText,
            error = Libris.oxblood,
        ),
        typography = MaterialTheme.typography.copy(
            bodyLarge = TextStyle(fontFamily = type.body, fontSize = 17.sp, color = Libris.ink),
            bodyMedium = TextStyle(fontFamily = type.body, fontSize = 15.sp, color = Libris.ink),
            labelMedium = TextStyle(fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft),
        ),
    ) { content(type) }
}
