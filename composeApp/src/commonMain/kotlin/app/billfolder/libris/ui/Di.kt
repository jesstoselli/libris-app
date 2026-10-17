package app.billfolder.libris.ui

import app.billfolder.libris.shared.AuthSession
import app.billfolder.libris.shared.LibrisApi
import app.billfolder.libris.shared.TokenStore
import app.billfolder.libris.shared.librisHttpClient
import app.billfolder.libris.ui.acervo.AcervoViewModel
import app.billfolder.libris.ui.genres.GenresViewModel
import app.billfolder.libris.ui.numeros.NumerosViewModel
import app.billfolder.libris.ui.wishlist.WishlistViewModel
import app.billfolder.libris.ui.ficha.FichaViewModel
import app.billfolder.libris.ui.form.BookFormViewModel
import app.billfolder.libris.ui.login.LoginViewModel
import org.koin.core.module.dsl.viewModel
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
    viewModelOf(::AcervoViewModel)
    viewModelOf(::WishlistViewModel)
    viewModelOf(::GenresViewModel)
    viewModel { NumerosViewModel(get(), get()) }
    viewModel { (bookId: String) -> FichaViewModel(bookId, get(), get()) }
    viewModel { (bookId: String?) -> BookFormViewModel(bookId, get(), get()) }
}

private var started = false

fun initKoin(baseUrl: String, tokens: TokenStore, engine: HttpClientEngine) {
    if (started) return
    started = true
    startKoin { modules(librisModule(baseUrl, tokens, engine)) }
}
