package com.andforce.device.manager

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MainActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewMainBinding.root)

        // 启动Socket
        val intent = android.content.Intent(this, com.andforce.socket.SocketEventService::class.java)
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

        lifecycleScope.launch {
            recordViewModel.recordState.observe(this@MainActivity) {
                when (it) {
                    is RecordState.Recording -> {
                        viewMainBinding.tvInfo.text = "Recording"
                    }
                    is RecordState.Stopped -> {
                        viewMainBinding.tvInfo.text = "Stopped"
                    }
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
                lifecycleScope.launch {
                    viewModel.createScreenCaptureIntent()
                }
            }
        }

        viewMainBinding.checkboxRemoteControl.setOnClickListener() {
            val intent = android.content.Intent(this, com.andforce.socket.SocketEventService::class.java)
            startService(intent)
        }

        lifecycleScope.launch {
            socketEventViewModel.socketStatusLiveData.observe(this@MainActivity) {
                when(it) {
                    SocketStatusListener.SocketStatus.CONNECTING -> {
                        viewMainBinding.socketStatus.text = "Socket:CONNECTING"
                        viewMainBinding.checkboxRemoteControl.isEnabled = true
                    }
                    SocketStatusListener.SocketStatus.CONNECTED -> {
                        viewMainBinding.socketStatus.text = "Socket:CONNECTED"
                        viewMainBinding.checkboxRemoteControl.isEnabled = false
                    }

                    SocketStatusListener.SocketStatus.DISCONNECTED-> {
                        viewMainBinding.socketStatus.text = "Socket:DISCONNECTED"
                        viewMainBinding.checkboxRemoteControl.isEnabled = true
                    }
                    SocketStatusListener.SocketStatus.CONNECT_ERROR-> {
                        viewMainBinding.socketStatus.text = "Socket:CONNECT_ERROR"
                        viewMainBinding.checkboxRemoteControl.isEnabled = true
                    }
                }
            }
        }
        lifecycleScope.launch {
            socketEventViewModel.mouseEventFlow.collect {
                it?.let {
                    when (it) {
                        is MouseEvent.Down -> {
                            viewMainBinding.tvInfo.text = "MouseDown"
                        }
                        is MouseEvent.Move -> {
                            viewMainBinding.tvInfo.text = "MouseMove"
                        }
                        is MouseEvent.Up -> {
                            viewMainBinding.tvInfo.text = "MouseUp"
                        }
                    }
                }
            }
        }
    }
}
