package com.andforce.device.manager

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andforce.device.manager.databinding.ActivityMainBinding
import com.andforce.socket.MouseEvent
import com.andforce.device.packagemanager.apps.AppBean
import com.andforce.device.packagemanager.apps.InstalledAppAdapter
import com.andforce.device.packagemanager.apps.OnUninstallClickListener
import com.andforce.device.packagemanager.apps.PackageManagerViewModel
import com.andforce.screen.cast.MediaProjectionRequestViewModel
import com.andforce.screen.cast.ScreenCastService
import com.andforce.screen.cast.coroutine.RecordViewModel
import com.andforce.screen.cast.listener.RecordState
import com.andforce.socket.SocketEventViewModel
import com.andforce.socket.SocketStatusListener
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MediaProjectionRequestViewModel(this@MainActivity) as T
            }
        })[MediaProjectionRequestViewModel::class.java]
    }

    private val viewMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val recordViewModel: RecordViewModel by inject()
    private val socketEventViewModel: SocketEventViewModel by inject()
    private val packageManagerViewModel: PackageManagerViewModel by inject()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewMainBinding.root)

        // 启动Socket
        val intent = Intent("ACTION_SOCKET_EVENT_SERVICE").apply {
            setPackage(packageName)
        }
        startService(intent)

        val adapter = InstalledAppAdapter(this.applicationContext)
        viewMainBinding.rvList.layoutManager = LinearLayoutManager(this)
        // 设置上下间隔
        viewMainBinding.rvList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: android.graphics.Rect, view: android.view.View, parent: RecyclerView, state: RecyclerView.State) {
                outRect.top = 10
                outRect.bottom = 10
            }
        })
        viewMainBinding.rvList.adapter = adapter.also {
            it.setOnUninstallClickListener(object : OnUninstallClickListener {
                override fun onUninstallClick(appBean: AppBean) {
                    packageManagerViewModel.uninstallApp(applicationContext, appBean)
                }
            })
        }

        packageManagerViewModel.installedApps.observe(this) {
            adapter.setData(it)
        }

        packageManagerViewModel.loadInstalledApps(this.applicationContext)

        Log.d("RecordViewModel", "RecordViewModel1: $recordViewModel")

        recordViewModel.recordState.observe(this@MainActivity) {
            when (it) {
                is RecordState.Recording -> {
                    viewMainBinding.btnStart.text = "Recording"
                }
                is RecordState.Stopped -> {
                    viewMainBinding.btnStart.text = "Stopped"
                    ScreenCastService.stopService(this@MainActivity)
                }
            }
        }

        viewModel.result.observe(this) {
            when (it) {
                is MediaProjectionRequestViewModel.Result.Success -> {
                    ScreenCastService.startService(this, false, it.data, it.resultCode)
                }
                MediaProjectionRequestViewModel.Result.PermissionDenied -> {
                    Toast.makeText(this, "User did not grant permission", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewMainBinding.btnStart.setOnClickListener {
            if (recordViewModel.recordState.value is RecordState.Recording) {
                Toast.makeText(this, "Recording, no need start", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.requestScreenCapturePermission()
            }
        }

        viewMainBinding.btnSocket.setOnClickListener {
            // 启动Socket
            if (socketEventViewModel.socketStatusLiveData.value == SocketStatusListener.SocketStatus.DISCONNECTED) {
                startService(intent)
            } else {
                stopService(intent)
            }
        }

        socketEventViewModel.socketStatusLiveData.observe(this@MainActivity) {
            when(it) {
                SocketStatusListener.SocketStatus.CONNECTING -> {
                    viewMainBinding.socketStatus.text = "Socket:CONNECTING"
                    viewMainBinding.btnSocket.apply {
                        isEnabled = false
                        text = "连接中..."
                    }
                }
                SocketStatusListener.SocketStatus.CONNECTED -> {
                    viewMainBinding.socketStatus.text = "Socket:CONNECTED"
                    viewMainBinding.btnSocket.apply {
                        isEnabled = true
                        text = "断开Socket"
                    }
                }

                SocketStatusListener.SocketStatus.DISCONNECTED-> {
                    viewMainBinding.socketStatus.text = "Socket:DISCONNECTED"
                    viewMainBinding.btnSocket.apply {
                        isEnabled = true
                        text = "连接Socket"
                    }
                }
                SocketStatusListener.SocketStatus.CONNECT_ERROR-> {
                    viewMainBinding.socketStatus.text = "Socket:CONNECT_ERROR"
                    viewMainBinding.btnSocket.apply {
                        isEnabled = true
                        text = "重试Socket"
                    }
                }
            }
        }

        lifecycleScope.launch {
            socketEventViewModel.mouseEventFlow.collect {
                it?.let {
                    when (it) {
                        is MouseEvent.Down -> {
                            viewMainBinding.mouseEvent.text = "鼠标事件: MouseDown"
                        }
                        is MouseEvent.Move -> {
                            viewMainBinding.mouseEvent.text = "鼠标事件: MouseMove"
                        }
                        is MouseEvent.Up -> {
                            viewMainBinding.mouseEvent.text = "鼠标事件: MouseUp"
                        }
                    }
                }
            }
        }
    }
}
