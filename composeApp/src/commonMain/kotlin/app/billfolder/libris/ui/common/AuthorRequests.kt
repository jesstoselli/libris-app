package app.billfolder.libris.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** A book page asking its list (the Acervo, or the Wishlist) to show only one author. */
data class AuthorRequest(val author: String, val wishlist: Boolean)

/** App-wide hand-off from a book page to the list it goes back to; the list takes the request once. */
class AuthorRequests {
    private val _pending = MutableStateFlow<AuthorRequest?>(null)
    val pending: StateFlow<AuthorRequest?> = _pending.asStateFlow()

    fun show(author: String, wishlist: Boolean) {
        _pending.value = AuthorRequest(author, wishlist)
    }

    fun take(request: AuthorRequest) = _pending.update { if (it == request) null else it }
}
