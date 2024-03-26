package com.andforce.screen.cast

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import com.andforce.screen.cast.coroutine.ScreenCastViewModel
import com.andforce.service.coroutine.CoroutineService
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class ScreenCastService: CoroutineService() {
    private var mpm: MediaProjectionManager? = null
    private var mp: MediaProjection? = null

    private val screenCastViewModel: ScreenCastViewModel by inject()

    companion object {
        const val TAG = "ScreenCastService"

        const val NOTIFICATION_ID = 1
        // 启动方法
        fun startService(context: Context, data: Intent, code: Int) {
            val startIntent = Intent(context.applicationContext, ScreenCastService::class.java)
            startIntent.putExtra("data", data)
            startIntent.putExtra("code", code)
            context.applicationContext.startForegroundService(startIntent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context.applicationContext, ScreenCastService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()

        startForeground(NOTIFICATION_ID, createNotification())
        Log.d(TAG, "onCreate")
        mpm = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            Log.d(TAG, "onStartCommand, intent is null, return")
            return START_STICKY
        }

        // 获取intent中的数据
        val data = intent.getParcelableExtra<Intent>("data")
        val code = intent.getIntExtra("code", 0)
        if (data == null || code != Activity.RESULT_OK) {
            Log.d(TAG, "onStartCommand, data == null, code != Activity.RESULT_OK")
            return START_NOT_STICKY
        }

        if (mpm == null) {
            Log.d(TAG, "onStartCommand, mpm is null")
            return START_NOT_STICKY
        } else {
            Log.d(TAG, "onStartCommand, code:$code, data:$data")

            mp = mpm!!.getMediaProjection(code, data)
            if (mp == null) {
                Log.d(TAG, "onStartCommand, mediaProjection is null")
            } else {
                serviceScope.launch {
                    screenCastViewModel.startCollectCaptureStatue()
                }
                serviceScope.launch {
                    screenCastViewModel.startCaptureImages(this@ScreenCastService, mp!!, 0.35f)
                }
                Log.d(TAG, "onStartCommand, mediaProjection is $mp")
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        mp?.stop()
    }

    private fun createNotification(): Notification {
        val builder = Notification.Builder(
            this,
            createNotificationChannel("my_service", "My Background Service")
        )
        val intent = Intent(this, ScreenCastActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentTitle("Recording Screen")
            .setContentText("Recording in progress")
            // intent to open an Activity
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
        return builder.build()
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String {
        val chan = NotificationChannel(
            channelId,
            channelName, NotificationManager.IMPORTANCE_NONE
        )
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    override fun onBind(intent: Intent?) = null
}