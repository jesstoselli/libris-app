package app.billfolder.libris.ui.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.ui.components.Ficha
import app.billfolder.libris.ui.components.LibrisIcons
import app.billfolder.libris.ui.components.PrimaryButton
import app.billfolder.libris.ui.components.RuledField
import app.billfolder.libris.ui.theme.Libris
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(viewModel: LoginViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val type = LocalLibrisType.current

    Box(Modifier.fillMaxSize().imePadding().padding(24.dp), contentAlignment = Alignment.Center) {
        Ficha(Modifier.widthIn(max = 420.dp).fillMaxWidth(), padding = 26.dp, foldedCorner = true) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Column {
                    Text("Libris", fontFamily = type.title, fontWeight = FontWeight.Bold, fontSize = 40.sp, letterSpacing = (-0.5).sp, color = Libris.oxblood)
                    Text("Seu acervo de leituras", fontFamily = type.body, fontWeight = FontWeight.Light, color = Libris.inkSoft)
                    Box(Modifier.padding(top = 10.dp).fillMaxWidth(0.18f).height(2.dp).background(Libris.brass))
                }
                RuledField(
                    label = "E-mail",
                    value = state.email,
                    onValueChange = viewModel::onEmail,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                )
                RuledField(
                    label = "Senha",
                    value = state.password,
                    onValueChange = viewModel::onPassword,
                    visualTransformation = if (state.showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { viewModel.submit() }),
                    trailing = {
                        Image(
                            rememberVectorPainter(if (state.showPassword) LibrisIcons.EyeOff else LibrisIcons.Eye),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Libris.inkSoft),
                            modifier = Modifier
                                .size(36.dp)
                                .semantics {
                                    contentDescription = if (state.showPassword) "Ocultar senha" else "Mostrar senha"
                                    selected = state.showPassword
                                }
                                .clickable(role = Role.Button, onClick = viewModel::togglePassword)
                                .padding(8.dp),
                        )
                    },
                )
                state.error?.let { Text(it, color = Libris.oxblood, fontFamily = type.body, fontSize = 14.sp) }
                PrimaryButton(if (state.pending) "Entrando…" else "Entrar", enabled = state.canSubmit, onClick = viewModel::submit)
            }
        }
    }
}
