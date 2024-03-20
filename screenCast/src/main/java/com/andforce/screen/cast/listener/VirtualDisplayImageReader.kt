package com.andforce.screen.cast.listener

import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection

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
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 5)

        val flags =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        mediaProjection.createVirtualDisplay(
            "$TAG-display",
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
            kotlin.runCatching {
                val image = reader.acquireLatestImage()
                imageListener?.onImage(image)
            }.onFailure {
                it.printStackTrace()
            }
        }
    }
}