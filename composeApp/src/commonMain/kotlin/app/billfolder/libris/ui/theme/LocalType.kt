package app.billfolder.libris.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily

val LocalLibrisType = staticCompositionLocalOf { LibrisType(FontFamily.Serif, FontFamily.SansSerif) }
