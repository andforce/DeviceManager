package com.cry.screenop.coroutine

import android.content.Context
import android.media.Image
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.view.WindowManager
import com.cry.screenop.listener.OnImageListener
import com.cry.screenop.listener.VirtualDisplayImageReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow

// https://www.jianshu.com/p/281093cabbc7
// https://www.jianshu.com/p/e73863ae9ae9

class RecordDataSource {

    suspend fun captureImages(context: Context, mp:MediaProjection, scale: Float) = callbackFlow<Image> {

        val callback = object: OnImageListener {
            override fun onImage(image: Image) {
                trySendBlocking(image)
            }

            override fun onFinished() {
                channel.close()
            }
        }

        var windowManager:WindowManager? = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?

        if (windowManager == null) {
            cancel("WindowManager is null", CancellationException("WindowManager is null"))
            return@callbackFlow
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay?.getRealMetrics(metrics)

        val finalWidthPixels = (metrics.widthPixels * scale).toInt()
        val finalHeightPixels = (metrics.heightPixels * scale).toInt()

        val virtualDisplayImageReader = VirtualDisplayImageReader(mp).apply {
            start(finalWidthPixels, finalHeightPixels, metrics.densityDpi)
            registerListener(callback)
        }

        awaitClose {
            virtualDisplayImageReader.unregisterListener()
        }
    }
}