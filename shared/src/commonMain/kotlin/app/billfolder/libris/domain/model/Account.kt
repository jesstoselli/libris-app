package app.billfolder.libris.domain.model

data class User(val id: String, val email: String, val displayName: String)

sealed interface AccountState {
    data object SignedOut : AccountState
    data class SignedIn(val user: User) : AccountState
}
