package app.billfolder.libris.ui

import androidx.compose.ui.window.ComposeUIViewController
import app.billfolder.libris.data.auth.KeychainTokenStore
import io.ktor.client.engine.darwin.Darwin
import platform.UIKit.UIViewController

/** Called from Swift; [baseUrl] comes from the Xcode build configuration (debug → local API, release → production). */
fun MainViewController(baseUrl: String): UIViewController {
    initKoin(baseUrl, KeychainTokenStore(), Darwin.create())
    return ComposeUIViewController { App() }
}
