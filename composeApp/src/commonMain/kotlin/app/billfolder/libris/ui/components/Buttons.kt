package app.billfolder.libris.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

/** The four kinds of button, the same on every screen (and the same as on the Mac). */
enum class ButtonVariant {
    /** The one action a screen is about: Cadastrar obra, Anotar, Comprei, Entrar. */
    Primary,

    /** Alternatives next to it: Cancelar, Buscar. A shade of the paper rather than an outline, so it stays quieter. */
    Secondary,

    /** Inline actions that shouldn't draw the eye: editar datas, + autor, ✎, ×. */
    Quiet,

    /** Removes something; outlined so it never competes with the primary action. */
    Danger,
}

/** Every control is this tall, so rows of buttons, stamps and choices line up. */
val ControlHeight = 48.dp

private val Shape = RoundedCornerShape(2.dp)

private class Look(val container: Color, val pressed: Color, val content: Color, val border: Color?)

private fun look(variant: ButtonVariant, onDesk: Boolean) = when (variant) {
    ButtonVariant.Primary -> Look(Libris.oxblood, Color(0xFF4C100F), Libris.paper, null)
    ButtonVariant.Secondary ->
        if (onDesk) {
            Look(Libris.deskText.copy(alpha = 0.10f), Libris.deskText.copy(alpha = 0.18f), Libris.deskText, null)
        } else {
            Look(Libris.paperShade.copy(alpha = 0.55f), Libris.paperShade, Libris.ink, null)
        }
    ButtonVariant.Quiet ->
        if (onDesk) {
            Look(Color.Transparent, Libris.deskText.copy(alpha = 0.10f), Libris.deskText, null)
        } else {
            Look(Color.Transparent, Libris.oxblood.copy(alpha = 0.12f), Libris.oxblood, null)
        }
    ButtonVariant.Danger -> Look(Color.Transparent, Libris.oxblood.copy(alpha = 0.14f), Libris.oxblood, Libris.oxblood.copy(alpha = 0.55f))
}

/** Background, border, pressed darkening and the brass focus ring shared by text and icon buttons. */
@Composable
private fun Modifier.buttonSurface(variant: ButtonVariant, onDesk: Boolean, enabled: Boolean, interaction: MutableInteractionSource, onClick: () -> Unit): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val focused by interaction.collectIsFocusedAsState()
    val l = look(variant, onDesk)
    return this
        .heightIn(min = ControlHeight)
        .alpha(if (enabled) 1f else 0.45f)
        .background(if (pressed) l.pressed else l.container, Shape)
        .then(if (l.border != null) Modifier.border(1.dp, l.border, Shape) else Modifier)
        .then(if (focused) Modifier.border(2.dp, Libris.brass, Shape) else Modifier)
        .clickable(interactionSource = interaction, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
}

@Composable
fun LibrisButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Primary,
    enabled: Boolean = true,
    onDesk: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        // Quiet buttons sit in the text flow, so their label lines up with the labels around them.
        modifier
            .widthIn(min = ControlHeight)
            .buttonSurface(variant, onDesk, enabled, interaction, onClick)
            .padding(horizontal = if (variant == ButtonVariant.Quiet) 0.dp else 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = LocalLibrisType.current.body, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = look(variant, onDesk).content, maxLines = 1)
    }
}

/** Square button for a single glyph (✎, ×); [description] is what a screen reader says. */
@Composable
fun LibrisIconButton(
    glyph: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Quiet,
    onDesk: Boolean = false,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier.size(ControlHeight).buttonSurface(variant, onDesk, true, interaction, onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(glyph, fontSize = 18.sp, color = look(variant, onDesk).content)
    }
}

/** Square button for a drawn icon (the scanner inside the ISBN field); [description] is what a screen reader says. */
@Composable
fun LibrisIconAction(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: ButtonVariant = ButtonVariant.Quiet,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier.size(ControlHeight).buttonSurface(variant, false, true, interaction, onClick).semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Image(rememberVectorPainter(icon), contentDescription = null, modifier = Modifier.size(24.dp), colorFilter = ColorFilter.tint(look(variant, false).content))
    }
}

/** A secondary action with an icon before its label, narrower than a full button (Galeria, Câmera under the cover). */
@Composable
fun LibrisCompactButton(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val content = look(ButtonVariant.Secondary, false).content
    Row(
        modifier.buttonSurface(ButtonVariant.Secondary, false, true, interaction, onClick).padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(rememberVectorPainter(icon), contentDescription = null, modifier = Modifier.size(18.dp), colorFilter = ColorFilter.tint(content))
        Text(label, fontFamily = LocalLibrisType.current.body, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = content, maxLines = 1)
    }
}

/** A secondary button with a small wine count after its label (Filtros 2); no count, no badge. */
@Composable
fun LibrisCountButton(label: String, count: Int, onClick: () -> Unit, modifier: Modifier = Modifier, onDesk: Boolean = false) {
    val interaction = remember { MutableInteractionSource() }
    val type = LocalLibrisType.current
    Row(
        modifier.buttonSurface(ButtonVariant.Secondary, onDesk, true, interaction, onClick).padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontFamily = type.body, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = look(ButtonVariant.Secondary, onDesk).content, maxLines = 1)
        if (count > 0) {
            Box(
                Modifier.heightIn(min = 20.dp).widthIn(min = 20.dp).background(Libris.oxblood, RoundedCornerShape(10.dp)).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text("$count", fontFamily = type.body, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Libris.paper)
            }
        }
    }
}
