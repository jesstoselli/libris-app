package app.billfolder.libris.data.auth

import app.billfolder.libris.data.remote.LibrisApi
import app.billfolder.libris.data.remote.dto.UserDto
import app.billfolder.libris.data.remote.toDomain
import app.billfolder.libris.domain.model.AccountState
import app.billfolder.libris.domain.model.User
import app.billfolder.libris.domain.repository.AuthRepository
import app.billfolder.libris.domain.error.ApiException
import app.billfolder.libris.domain.error.SessionExpiredException
import app.billfolder.libris.domain.error.SignedOutException
import app.billfolder.libris.domain.error.UnauthorizedException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable

@Serializable
data class Session(val accessToken: String, val refreshToken: String, val user: UserDto)

/** Where the tokens live; each platform keeps them private to the app (Android prefs, iOS Keychain). */
interface TokenStore {
    fun load(): Session?
    fun save(session: Session?)
}

class InMemoryTokenStore(private var session: Session? = null) : TokenStore {
    override fun load(): Session? = session
    override fun save(session: Session?) {
        this.session = session
    }
}

class AuthSession(private val api: LibrisApi, private val tokens: TokenStore) : AuthRepository {

    private val refreshLock = Mutex()
    private val _state = MutableStateFlow<AccountState>(tokens.load()?.let { AccountState.SignedIn(it.user.toDomain()) } ?: AccountState.SignedOut)
    override val state: StateFlow<AccountState> = _state.asStateFlow()

    override suspend fun login(email: String, password: String): User {
        val auth = api.login(email.trim(), password)
        tokens.save(Session(auth.accessToken, auth.refreshToken, auth.user))
        _state.value = AccountState.SignedIn(auth.user.toDomain())
        return auth.user.toDomain()
    }

    /** Forgets the tokens first, so being offline never keeps the session alive; telling the server is best-effort. */
    override suspend fun logout() {
        val session = tokens.load()
        tokens.save(null)
        _state.value = AccountState.SignedOut
        if (session == null) return
        try {
            api.logout(session.refreshToken)
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // The refresh token simply expires on its own.
        }
    }

    suspend fun <T> authorized(call: suspend (accessToken: String) -> T): T {
        val session = tokens.load() ?: throw SignedOutException()
        return try {
            call(session.accessToken)
        } catch (_: UnauthorizedException) {
            call(refreshedAfter(session.accessToken))
        }
    }

    /**
     * One refresh at a time: the server rotates refresh tokens and treats a reused one as stolen.
     * A caller that waited (or a late 401 for an old token) finds the new token already there.
     * Only an explicit rejection (400/401) ends the session; a network blip or 5xx just fails this call.
     */
    private suspend fun refreshedAfter(staleAccessToken: String): String = refreshLock.withLock {
        val current = tokens.load() ?: throw SignedOutException()
        if (current.accessToken != staleAccessToken) return current.accessToken
        val auth = try {
            api.refresh(current.refreshToken)
        } catch (e: ApiException) {
            if (e.status == 400 || e.status == 401) {
                tokens.save(null)
                _state.value = AccountState.SignedOut
                throw SessionExpiredException()
            }
            throw e
        }
        tokens.save(current.copy(accessToken = auth.accessToken, refreshToken = auth.refreshToken))
        auth.accessToken
    }
}
