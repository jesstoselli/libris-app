package app.billfolder.libris.ui.nav

import kotlinx.serialization.Serializable

@Serializable data object AcervoRoute

@Serializable data object WishlistRoute

@Serializable data object NumerosRoute

@Serializable data object RegistrarRoute

/** [fromWishlist] only decides which ribbon item is lit. */
@Serializable data class FichaRoute(val id: String, val fromWishlist: Boolean = false)

@Serializable data object GenresRoute

@Serializable data class EditRoute(val id: String)
