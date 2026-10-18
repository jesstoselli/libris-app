package app.billfolder.libris.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import app.billfolder.libris.ui.theme.Libris

/** "Registrar" as a floating button over the lists, bottom right, so the ribbon keeps only the places to go. */
@Composable
fun AddBookButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    Box(
        modifier
            .size(56.dp)
            .shadow(6.dp, CircleShape)
            .background(if (pressed) Color(0xFF4C100F) else Libris.oxblood, CircleShape)
            .then(if (focused) Modifier.border(2.dp, Libris.brass, CircleShape) else Modifier)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClickLabel = "Registrar livro", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            rememberVectorPainter(LibrisIcons.Plus),
            contentDescription = "Registrar livro",
            colorFilter = ColorFilter.tint(Libris.paper),
            modifier = Modifier.size(26.dp),
        )
    }
}
