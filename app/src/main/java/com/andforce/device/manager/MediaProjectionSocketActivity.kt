package com.andforce.device.manager

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andforce.device.manager.databinding.MediaprojectionActivityMainBinding
import com.andforce.socket.MouseEvent
import com.andforce.device.packagemanager.apps.AppBean
import com.andforce.device.packagemanager.apps.InstalledAppAdapter
import com.andforce.device.packagemanager.apps.OnUninstallClickListener
import com.andforce.device.packagemanager.apps.PackageManagerViewModel
import com.andforce.screen.cast.coroutine.RecordViewModel
import com.andforce.socket.SocketViewModel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject


class MediaProjectionSocketActivity : AppCompatActivity() {

    private val viewModel by lazy {
        ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MediaProjectionViewModel(this@MediaProjectionSocketActivity) as T
            }
        })[MediaProjectionViewModel::class.java]
    }

    private val viewMainBinding by lazy {
        MediaprojectionActivityMainBinding.inflate(layoutInflater)
    }

    private val recordViewModel: RecordViewModel by inject()
    private val socketViewModel: SocketViewModel by inject()
    private val packageManagerViewModel: PackageManagerViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewMainBinding.root)

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

        recordViewModel.recordState.observe(this) {
            when (it) {
                is RecordViewModel.RecordState.Recording -> {
                    viewMainBinding.tvInfo.text = "Recording"
                }
                is RecordViewModel.RecordState.Stopped -> {
                    viewMainBinding.tvInfo.text = "Stopped"
                }
            }
        }

        viewModel.result.observe(this) {
            when (it) {
                is MediaProjectionViewModel.Result.Success -> {
                    CastService.startService(this, false, it.data, it.resultCode)
                }
                MediaProjectionViewModel.Result.PermissionDenied -> {
                    Toast.makeText(this, "User did not grant permission", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewMainBinding.btnStart.setOnClickListener {
            if (recordViewModel.recordState.value is RecordViewModel.RecordState.Recording) {
                Toast.makeText(this, "Recording, no need start", Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    viewModel.createScreenCaptureIntent()
                }
            }
        }

        viewMainBinding.btnTest.setOnClickListener {
            Toast.makeText(this, "收到点击事件了", Toast.LENGTH_SHORT).show()
        }

        lifecycleScope.launch {
            socketViewModel.eventFlow.collect {
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
