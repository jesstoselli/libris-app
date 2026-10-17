package app.billfolder.libris.ui.nav

import kotlinx.serialization.Serializable

@Serializable data object AcervoRoute

@Serializable data object WishlistRoute

@Serializable data object NumerosRoute

@Serializable data object RegistrarRoute

@Serializable data class FichaRoute(val id: String)

@Serializable data class EditRoute(val id: String)
