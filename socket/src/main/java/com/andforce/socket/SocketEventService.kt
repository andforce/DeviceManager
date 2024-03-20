package com.andforce.socket

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.andforce.device.packagemanager.PackageManagerHelper
import com.andforce.network.NetworkViewModel
import com.andforce.screen.cast.coroutine.RecordViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.ByteArrayOutputStream

class SocketEventService: Service() {

    private val socketEventViewModel: SocketEventViewModel by inject()
    private val downloaderViewModel: NetworkViewModel by inject()
    private val recordViewModel: RecordViewModel by inject()

    private var capturedImageJob: Job? = null
    private var apkEventJob: Job? = null
    private var apkDownloadJob: Job? = null

    private val mainScope = MainScope()

    private var socketClient: SocketClient? = null
    private var socketUrl: String? = null

    private val connectivityManager: ConnectivityManager by lazy {
        getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    override fun onBind(intent: Intent?): IBinder? {

        return null
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            // 网络可用时调用
            connectIfNeed().also {
                socketClient?.let {
                    socketEventViewModel.listenEvent(it)
                    socketEventViewModel.listenApkEvent(it)
                }
            }
        }

        override fun onLost(network: Network) {
            // 网络丢失时调用
            disconnect()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null
    }

    override fun onCreate() {
        super.onCreate()
        val networkRequest: NetworkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }

        capturedImageJob =  mainScope.launch(handler) {
            recordViewModel.capturedImage.collect {
                it?.let { bitmap->
                    withContext(Dispatchers.IO) {
                        if (socketClient?.isConnected() == false) {
                            return@withContext
                        }

                        val byteArrayOutputStream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                        val byteArray = byteArrayOutputStream.toByteArray()

                        socketClient?.send(byteArray)
                        runCatching {
                            byteArrayOutputStream.close()
                        }
                        if (bitmap.isRecycled.not()) {
                            bitmap.recycle()
                        }
                    }
                }
            }
        }

        apkEventJob = mainScope.launch {
            socketEventViewModel.apkEventFlow.collect {
                Log.d("CastService", "collect ApkEvent: $it")
                it?.let {
                    downloaderViewModel.downloadApk(applicationContext, it.name, it.path)
                }
            }
        }

        apkDownloadJob = mainScope.launch {
            downloaderViewModel.fileDownloadStateFlow.collect {
                Log.d("CastService", "start install : $it")
                val helper = PackageManagerHelper(applicationContext)
                helper.registerListener { actionType, success ->
                    if (actionType == PackageManagerHelper.ACTION_TYPE_INSTALL) {
                        if (success) {
                            Toast.makeText(applicationContext, "install apk success", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(applicationContext, "install apk failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                    Log.d("CastService", "install apk success: $success")
                }
                PackageManagerHelper(applicationContext).installPackage(it)
                Toast.makeText(applicationContext, "start install : $it", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun connectIfNeed() {
        if (socketUrl == null || !isNetworkAvailable()) {
            return
        }

        if (socketClient == null) {
            socketClient = SocketClient(socketUrl!!)
        }

        if (socketClient?.isConnected() == true) {
            return
        }

        if (isNetworkAvailable()) {
            socketClient?.startConnection()
        }
        startSystemAutoTouchService()
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

    private fun disconnect() {
        socketClient?.release()
        socketClient = null
        stopSystemAutoTouchService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val socketUrl = intent.getStringExtra("socketUrl")
            this.socketUrl = socketUrl

            connectIfNeed().also {
                socketClient?.let {
                    socketEventViewModel.listenEvent(it)
                    socketEventViewModel.listenApkEvent(it)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
        socketClient?.release()

        capturedImageJob?.cancel()
        apkEventJob?.cancel()
        apkDownloadJob?.cancel()
    }
}