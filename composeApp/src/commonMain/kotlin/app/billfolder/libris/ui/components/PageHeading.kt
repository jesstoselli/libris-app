package app.billfolder.libris.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

@Composable
fun PageHeading(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    val type = LocalLibrisType.current
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                fontFamily = type.title,
                fontWeight = FontWeight.Bold,
                fontSize = 30.sp,
                lineHeight = 31.sp,
                letterSpacing = (-0.45).sp,
                color = Libris.deskText,
            )
            subtitle?.let {
                Text(it, fontFamily = type.body, fontWeight = FontWeight.Light, fontSize = 14.sp, color = Libris.deskTextSoft)
            }
        }
        action?.invoke()
    }
}
