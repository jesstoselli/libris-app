package app.billfolder.libris.ui.components

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
        Text(label, fontFamily = type.body, fontSize = 13.sp, color = Libris.inkSoft)
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

@Composable
fun PrimaryButton(label: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val type = LocalLibrisType.current
    Box(
        modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = 48.dp)
            .background(Libris.oxblood)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontFamily = type.title, fontSize = 16.sp, color = Libris.paper)
    }
}

@Composable
fun DeskSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    val type = LocalLibrisType.current
    Box(
        modifier
            .heightIn(min = 44.dp)
            .background(Color(0x61171810))
            .border(1.dp, Libris.deskTextSoft.copy(alpha = 0.24f), RectangleShape)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (value.isEmpty()) {
            Text(placeholder, fontFamily = type.body, fontSize = 14.sp, color = Libris.deskTextSoft.copy(alpha = 0.78f))
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(fontFamily = type.body, fontSize = 14.sp, color = Libris.deskText),
            cursorBrush = SolidColor(Libris.brass),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
