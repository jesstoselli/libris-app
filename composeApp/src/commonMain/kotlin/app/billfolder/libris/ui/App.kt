package app.billfolder.libris.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.shared.AccountState
import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.ui.components.Desk
import app.billfolder.libris.ui.login.LoginScreen
import app.billfolder.libris.ui.nav.MainShell
import app.billfolder.libris.ui.theme.LibrisTheme
import app.billfolder.libris.ui.theme.LocalLibrisType
import org.koin.compose.koinInject

@Composable
fun App() {
    LibrisTheme { type ->
        CompositionLocalProvider(LocalLibrisType provides type) {
            val session = koinInject<AuthSession>()
            val account by session.state.collectAsStateWithLifecycle()
            Desk {
                when (account) {
                    AccountState.SignedOut -> LoginScreen()
                    is AccountState.SignedIn -> MainShell()
                }
            }
        }
    }
}
