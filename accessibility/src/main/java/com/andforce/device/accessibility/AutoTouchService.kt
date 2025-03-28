package com.andforce.device.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
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

class AutoTouchService : AccessibilityService() {

    private val socketEventViewModel: SocketEventViewModel by inject()

    private var socketEventJob: Job? = null
    private var socketMoveEventJob: Job? = null

    private val injectEventHelper = InjectEventHelper.getInstance()
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()
        AutoTouchManager.isAccessibility = true

        socketMoveEventJob = GlobalScope.launch {
            socketEventViewModel.mouseMoveEventFlow.collect() {
                it?.let {
                    Log.d("AutoTouchService", "collect MouseMoveEvent: $it")
                    injectEvent(it)
                }
            }
        }
        socketEventJob = GlobalScope.launch {

            socketEventViewModel.mouseDownUpEventFlow.buffer(capacity = 1024).collect {

                it?.let {
                    Log.d("AutoTouchService", "collect MouseEvent: $it")

                    injectEvent(it)
                }
            }
        }
    }

    private fun injectEvent(it: MouseEvent) {
        val screenW = ScreenUtils.metrics(this@AutoTouchService).widthPixels
        val screenH = ScreenUtils.metrics(this@AutoTouchService).heightPixels

        val scaleW = screenW / it.remoteWidth.toFloat()
        val scaleH = screenH / it.remoteHeight.toFloat()
        val fromRealX = if (it.x * scaleW < 0) 0f else it.x * scaleW
        val fromRealY = if (it.y * scaleH < 0) 0f else it.y * scaleH

        when (it) {
            is MouseEvent.Down -> {
                injectEventHelper.injectTouchDown(
                    this@AutoTouchService,
                    screenW,
                    screenH,
                    fromRealX,
                    fromRealY
                )
            }

            is MouseEvent.Move -> {
                injectEventHelper.injectTouchMove(
                    this@AutoTouchService,
                    screenW,
                    screenH,
                    fromRealX,
                    fromRealY
                )
            }

            is MouseEvent.Up -> {
                injectEventHelper.injectTouchUp(
                    this@AutoTouchService,
                    screenW,
                    screenH,
                    fromRealX,
                    fromRealY
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        socketEventJob?.cancel()
        socketMoveEventJob?.cancel()
        AutoTouchManager.isAccessibility = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        AutoTouchManager.isAccessibility = true
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //Log.d("AutoTouchService", "onAccessibilityEvent: $event")
    }

    override fun onInterrupt() {
        //Log.d("AutoTouchService", "onInterrupt")
    }
}