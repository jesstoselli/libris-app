package app.billfolder.libris.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.theme.Libris

@Composable
fun SessionAction(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Image(
        painter = rememberVectorPainter(LibrisIcons.Exit),
        contentDescription = "Sair",
        colorFilter = ColorFilter.tint(Libris.brass),
        modifier = modifier
            .size(44.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(10.dp),
    )
}
