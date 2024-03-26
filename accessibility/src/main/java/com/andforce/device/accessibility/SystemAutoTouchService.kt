package com.andforce.device.accessibility

import android.content.Intent
import android.util.Log
import com.andforce.commonutils.ScreenUtils
import com.andforce.injectevent.InjectEventHelper
import com.andforce.service.coroutine.CoroutineService
import com.andforce.socket.mouseevent.MouseEvent
import com.andforce.socket.viewmodel.SocketEventViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SystemAutoTouchService: CoroutineService() {
    companion object {
        const val TAG = "SystemAutoTouchService"
    }

    private val socketEventViewModel: SocketEventViewModel by inject()

    private val injectEventHelper = InjectEventHelper.getInstance()

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate")

        serviceScope.launch {
            socketEventViewModel.mouseMoveEventFlow.collect {
                if (!AutoTouchManager.isAccessibility) {
                    it?.let {
                        injectEvent(it)
                    }
                }
            }
        }

        serviceScope.launch {
            socketEventViewModel.mouseDownUpEventFlow.collect {
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
        Log.d(TAG, "onStartCommand")
    }
}