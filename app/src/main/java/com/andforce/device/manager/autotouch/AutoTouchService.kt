package com.andforce.device.manager.autotouch

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.andforce.injectevent.InjectEventHelper
import com.andforce.device.manager.utils.ScreenUtils
import com.andforce.socket.SocketViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class AutoTouchService : AccessibilityService() {

    private val socketViewModel: SocketViewModel by inject()

    private var job: Job? = null

    private val injectEventHelper = InjectEventHelper()
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        job = GlobalScope.launch {

            socketViewModel.eventFlow.buffer(capacity = 1024).collect {

                it?.let {
                    Log.d("AutoTouchService", "collect MouseEvent: $it")

                    val screenW = ScreenUtils.metrics(this@AutoTouchService).widthPixels
                    val screenH = ScreenUtils.metrics(this@AutoTouchService).heightPixels

                    val scaleW = screenW / it.remoteWidth.toFloat()
                    val scaleH = screenH / it.remoteHeight.toFloat()
                    val fromRealX = if (it.x * scaleW < 0) 0f else it.x * scaleW
                    val fromRealY = if (it.y * scaleH < 0) 0f else it.y * scaleH

                    when (it) {
                        is com.andforce.socket.MouseEvent.Down -> {
                            injectEventHelper.injectTouchDown(this@AutoTouchService,screenW,screenH,fromRealX,fromRealY)
                        }
                        is com.andforce.socket.MouseEvent.Move -> {
                            injectEventHelper.injectTouchMove(this@AutoTouchService,screenW,screenH,fromRealX,fromRealY)
                        }
                        is com.andforce.socket.MouseEvent.Up -> {
                            injectEventHelper.injectTouchUp(this@AutoTouchService,screenW,screenH,fromRealX,fromRealY)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //Log.d("AutoTouchService", "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        //Log.d("AutoTouchService", "onInterrupt")
    }
}