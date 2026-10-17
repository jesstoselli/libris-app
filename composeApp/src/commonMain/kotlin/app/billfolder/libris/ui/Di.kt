package app.billfolder.libris.ui

import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.TokenStore
import app.billfolder.libris.shared.librisHttpClient
import app.billfolder.libris.ui.login.LoginViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

/** Everything the screens need; the platform supplies where the API lives, how tokens are kept and the HTTP engine. */
fun librisModule(baseUrl: String, tokens: TokenStore, engine: HttpClientEngine): Module = module {
    single<HttpClient> { librisHttpClient(engine) }
    single { LibrisApi(get(), baseUrl) }
    single { tokens }
    single { AuthSession(get(), get()) }
    viewModelOf(::LoginViewModel)
}

private var started = false

fun initKoin(baseUrl: String, tokens: TokenStore, engine: HttpClientEngine) {
    if (started) return
    started = true
    startKoin { modules(librisModule(baseUrl, tokens, engine)) }
}
