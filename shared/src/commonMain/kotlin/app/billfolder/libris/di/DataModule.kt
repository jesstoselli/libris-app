package app.billfolder.libris.di

import app.billfolder.libris.data.auth.AuthSession
import app.billfolder.libris.data.auth.TokenStore
import app.billfolder.libris.data.remote.LibrisApi
import app.billfolder.libris.data.remote.librisHttpClient
import app.billfolder.libris.data.repository.RemoteBookRepository
import app.billfolder.libris.data.repository.RemoteCoverRepository
import app.billfolder.libris.data.repository.RemoteGenreRepository
import app.billfolder.libris.data.repository.RemoteReadingRepository
import app.billfolder.libris.data.repository.RemoteStatsRepository
import app.billfolder.libris.domain.repository.AuthRepository
import app.billfolder.libris.domain.repository.BookRepository
import app.billfolder.libris.domain.repository.CoverRepository
import app.billfolder.libris.domain.repository.GenreRepository
import app.billfolder.libris.domain.repository.ReadingRepository
import app.billfolder.libris.domain.repository.StatsRepository
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import org.koin.core.module.Module
import org.koin.dsl.module

/** The platform supplies where the API lives, how tokens are kept and the HTTP engine; the rest is shared. */
fun dataModule(baseUrl: String, tokens: TokenStore, engine: HttpClientEngine): Module = module {
    single<HttpClient> { librisHttpClient(engine) }
    single { LibrisApi(get(), baseUrl) }
    single<TokenStore> { tokens }
    single { AuthSession(get(), get()) }
    single<AuthRepository> { get<AuthSession>() }
    single<BookRepository> { RemoteBookRepository(get(), get()) }
    single<ReadingRepository> { RemoteReadingRepository(get(), get()) }
    single<GenreRepository> { RemoteGenreRepository(get(), get()) }
    single<CoverRepository> { RemoteCoverRepository(get(), get()) }
    single<StatsRepository> { RemoteStatsRepository(get(), get()) }
}
