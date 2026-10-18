package app.billfolder.libris.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import app.billfolder.libris.domain.repository.AuthRepository
import app.billfolder.libris.domain.model.BookOwnership
import app.billfolder.libris.ui.components.LibrisIcons
import app.billfolder.libris.ui.components.LocalSnackbar
import app.billfolder.libris.ui.components.RibbonBar
import app.billfolder.libris.ui.components.RibbonItem
import app.billfolder.libris.ui.feature.library.LibraryScreen
import app.billfolder.libris.ui.feature.bookdetail.BookDetailScreen
import app.billfolder.libris.ui.feature.form.BookFormScreen
import app.billfolder.libris.ui.feature.genres.GenresScreen
import app.billfolder.libris.ui.feature.stats.StatsScreen
import app.billfolder.libris.ui.feature.wishlist.WishlistScreen
import kotlin.reflect.KClass
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MainShell(auth: AuthRepository = koinInject()) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val scope = rememberCoroutineScope()
    val logout = { scope.launch { auth.logout() }; Unit }
    fun isOn(route: KClass<*>) = entry?.destination?.hasRoute(route) == true
    val wishlistDetail = entry?.takeIf { it.destination.hasRoute(BookDetailRoute::class) }?.toRoute<BookDetailRoute>()?.fromWishlist == true
    fun go(route: Any) = nav.navigate(route) {
        popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    val snackbar = remember { SnackbarHostState() }
    CompositionLocalProvider(LocalSnackbar provides snackbar) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).statusBarsPadding()) {
                NavHost(nav, startDestination = LibraryRoute) {
                    composable<LibraryRoute> {
                        LibraryScreen(
                            onOpenBook = { nav.navigate(BookDetailRoute(it)) },
                            onRegister = { go(AddBookRoute) },
                            onManageGenres = { nav.navigate(GenresRoute) },
                            onLogout = logout,
                        )
                    }
                    composable<BookDetailRoute> { entry ->
                        val id = entry.toRoute<BookDetailRoute>().id
                        BookDetailScreen(id, onBack = { nav.popBackStack() }, onEdit = { nav.navigate(EditRoute(id)) })
                    }
                    composable<EditRoute> { entry ->
                        BookFormScreen(entry.toRoute<EditRoute>().id, onSaved = { nav.popBackStack() }, onBack = { nav.popBackStack() })
                    }
                    composable<WishlistRoute> {
                        WishlistScreen(
                            onOpenBook = { nav.navigate(BookDetailRoute(it, fromWishlist = true)) },
                            onRegister = { go(AddBookRoute) },
                            onLogout = logout,
                        )
                    }
                    composable<StatsRoute> { StatsScreen(onLogout = logout) }
                    composable<GenresRoute> { GenresScreen(onBack = { nav.popBackStack() }) }
                    composable<AddBookRoute> {
                        BookFormScreen(
                            null,
                            onSaved = { book ->
                                val target: Any = if (book.ownership == BookOwnership.Owned) BookDetailRoute(book.id) else WishlistRoute
                                nav.navigate(target) { popUpTo(AddBookRoute) { inclusive = true } }
                            },
                            onBack = null,
                            onLogout = logout,
                        )
                    }
                }
            }
            RibbonBar(
                listOf(
                    RibbonItem("Acervo", LibrisIcons.Book, isOn(LibraryRoute::class) || (isOn(BookDetailRoute::class) && !wishlistDetail) || isOn(EditRoute::class) || isOn(GenresRoute::class)) { go(LibraryRoute) },
                    RibbonItem("Wishlist", LibrisIcons.Bookmark, isOn(WishlistRoute::class) || (isOn(BookDetailRoute::class) && wishlistDetail)) { go(WishlistRoute) },
                    RibbonItem("Números", LibrisIcons.Chart, isOn(StatsRoute::class)) { go(StatsRoute) },
                    RibbonItem("Registrar", LibrisIcons.Plus, isOn(AddBookRoute::class)) { go(AddBookRoute) },
                ),
            )
        }
        Box(Modifier.fillMaxSize().padding(bottom = 86.dp), contentAlignment = Alignment.BottomCenter) { SnackbarHost(snackbar) }
    }
}
