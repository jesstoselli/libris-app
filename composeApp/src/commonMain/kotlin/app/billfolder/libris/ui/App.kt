package app.billfolder.libris.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.billfolder.libris.data.auth.AccountState
import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.ui.components.Desk
import app.billfolder.libris.ui.feature.login.LoginScreen
import app.billfolder.libris.ui.nav.MainShell
import app.billfolder.libris.ui.theme.LibrisTheme
import app.billfolder.libris.ui.theme.LocalLibrisType
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade
import org.koin.compose.koinInject

@Composable
fun App() {
    // Covers load through Ktor on every platform (iOS has no default network fetcher).
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context).components { add(KtorNetworkFetcherFactory()) }.crossfade(true).build()
    }
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
