package app.billfolder.libris.ui.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** A book page asking its list (the Acervo, or the Wishlist) to show only one author, or only one genre. */
data class FilterRequest(val wishlist: Boolean, val author: String? = null, val genreId: String? = null)

/** App-wide hand-off from a book page to the list it goes back to; the list takes the request once. */
class FilterRequests {
    private val _pending = MutableStateFlow<FilterRequest?>(null)
    val pending: StateFlow<FilterRequest?> = _pending.asStateFlow()

    fun showAuthor(author: String, wishlist: Boolean) {
        _pending.value = FilterRequest(wishlist, author = author)
    }

    fun showGenre(genreId: String, wishlist: Boolean) {
        _pending.value = FilterRequest(wishlist, genreId = genreId)
    }

    fun take(request: FilterRequest) = _pending.update { if (it == request) null else it }
}
