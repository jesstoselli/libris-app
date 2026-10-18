package app.billfolder.libris.ui.platform

import androidx.compose.runtime.Composable

/** A cover chosen on the device, already downscaled and re-encoded as JPEG (the API refuses files over 5 MB). */
class PickedImage(val bytes: ByteArray, val mimeType: String = "image/jpeg")

interface CoverPicker {
    val cameraAvailable: Boolean
    fun fromGallery()
    fun fromCamera()
}

@Composable
expect fun rememberCoverPicker(onPicked: (PickedImage) -> Unit, onError: (String) -> Unit): CoverPicker

/** Full-screen camera that reports the first book barcode it reads (EAN-13 starting with 978/979). */
@Composable
expect fun IsbnScanner(onScanned: (String) -> Unit, onClose: () -> Unit)

const val MAX_COVER_SIDE = 1600
