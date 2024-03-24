package com.andforce.screen.cast.coroutine

import android.content.Context
import android.media.projection.MediaProjection
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.andforce.screen.cast.listener.OnImageListener
import com.andforce.screen.cast.listener.RecordState
import com.andforce.screen.cast.listener.RecordStatusListener
import com.andforce.screen.cast.listener.VirtualDisplayImageReader
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext

// https://www.jianshu.com/p/281093cabbc7
// https://www.jianshu.com/p/e73863ae9ae9

class ScreenCastDataSource {
    private var virtualDisplayImageReader: VirtualDisplayImageReader? = null

    suspend fun recordStatusFlow() = callbackFlow {

        val callback = object : RecordStatusListener {
            override fun onStatus(status: RecordState) {
                trySend(status)
            }
        }

        val virtualDisplayImageReader = virtualDisplayImageReader?.apply {
            registerStatusListener(callback)
        }

        awaitClose {
            virtualDisplayImageReader?.unregisterStatusListener()
        }
    }

    suspend fun captureImageFlow(context: Context, mp: MediaProjection, scale: Float) = callbackFlow {

        if (virtualDisplayImageReader == null) {
            virtualDisplayImageReader = VirtualDisplayImageReader(mp)
        }

        val callback = object : OnImageListener {
            override fun onImage(image: ByteArray?) {
                image?.let {
                    val result = trySend(image)
                    Log.i("CAPTURE", "onImage(), trySend, result:${result.isSuccess}")
                }
            }

            override fun onFinished() {
                channel.close()
                Log.i("CAPTURE", "onFinished()")
            }
        }

        val windowManager: WindowManager? =
            context.getSystemService(Context.WINDOW_SERVICE) as WindowManager?

        if (windowManager == null) {
            cancel("WindowManager is null", CancellationException("WindowManager is null"))
            return@callbackFlow
        }

        val metrics = DisplayMetrics()
        windowManager.defaultDisplay?.getRealMetrics(metrics)

        val finalWidthPixels = (metrics.widthPixels * scale).toInt()
        val finalHeightPixels = (metrics.heightPixels * scale).toInt()

        withContext(Dispatchers.Main) {
            virtualDisplayImageReader?.apply {
                start(finalWidthPixels, finalHeightPixels, metrics.densityDpi)
                registerListener(callback)
            }
        }

        awaitClose {
            Log.i("CAPTURE", "awaitClose, unregisterListener()")
            virtualDisplayImageReader?.unregisterListener()
        }
    }
}