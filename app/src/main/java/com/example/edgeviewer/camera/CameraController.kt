package com.example.edgeviewer.camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.LifecycleOwner
import com.example.edgeviewer.nativebridge.NativeProcessor
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import kotlin.math.roundToInt

class CameraController(
    private val context: Context,
    private val onFrame: (width: Int, height: Int, rgba: ByteArray, fps: Double) -> Unit
) {
    private val executor = Executors.newSingleThreadExecutor()
    private var lastTs = 0L
    private var fps = 0.0

    fun start() {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()

            analysis.setAnalyzer(executor) { image ->
                try {
                    handleImage(image)
                } finally {
                    image.close()
                }
            }

            val selector = CameraSelector.DEFAULT_BACK_CAMERA
            val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())
            mainHandler.post {
                provider.unbindAll()
                provider.bindToLifecycle(context as LifecycleOwner, selector, analysis)
            }
        }, ContextCompatExecutor(context))
    }

    fun stop() {
    }

    private fun handleImage(image: ImageProxy) {
        val w = image.width
        val h = image.height
        val nv21 = yuv420ToNv21(image)
        val rgba = ByteArray(w * h * 4)

        NativeProcessor.processNV21ToRGBA(nv21, w, h, rgba)

        val now = System.nanoTime()
        if (lastTs != 0L) {
            val dt = (now - lastTs) / 1e9
            fps = 1.0 / dt
        }
        lastTs = now

        onFrame(w, h, rgba, fps)
    }

    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)

        val chromaRowStride = image.planes[1].rowStride
        val chromaPixelStride = image.planes[1].pixelStride

        val offset = ySize
        val width = image.width
        val height = image.height
        var outputOffset = offset

        val vRowStride = image.planes[2].rowStride
        val vPixelStride = image.planes[2].pixelStride
        val uRowStride = image.planes[1].rowStride
        val uPixelStride = image.planes[1].pixelStride

        val vBufferDup = image.planes[2].buffer.duplicate()
        val uBufferDup = image.planes[1].buffer.duplicate()

        for (row in 0 until height / 2) {
            var vCol = row * vRowStride
            var uCol = row * uRowStride
            for (col in 0 until width / 2) {
                nv21[outputOffset++] = vBufferDup.get(vCol)
                nv21[outputOffset++] = uBufferDup.get(uCol)
                vCol += vPixelStride
                uCol += uPixelStride
            }
        }

        return nv21
    }
}

private class ContextCompatExecutor(context: Context) : java.util.concurrent.Executor {
    private val handlerThread = HandlerThread("camera-exec").apply { start() }
    private val handler = Handler(handlerThread.looper)
    override fun execute(command: Runnable) {
        handler.post(command)
    }
}


