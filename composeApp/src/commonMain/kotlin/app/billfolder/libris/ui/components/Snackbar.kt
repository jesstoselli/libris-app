package app.billfolder.libris.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf

/** One snackbar for the whole shell, so a confirmation survives navigating away ("Livro apagado."). */
val LocalSnackbar = staticCompositionLocalOf { SnackbarHostState() }
