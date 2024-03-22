package com.andforce.screen.cast.listener

import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class VirtualDisplayImageReader(
    private val mediaProjection: MediaProjection
) {

    companion object {
        const val TAG = "VirtualDisplayImageReader"
    }

    private var imageReader: ImageReader? = null
    private var imageListener: OnImageListener? = null

    private var statusListener: RecordStatusListener? = null

    fun start(width: Int, height: Int, dpi: Int) {
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)

        val flags =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        mediaProjection.createVirtualDisplay(
            "$TAG-${SystemClock.uptimeMillis()}-display",
            width, height, dpi, flags,
            imageReader!!.surface,
            object : VirtualDisplay.Callback() {
                override fun onPaused() {
                    super.onPaused()
                }

                    override fun onResumed() {
                        super.onResumed()
                        statusListener?.onStatus(RecordState.Recording)
                    }

                override fun onStopped() {
                    super.onStopped()
                    statusListener?.onStatus(RecordState.Recording)
                }
            },
            null
        )
    }

    private val mediaCallBack = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            statusListener?.onStatus(RecordState.Stopped)
            imageListener?.onFinished()
        }
    }

    fun registerStatusListener(listener: RecordStatusListener) {
        this.statusListener = listener
    }

    fun unregisterStatusListener() {
        this.statusListener = null
    }

    fun registerListener(imageListener: OnImageListener) {
        this.imageListener = imageListener
        mediaProjection.registerCallback(mediaCallBack, null)

        imageReader?.setOnImageAvailableListener(listener, null)
    }

    fun unregisterListener() {
        mediaProjection.unregisterCallback(mediaCallBack)
    }

    private val listener = ImageReader.OnImageAvailableListener { reader ->
        if (reader != null) {
            var image: android.media.Image? = null
            try {
                image = reader.acquireLatestImage()
                // ---------------------------
                val width = image.width
                val height = image.height

                val plane = image.planes[0]
                val buffer = plane.buffer
                val pixelStride = plane.pixelStride
                val rowStride = plane.rowStride
                val rowPadding = rowStride - pixelStride * width

                val orgBitmap = Bitmap.createBitmap(
                    width + rowPadding / pixelStride,
                    height,
                    Bitmap.Config.ARGB_8888
                ).apply {
                    copyPixelsFromBuffer(buffer)
                }

                val cropBitmap = Bitmap.createBitmap(orgBitmap, 0, 0, width, height).also {
                    if (!orgBitmap.isRecycled) {
                        orgBitmap.recycle()
                    }
                }
                val byteArrayOutputStream = ByteArrayOutputStream().also {
                    cropBitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
                }
                val byteArray = byteArrayOutputStream.toByteArray().also {
                    if (!cropBitmap.isRecycled) {
                        cropBitmap.recycle()
                    }
                    runCatching {
                        byteArrayOutputStream.close()
                    }
                }
                Log.i("CAPTURE", "cropBitmap, byteArray.size:${byteArray.size}")
                if (image != null) {
                    imageListener?.onImage(byteArray)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error on image available listener", e)
            } finally {
                try {
                    image?.close()
                } catch (e: Exception) {
                    Log.e(TAG, "Error on image close", e)
                }
            }
        }
    }
}