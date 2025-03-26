package com.andforce.socket

import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.andforce.device.applock.AppLauncherManager
import com.andforce.device.applock.AvoidList
import com.andforce.device.packagemanager.apps.PackageManagerViewModel
import com.andforce.network.download.DownloaderViewModel
import com.andforce.screen.cast.coroutine.ScreenCastViewModel
import com.andforce.service.coroutine.CoroutineService
import com.andforce.socket.viewmodel.SocketEventViewModel
import kotlinx.coroutines.launch
import okio.ByteString.Companion.encode
import org.koin.android.ext.android.inject
import java.net.URLEncoder

/**
 * 启动、关闭 ACTION_SOCKET_EVENT_SERVICE
 */
class SocketEventService: CoroutineService() {

    companion object {
        const val TAG = "SocketEventService"
    }

    private val socketEventViewModel: SocketEventViewModel by inject()
    private val downloaderViewModel: DownloaderViewModel by inject()
    private val screenCastViewModel: ScreenCastViewModel by inject()
    private val packageManagerViewModel: PackageManagerViewModel by inject()

    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            Log.d(TAG, "ConnectivityManager.NetworkCallback-onAvailable")

            // 网络可用时调用
            socketEventViewModel.connectIfNeed(socketUrl())
            socketEventViewModel.listenMouseEventFromSocket()
            socketEventViewModel.listenMouseMoveEventFromSocket()

            socketEventViewModel.listenApkFilePushEvent()
            socketEventViewModel.listenApkUninstallEvent()
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

    private fun socketUrl(): String {
        //val token = URLEncoder.encode("ggQHHDvivlWGLuiGbWxqG9wtv1R0PvJU0vjy2fBeWZMscRYVc35FUaySuZqC1+s6", "UTF-8")
        //return "http://10.66.39.29:3001/socket?token=$token"
        return "http://10.66.39.29:3001"
        //http://10.66.32.239:9112/socket?token=ggQHHDvivlWGLuiGbWxqG9wtv1R0PvJU0vjy2fBeWZMscRYVc35FUaySuZqC1+s6
    }

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

        serviceScope.launch {
            Log.d(TAG, "recordViewModel.capturedImage")
            screenCastViewModel.capturedImageFlow.collect {
                Log.i("CAPTURE", "start sendBitmapToServer")
                socketEventViewModel.sendBitmapToServer(it)
            }
        }

        serviceScope.launch {
            Log.d(TAG, "socketEventViewModel.apkFilePushEventFlow.collect")
            socketEventViewModel.apkFilePushEventFlow.collect {
                Log.d(TAG, "collect ApkEvent: $it")
                it?.let {
                    // TODO: 替换APK下载地址
                    //downloaderViewModel.downloadApk(applicationContext, BuildConfig.HOST + it.path)
                }
            }
        }

        serviceScope.launch {
            socketEventViewModel.apkUninstallEventFlow.collect {
                Log.d(TAG, "collect ApkUninstallEvent: $it")
                it?.let {
                    packageManagerViewModel.uninstallApp(applicationContext, it.packageName)
                }
            }
        }

        serviceScope.launch {
            Log.d(TAG, "downloaderViewModel.fileDownloadStateFlow.collect")
            downloaderViewModel.downloadStateFlow.collect {
                packageManagerViewModel.installApp(applicationContext, it)
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

        socketEventViewModel.connectIfNeed(socketUrl())
        socketEventViewModel.listenMouseEventFromSocket()
        socketEventViewModel.listenMouseMoveEventFromSocket()

        socketEventViewModel.listenApkFilePushEvent()
        socketEventViewModel.listenApkUninstallEvent()

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")

        connectivityManager.unregisterNetworkCallback(networkCallback)

        stopSystemAutoTouchService()

        socketEventViewModel.disconnect()
    }

    override fun onBind(intent: Intent?) = null
}