package com.andforce.socket

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.andforce.device.applock.AppLauncherManager
import com.andforce.device.applock.AvoidList
import com.andforce.device.packagemanager.PackageManagerHelper
import com.andforce.network.NetworkViewModel
import com.andforce.screen.cast.coroutine.RecordViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * 启动、关闭 ACTION_SOCKET_EVENT_SERVICE
 */
class SocketEventService: Service() {

    companion object {
        const val TAG = "SocketEventService"
    }

    private val socketEventViewModel: SocketEventViewModel by inject()
    private val downloaderViewModel: NetworkViewModel by inject()
    private val recordViewModel: RecordViewModel by inject()

    private var capturedImageJob: Job? = null
    private var apkPushEventJob: Job? = null
    private var apkDownloadJob: Job? = null

    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "ConnectivityManager.NetworkCallback-onAvailable")

            // 网络可用时调用
            socketEventViewModel.connectIfNeed()
            socketEventViewModel.listenMouseEventFromSocket()
            socketEventViewModel.listenApkFilePushEvent()
            socketEventViewModel.listenSocketStatus()

            // 开启自动触摸服务
            startSystemAutoTouchService()
        }

        override fun onLost(network: Network) {
            Log.d(TAG, "ConnectivityManager.NetworkCallback-onLost")

            socketEventViewModel.disconnect()
            // 停止自动触摸服务
            stopSystemAutoTouchService()
        }
    }

//    private fun isNetworkAvailable(): Boolean {
//        val network = connectivityManager.activeNetwork
//        val capabilities = connectivityManager.getNetworkCapabilities(network)
//        return capabilities != null
//    }

    fun isBrowserApp(context: Context, packageName: String): Boolean {
        val packageManager = context.packageManager
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (info in activities) {
            if (info.activityInfo.packageName == packageName) {
                return true
            }
        }
        return false
    }
    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate() {
        super.onCreate()

        AppLauncherManager.startListener(object : AppLauncherManager.Action {
            override fun allowLaunch(intent: Intent, pkg: String): Boolean {

                if (!AvoidList.list.contains(pkg)) {
                    val intentLock = Intent("LOCK_ACTIVITY")
                    intentLock.`package` = packageName
                    intentLock.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                    applicationContext.startActivity(intentLock)
                    return false
                }
                return true
            }
        })

        Log.d(TAG, "onCreate")

        val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
        Log.d(TAG, "registerNetworkCallback")

        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }

        capturedImageJob = GlobalScope.launch(handler) {
            Log.d(TAG, "recordViewModel.capturedImage")
            recordViewModel.capturedImageFlow.collect {
                socketEventViewModel.sendBitmapToServer(it)
            }
        }

        apkPushEventJob = GlobalScope.launch {
            Log.d(TAG, "socketEventViewModel.apkFilePushEventFlow.collect")
            socketEventViewModel.apkFilePushEventFlow.collect {
                Log.d(TAG, "collect ApkEvent: $it")
                it?.let {
                    downloaderViewModel.downloadApk(applicationContext, it.name, it.path)
                }
            }
        }

        apkDownloadJob = GlobalScope.launch {
            Log.d(TAG, "downloaderViewModel.fileDownloadStateFlow.collect")
            downloaderViewModel.fileDownloadStateFlow.collect {
                val helper = PackageManagerHelper(applicationContext)
                helper.registerListener { actionType, success ->
                    if (actionType == PackageManagerHelper.ACTION_TYPE_INSTALL) {
                        if (success) {
                            Toast.makeText(applicationContext, "install apk success", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "install apk failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.d(TAG, "install apk success: $success")
                }
                helper.installPackage(it)
            }
        }
    }

    private fun startSystemAutoTouchService() {
        val intent = Intent()
        intent.action = "com.andforce.SystemAutoTouchService"
        intent.`package` = packageName
        startService(intent)
    }

    private fun stopSystemAutoTouchService() {
        val intent = Intent()
        intent.action = "com.andforce.SystemAutoTouchService"
        intent.`package` = packageName
        stopService(intent)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")

        socketEventViewModel.connectIfNeed()
        socketEventViewModel.listenMouseEventFromSocket()
        socketEventViewModel.listenApkFilePushEvent()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        connectivityManager.unregisterNetworkCallback(networkCallback)

        stopSystemAutoTouchService()

        socketEventViewModel.disconnect()

        capturedImageJob?.cancel()
        apkPushEventJob?.cancel()
        apkDownloadJob?.cancel()
    }

    override fun onBind(intent: Intent?) = null
}