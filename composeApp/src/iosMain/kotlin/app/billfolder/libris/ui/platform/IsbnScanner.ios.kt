package app.billfolder.libris.ui.platform

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGRectZero
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue
import platform.darwin.dispatch_get_main_queue
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun IsbnScanner(onScanned: (String) -> Unit, onClose: () -> Unit) {
    var access by remember { mutableStateOf<Boolean?>(null) }
    LaunchedEffect(Unit) {
        when (AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> access = true
            AVAuthorizationStatusNotDetermined -> AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { ok ->
                dispatch_async(dispatch_get_main_queue()) { access = ok }
            }
            else -> access = false
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        when (access) {
            true -> {
                UIKitView(
                    factory = { ScannerView(onScanned).also { it.start() } },
                    modifier = Modifier.fillMaxSize(),
                    onRelease = { it.stop() },
                )
                Text("Aponte para o código de barras do livro", color = Color.White, modifier = Modifier.align(Alignment.BottomCenter).padding(32.dp))
            }
            false -> Text(
                "Sem acesso à câmera. Libere em Ajustes › Libris ou digite o ISBN.",
                color = Color.White,
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
            )
            null -> Unit
        }
        Text("✕ Fechar", color = Color.White, modifier = Modifier.statusBarsPadding().padding(16.dp).clickable(onClick = onClose).padding(8.dp))
    }
}

/** Camera preview plus an EAN-13 reader; reports the first book barcode once. */
@OptIn(ExperimentalForeignApi::class)
private class ScannerView(private val onScanned: (String) -> Unit) : UIView(frame = CGRectZero.readValue()) {
    private val session = AVCaptureSession()
    private val preview = AVCaptureVideoPreviewLayer(session = session).apply { videoGravity = AVLayerVideoGravityResizeAspectFill }
    private var reported = false

    private val delegate = object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
        override fun captureOutput(output: AVCaptureOutput, didOutputMetadataObjects: List<*>, fromConnection: AVCaptureConnection) {
            if (reported) return
            val code = didOutputMetadataObjects.filterIsInstance<AVMetadataMachineReadableCodeObject>()
                .firstNotNullOfOrNull { it.stringValue?.takeIf(::looksLikeIsbn) } ?: return
            reported = true
            onScanned(code)
        }
    }

    init {
        layer.addSublayer(preview)
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo)
        val input = device?.let { AVCaptureDeviceInput.deviceInputWithDevice(it, null) }
        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
            val output = AVCaptureMetadataOutput()
            if (session.canAddOutput(output)) {
                session.addOutput(output)
                output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
                output.metadataObjectTypes = listOf(AVMetadataObjectTypeEAN13Code)
            }
        }
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        preview.frame = bounds
        CATransaction.commit()
    }

    // startRunning blocks, so it stays off the main thread.
    fun start() = dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) { session.startRunning() }

    fun stop() = dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) { session.stopRunning() }
}

