package app.billfolder.libris.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsImageRenderer
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.posix.memcpy

@Composable
actual fun rememberCoverPicker(onPicked: (PickedImage) -> Unit, onError: (String) -> Unit): CoverPicker =
    remember { IosCoverPicker(onPicked, onError) }

@OptIn(ExperimentalForeignApi::class)
private class IosCoverPicker(private val onPicked: (PickedImage) -> Unit, private val onError: (String) -> Unit) : CoverPicker {

    override val cameraAvailable = UIImagePickerController.isSourceTypeAvailable(UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera)

    // The pickers keep only weak references to their delegates; these fields keep them alive.
    private val galleryDelegate = object : NSObject(), PHPickerViewControllerDelegateProtocol {
        override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
            picker.dismissViewControllerAnimated(true, null)
            val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
            result.itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, _ ->
                val image = data?.let { UIImage(data = it) }
                dispatch_async(dispatch_get_main_queue()) {
                    if (image == null) onError("Não consegui abrir essa imagem.") else deliver(image)
                }
            }
        }
    }

    private val cameraDelegate = object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
        override fun imagePickerController(picker: UIImagePickerController, didFinishPickingMediaWithInfo: Map<Any?, *>) {
            picker.dismissViewControllerAnimated(true, null)
            (didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage)?.let(::deliver)
        }

        override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
            picker.dismissViewControllerAnimated(true, null)
        }
    }

    override fun fromGallery() {
        val config = PHPickerConfiguration().apply {
            filter = PHPickerFilter.imagesFilter
            selectionLimit = 1
        }
        val picker = PHPickerViewController(configuration = config)
        picker.delegate = galleryDelegate
        topController()?.presentViewController(picker, animated = true, completion = null)
    }

    override fun fromCamera() {
        if (!cameraAvailable) return onError("Câmera indisponível neste aparelho.")
        val picker = UIImagePickerController()
        picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        picker.delegate = cameraDelegate
        topController()?.presentViewController(picker, animated = true, completion = null)
    }

    /** Downscale (the API refuses files over 5 MB) and re-encode as JPEG. */
    private fun deliver(image: UIImage) {
        val (w, h) = image.size.useContents { width to height }
        val scale = minOf(1.0, MAX_COVER_SIDE / maxOf(w, h))
        val target = CGSizeMake(w * scale, h * scale)
        val resized = UIGraphicsImageRenderer(size = target).imageWithActions { _ ->
            image.drawInRect(CGRectMake(0.0, 0.0, w * scale, h * scale))
        }
        val data = UIImageJPEGRepresentation(resized, 0.85) ?: return onError("Não consegui abrir essa imagem.")
        onPicked(PickedImage(data.toByteArray()))
    }

    private fun topController(): UIViewController? {
        var controller = UIApplication.sharedApplication.keyWindow?.rootViewController
        while (controller?.presentedViewController != null) controller = controller.presentedViewController
        return controller
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray = ByteArray(length.toInt()).also { bytes ->
    if (bytes.isNotEmpty()) bytes.usePinned { memcpy(it.addressOf(0), this.bytes, length) }
}
