package com.andforce.device.accessibility

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.andforce.commonutils.ScreenUtils
import com.andforce.injectevent.InjectEventHelper
import com.andforce.socket.SocketEventViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SystemAutoTouchService: Service() {
    private val socketEventViewModel: SocketEventViewModel by inject()

    private var socketEventJob: Job? = null

    private val injectEventHelper = InjectEventHelper.getInstance()
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, SystemAutoTouchService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, SystemAutoTouchService::class.java)
            context.stopService(intent)
        }
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        socketEventJob = GlobalScope.launch {

            socketEventViewModel.eventFlow.buffer(capacity = 1024).collect {

                it?.let {
                    Log.d("AutoTouchService", "collect MouseEvent: $it")

                    val screenW = ScreenUtils.metrics(this@SystemAutoTouchService).widthPixels
                    val screenH = ScreenUtils.metrics(this@SystemAutoTouchService).heightPixels

                    val scaleW = screenW / it.remoteWidth.toFloat()
                    val scaleH = screenH / it.remoteHeight.toFloat()
                    val fromRealX = if (it.x * scaleW < 0) 0f else it.x * scaleW
                    val fromRealY = if (it.y * scaleH < 0) 0f else it.y * scaleH

                    when (it) {
                        is com.andforce.socket.MouseEvent.Down -> {
                            injectEventHelper.injectTouchDownSystem(fromRealX,fromRealY)
                        }
                        is com.andforce.socket.MouseEvent.Move -> {
                            injectEventHelper.injectTouchMoveSystem(fromRealX,fromRealY)
                        }
                        is com.andforce.socket.MouseEvent.Up -> {
                            injectEventHelper.injectTouchUpSystem(fromRealX,fromRealY)
                        }
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    override fun onDestroy() {
        super.onDestroy()
        socketEventJob?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}