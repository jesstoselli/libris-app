package app.billfolder.libris.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType

/** The "typed form" look: small label above, value resting on a thin ruled line. */
@Composable
fun RuledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    error: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val type = LocalLibrisType.current
    Column(modifier.fillMaxWidth()) {
        if (label.isNotEmpty()) Text(label, fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
        Row(verticalAlignment = Alignment.CenterVertically) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(fontFamily = type.body, fontSize = 17.sp, color = Libris.ink),
                cursorBrush = SolidColor(Libris.oxblood),
                visualTransformation = visualTransformation,
                keyboardOptions = keyboardOptions,
                keyboardActions = keyboardActions,
                modifier = Modifier.weight(1f).padding(vertical = 6.dp),
            )
            trailing?.invoke()
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(if (error != null) Libris.oxblood else Libris.inkSoft))
        error?.let { Text(it, fontFamily = type.body, fontSize = 13.sp, color = Libris.oxblood, modifier = Modifier.padding(top = 2.dp)) }
    }
}

/** The screen's main action across the full width of the phone. */
@Composable
fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) =
    LibrisButton(label, onClick, modifier.fillMaxWidth(), ButtonVariant.Primary, enabled)

/** The main way into a list: wide, quiet, with a real search icon. */
@Composable
fun DeskSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val type = LocalLibrisType.current
    Row(
        modifier
            .heightIn(min = ControlHeight)
            .background(Libris.deskText.copy(alpha = 0.05f))
            .border(1.dp, Libris.deskText.copy(alpha = 0.16f), RectangleShape)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            rememberVectorPainter(LibrisIcons.Search),
            contentDescription = null,
            colorFilter = ColorFilter.tint(Libris.deskTextSoft),
            modifier = Modifier.size(20.dp),
        )
        Box(Modifier.weight(1f).padding(start = 10.dp), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                Text(placeholder, fontFamily = type.body, fontSize = 16.sp, color = Libris.deskTextSoft.copy(alpha = 0.78f), maxLines = 1)
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(fontFamily = type.body, fontSize = 16.sp, color = Libris.deskText),
                cursorBrush = SolidColor(Libris.brass),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
