package com.cry.screenop.listener

import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
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

    fun start(width: Int, height: Int, dpi: Int) {
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 5)

        val flags =
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
        mediaProjection.createVirtualDisplay(
            "$TAG-display",
            width, height, dpi, flags,
            imageReader!!.surface, null, null
        )
    }

    private val mediaCallBack = object : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            imageListener?.onFinished()
        }
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