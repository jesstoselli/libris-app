package app.billfolder.libris.ui.platform

import androidx.compose.runtime.Composable

// The JVM target only exists to run the shared tests; it has no camera or photo library.

@Composable
actual fun rememberCoverPicker(onPicked: (PickedImage) -> Unit, onError: (String) -> Unit): CoverPicker = object : CoverPicker {
    override val cameraAvailable = false
    override fun fromGallery() = onError("Indisponível neste alvo.")
    override fun fromCamera() = onError("Indisponível neste alvo.")
}

@Composable
actual fun IsbnScanner(onScanned: (String) -> Unit, onClose: () -> Unit) = onClose()
