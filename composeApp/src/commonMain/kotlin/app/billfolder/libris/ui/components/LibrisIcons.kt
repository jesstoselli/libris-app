package app.billfolder.libris.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** The ribbon icons of the Mac app (same 24×24 paths). */
object LibrisIcons {
    val Book = icon("Library", "M3 5c3-1.5 6-1.5 9 .5 3-2 6-2 9-.5v14c-3-1.5-6-1.5-9 .5-3-2-6-2-9-.5z")
    val Bookmark = icon("Wishlist", "M6 2h12v20l-6-5-6 5z")
    val Chart = icon("Stats", "M4 20V10h3v10zm6.5 0V4h3v16zM17 20v-7h3v7z")
    val Plus = icon("Add", "M11 4h2v7h7v2h-7v7h-2v-7H4v-2h7z")
    val Exit = icon("Exit", "M10 3h9v18h-9v-2h7V5h-7zm-1.5 5 1.4 1.4L8.3 11H15v2H8.3l1.6 1.6L8.5 16 4.5 12z")
    val Search = outline("Buscar", "M10.5 4a6.5 6.5 0 1 0 0 13a6.5 6.5 0 1 0 0 -13 M15.5 15.5L21 21")
    val Eye = outline("Mostrar", "M2 12s3.5-6.5 10-6.5S22 12 22 12s-3.5 6.5-10 6.5S2 12 2 12z M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0")
    val EyeOff = outline("Ocultar", "M2 12s3.5-6.5 10-6.5S22 12 22 12s-3.5 6.5-10 6.5S2 12 2 12z M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0 M4 4l16 16")

    private fun icon(name: String, path: String) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(addPathNodes(path), fill = SolidColor(Color.Black))
        .build()

    private fun outline(name: String, path: String) = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        .addPath(addPathNodes(path), fill = null, stroke = SolidColor(Color.Black), strokeLineWidth = 1.8f)
        .build()
}
