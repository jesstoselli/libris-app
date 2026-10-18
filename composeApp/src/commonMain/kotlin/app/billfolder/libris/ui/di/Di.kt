package app.billfolder.libris.ui.di

import app.billfolder.libris.data.auth.TokenStore
import app.billfolder.libris.di.dataModule
import app.billfolder.libris.ui.feature.acervo.AcervoViewModel
import app.billfolder.libris.ui.feature.ficha.FichaViewModel
import app.billfolder.libris.ui.feature.form.BookFormViewModel
import app.billfolder.libris.ui.feature.genres.GenresViewModel
import app.billfolder.libris.ui.feature.login.LoginViewModel
import app.billfolder.libris.ui.feature.numeros.NumerosViewModel
import app.billfolder.libris.ui.feature.wishlist.WishlistViewModel
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** The screens' ViewModels; they only see the domain repositories. */
val presentationModule: Module = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::AcervoViewModel)
    viewModelOf(::WishlistViewModel)
    viewModelOf(::GenresViewModel)
    viewModel { NumerosViewModel(get()) }
    viewModel { (bookId: String) -> FichaViewModel(bookId, get(), get()) }
    viewModel { (bookId: String?) -> BookFormViewModel(bookId, get(), get(), get()) }
}

private var started = false

fun initKoin(baseUrl: String, tokens: TokenStore, engine: HttpClientEngine) {
    if (started) return
    started = true
    startKoin { modules(dataModule(baseUrl, tokens, engine), presentationModule) }
}
