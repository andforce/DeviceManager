package com.andforce.device.manager

import android.app.Application
import com.andforce.network.api.ApiViewModel
import com.andforce.device.packagemanager.apps.PackageManagerViewModel
import com.andforce.network.download.DownloaderViewModel
import com.andforce.screen.cast.coroutine.ScreenCastViewModel
import com.andforce.socket.viewmodel.SocketEventViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 定义 Koin 模块
        val myModule = module {
            // 将 MyViewModel 定义为全局单例
            single { ScreenCastViewModel() }
            single { SocketEventViewModel() }
            single { PackageManagerViewModel() }
            single { ApiViewModel() }
            single { DownloaderViewModel() }
        }

        startKoin {
            androidContext(this@App)
            modules(myModule)
        }
    }
}