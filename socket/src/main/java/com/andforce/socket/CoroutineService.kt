package com.andforce.socket

import android.app.Service
import android.content.Intent
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

open class CoroutineService: Service() {

    val serviceScope = CoroutineScope(Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}