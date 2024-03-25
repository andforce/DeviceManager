package com.andforce.device.accessibility

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.andforce.commonutils.ScreenUtils
import com.andforce.injectevent.InjectEventHelper
import com.andforce.socket.mouseevent.MouseEvent
import com.andforce.socket.viewmodel.SocketEventViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SystemAutoTouchService: Service() {
    companion object {
        val TAG = "SystemAutoTouchService"
    }

    private val socketEventViewModel: SocketEventViewModel by inject()

    private var socketEventJob: Job? = null
    private var socketMoveEventJob: Job? = null

    private val injectEventHelper = InjectEventHelper.getInstance()
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")

        socketMoveEventJob = GlobalScope.launch {
            socketEventViewModel.mouseMoveEventFlow.collect() {
                if (!AutoTouchManager.isAccessibility) {
                    it?.let {
                        injectEvent(it)
                    }
                }
            }
        }

        socketEventJob = GlobalScope.launch {
            socketEventViewModel.mouseDownUpEventFlow.buffer(capacity = 1024).collect {
                if (!AutoTouchManager.isAccessibility) {
                    it?.let {
                        injectEvent(it)
                    }
                }
            }
        }
    }

    private fun injectEvent(it: MouseEvent) {
        Log.i(TAG, "collect MouseEvent: $it")

        val screenW = ScreenUtils.metrics(this@SystemAutoTouchService).widthPixels
        val screenH = ScreenUtils.metrics(this@SystemAutoTouchService).heightPixels

        val scaleW = screenW / it.remoteWidth.toFloat()
        val scaleH = screenH / it.remoteHeight.toFloat()
        val fromRealX = if (it.x * scaleW < 0) 0f else it.x * scaleW
        val fromRealY = if (it.y * scaleH < 0) 0f else it.y * scaleH

        when (it) {
            is MouseEvent.Down -> {
                injectEventHelper.injectTouchDownSystem(fromRealX, fromRealY)
            }

            is MouseEvent.Move -> {
                injectEventHelper.injectTouchMoveSystem(fromRealX, fromRealY)
            }

            is MouseEvent.Up -> {
                injectEventHelper.injectTouchUpSystem(fromRealX, fromRealY)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        socketEventJob?.cancel()
        socketMoveEventJob?.cancel()
        Log.d(TAG, "onStartCommand")
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}