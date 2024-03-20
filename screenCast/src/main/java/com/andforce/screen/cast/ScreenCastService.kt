package com.andforce.screen.cast

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.andforce.screen.cast.coroutine.RecordViewModel
import org.koin.android.ext.android.inject


class ScreenCastService: Service() {
    private var mpm: MediaProjectionManager? = null
    private val recordViewModel: RecordViewModel by inject()

    companion object {
        const val NOTIFICATION_ID = 1
        // 启动方法
        fun startService(context: Context, isForeground: Boolean, data: Intent, code: Int) {
            val startIntent = Intent(context.applicationContext, ScreenCastService::class.java)
            startIntent.putExtra("data", data)
            startIntent.putExtra("code", code)
            if (isForeground) {
                context.applicationContext.startForegroundService(startIntent)
            } else {
                context.applicationContext.startService(startIntent)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        //startForeground(NOTIFICATION_ID, createNotification())
        Log.d("RecordViewModel", "RecordViewModel2: $recordViewModel")
        mpm = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?
    }

    override fun onDestroy() {
        super.onDestroy()
        recordViewModel.updateRecordState(RecordViewModel.RecordState.Stopped)
    }
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent == null) {
            return START_STICKY
        }

        // 获取intent中的数据
        val data = intent.getParcelableExtra<Intent>("data")
        val code = intent.getIntExtra("code", 0)
        if (data == null || code == 0) {
            Toast.makeText(this, "data or code is null", Toast.LENGTH_SHORT).show()
            return START_NOT_STICKY
        }

        mpm?.getMediaProjection(code, data)?.let { mp ->
            recordViewModel.startCaptureImages(this, mp, 0.35f)
            recordViewModel.updateRecordState(RecordViewModel.RecordState.Recording)
        }

        return START_STICKY
    }
    private fun createNotification(): Notification {
        val builder: Notification.Builder = Notification.Builder(this,
            createNotificationChannel("my_service", "My Background Service"))
        builder.setContentTitle("Recording Screen")
            .setContentText("Recording in progress")
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
}