package app.billfolder.libris.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

@Composable
fun PageHeading(title: String, subtitle: String? = null, modifier: Modifier = Modifier) {
    val type = LocalLibrisType.current
    Column(modifier) {
        Text(title, fontFamily = type.title, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp, lineHeight = 32.sp, color = Libris.deskText)
        subtitle?.let { Text(it, fontFamily = type.body, fontWeight = FontWeight.Light, fontSize = 15.sp, color = Libris.deskTextSoft) }
    }
}
