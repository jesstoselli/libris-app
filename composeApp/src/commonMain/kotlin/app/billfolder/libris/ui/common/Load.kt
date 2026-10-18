package app.billfolder.libris.ui.common

sealed interface Load<out T> {
    data object Loading : Load<Nothing>
    data class Loaded<T>(val value: T) : Load<T>
    data class Failed(val message: String) : Load<Nothing>
}

val <T> Load<T>.valueOrNull: T? get() = (this as? Load.Loaded<T>)?.value

/** Runs a load and turns any failure into the message the screen shows (cancellation still propagates). */
suspend fun <T> loadOf(block: suspend () -> T): Load<T> = try {
    Load.Loaded(block())
} catch (e: kotlinx.coroutines.CancellationException) {
    throw e
} catch (e: Exception) {
    Load.Failed(e.userMessage())
}

fun <T, R> Load<T>.map(transform: (T) -> R): Load<R> = when (this) {
    is Load.Loaded -> Load.Loaded(transform(value))
    is Load.Failed -> this
    Load.Loading -> Load.Loading
}
