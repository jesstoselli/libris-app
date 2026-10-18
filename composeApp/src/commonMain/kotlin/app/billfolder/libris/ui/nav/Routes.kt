package app.billfolder.libris.ui.nav

import kotlinx.serialization.Serializable

@Serializable data object LibraryRoute

@Serializable data object WishlistRoute

@Serializable data object StatsRoute

@Serializable data object AddBookRoute

/** [fromWishlist] only decides which ribbon item is lit. */
@Serializable data class BookDetailRoute(val id: String, val fromWishlist: Boolean = false)

@Serializable data object GenresRoute

@Serializable data class EditRoute(val id: String)
