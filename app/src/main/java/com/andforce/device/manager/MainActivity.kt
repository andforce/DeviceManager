package com.andforce.device.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andforce.device.manager.apps.AppInfoViewModel
import com.andforce.device.manager.apps.AppInfo
import com.andforce.device.manager.databinding.ActivityMainBinding
import com.andforce.device.packagemanager.apps.AppBean
import com.andforce.device.packagemanager.apps.InstalledAppAdapter
import com.andforce.device.packagemanager.apps.OnUninstallClickListener
import com.andforce.device.packagemanager.apps.PackageManagerViewModel
import com.andforce.network.download.DownloaderViewModel
import com.andforce.screen.cast.MediaProjectionRequestViewModel
import com.andforce.screen.cast.ScreenCastService
import com.andforce.screen.cast.coroutine.ScreenCastViewModel
import com.andforce.screen.cast.listener.RecordState
import com.andforce.socket.mouseevent.MouseEvent
import com.andforce.socket.viewmodel.SocketEventViewModel
import com.andforce.socket.mouseevent.SocketStatusListener
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private val mediaProjectionRequestViewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MediaProjectionRequestViewModel(this@MainActivity) as T
            }
        })[MediaProjectionRequestViewModel::class.java]
    }

    private val viewBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val screenCastViewModel: ScreenCastViewModel by inject()
    private val socketEventViewModel: SocketEventViewModel by inject()
    private val packageManagerViewModel: PackageManagerViewModel by inject()
    private val appInfoViewModel: AppInfoViewModel by inject()
    private val downloaderViewModel: DownloaderViewModel by inject()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)

        downloaderViewModel.apply {
            downloadProcessFlow.observe(this@MainActivity) {
                viewBinding.apkInfoDownloadProgress.text = "APK下载进度: ${it * 100}%"
            }
            downloadErrorFlow.asLiveData().observe(this@MainActivity) {
                it?.let {
                    viewBinding.apkInfo.text = "APK下载失败: ${it.code} ,${it.message}"
                }
            }
        }

        viewBinding.btnLoadApps.setOnClickListener {
            lifecycleScope.launch {
                packageManagerViewModel.loadInstalledApps(applicationContext)
            }
        }

        // 启动Socket
        val intent = Intent("ACTION_SOCKET_EVENT_SERVICE").apply {
            setPackage(packageName)
        }
        startService(intent)

        val installedAppAdapter = InstalledAppAdapter(this.applicationContext)
        viewBinding.rvList.layoutManager = LinearLayoutManager(this)
        // 设置上下间隔
        viewBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: android.graphics.Rect,
                view: android.view.View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                outRect.top = 10
                outRect.bottom = 10
            }
        })
        viewBinding.rvList.adapter = installedAppAdapter.also {
            it.setOnUninstallClickListener(object : OnUninstallClickListener {
                override fun onUninstallClick(appBean: AppBean) {
                    lifecycleScope.launch {
                        packageManagerViewModel.uninstallApp(applicationContext, appBean.packageName)
                    }
                }
            })
        }

        packageManagerViewModel.appUninstallResult.observe(this) {
            lifecycleScope.launch {
                packageManagerViewModel.loadInstalledApps(applicationContext)
            }
        }
        packageManagerViewModel.appInstallResult.observe(this) {
            lifecycleScope.launch {
                packageManagerViewModel.loadInstalledApps(applicationContext)
            }
        }
        // Load本机应用
        packageManagerViewModel.appLoadedList.observe(this) { it ->
            installedAppAdapter.setData(it)
            val appInfo = it.filter { !it.isSystem }.map { appBean ->
                AppInfo(
                    appBean.appName,
                    appBean.packageName
                )
            }
            lifecycleScope.launch {
                appInfoViewModel.uploadAppInfoList(appInfo)
            }
        }

        lifecycleScope.launch {
            packageManagerViewModel.loadInstalledApps(applicationContext)
        }

        mediaProjectionRequestViewModel.permissionResult.observe(this) {
            when (it) {
                is MediaProjectionRequestViewModel.Result.Success -> {
                    ScreenCastService.startService(this, it.data, it.resultCode)
                }

                MediaProjectionRequestViewModel.Result.PermissionDenied -> {
                    Toast.makeText(this, "User did not grant permission", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewBinding.btnStart.setOnClickListener {
            if (screenCastViewModel.recordState.value is RecordState.Recording) {
                Toast.makeText(this, "Recording, no need start", Toast.LENGTH_SHORT).show()
            } else {
                mediaProjectionRequestViewModel.requestScreenCapturePermission()
            }
        }

        screenCastViewModel.recordState.observe(this@MainActivity) {
            when (it) {
                is RecordState.Recording -> {
                    viewBinding.btnStart.text = "结束投屏"
                    viewBinding.castInfo.text = "投屏正在进行"
                }

                is RecordState.Stopped -> {
                    viewBinding.btnStart.text = "投屏开始"
                    viewBinding.castInfo.text = "投屏结束"
                    ScreenCastService.stopService(this@MainActivity)
                }
            }
        }

        viewBinding.btnSocket.setOnClickListener {
            // 启动Socket
            if (socketEventViewModel.socketStatusLiveData.value == SocketStatusListener.SocketStatus.DISCONNECTED) {
                startService(intent)
            } else {
                stopService(intent)
            }
        }

        socketEventViewModel.socketStatusLiveData.observe(this@MainActivity) {
            when (it) {
                SocketStatusListener.SocketStatus.CONNECTING -> {
                    viewBinding.socketStatus.text = "Socket:CONNECTING"
                    viewBinding.btnSocket.apply {
                        isEnabled = false
                        text = "连接中..."
                    }
                }

                SocketStatusListener.SocketStatus.CONNECTED -> {
                    viewBinding.socketStatus.text = "Socket:CONNECTED"
                    viewBinding.btnSocket.apply {
                        isEnabled = true
                        text = "断开Socket"
                    }
                }

                SocketStatusListener.SocketStatus.DISCONNECTED -> {
                    viewBinding.socketStatus.text = "Socket:DISCONNECTED"
                    viewBinding.btnSocket.apply {
                        isEnabled = true
                        text = "连接Socket"
                    }
                }

                SocketStatusListener.SocketStatus.CONNECT_ERROR -> {
                    viewBinding.socketStatus.text = "Socket:CONNECT_ERROR"
                    viewBinding.btnSocket.apply {
                        isEnabled = true
                        text = "重试Socket"
                    }
                }
            }
        }

        socketEventViewModel.apkFilePushEventFlow.asLiveData().observe(this@MainActivity) {
            it?.let {
                viewBinding.apkInfo.text = "APK事件: ${it.path}"
            }
        }

        lifecycleScope.launch {
            merge(
                socketEventViewModel.mouseMoveEventFlow,
                socketEventViewModel.mouseDownUpEventFlow
            ).collect {
                updateMouseEventInfo(it)
            }
        }
    }

    private fun updateMouseEventInfo(it: MouseEvent?) {
        it?.let {
            when (it) {
                is MouseEvent.Down -> {
                    viewBinding.mouseEvent.text = "鼠标事件: MouseDown"
                }

                is MouseEvent.Move -> {
                    viewBinding.mouseEvent.text = "鼠标事件: MouseMove"
                }

                is MouseEvent.Up -> {
                    viewBinding.mouseEvent.text = "鼠标事件: MouseUp"
                }
            }
        }
    }
}
