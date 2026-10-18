package app.billfolder.libris.ui.components

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

    /** Alternatives next to it: Cancelar, Buscar, Escanear. */
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
            Look(Color.Transparent, Libris.deskText.copy(alpha = 0.12f), Libris.deskText, Libris.deskTextSoft.copy(alpha = 0.5f))
        } else {
            Look(Color.Transparent, Libris.inkSoft.copy(alpha = 0.16f), Libris.ink, Libris.inkSoft)
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
