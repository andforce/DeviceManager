package com.andforce.device.manager

import android.app.Application
import com.andforce.network.NetworkViewModel
import com.andforce.device.manager.apps.PackageManagerViewModel
import com.andforce.device.manager.socket.SocketViewModel
import com.andforce.screen.cast.coroutine.RecordViewModel
import kotlinx.coroutines.MainScope
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module


class App : Application() {
    override fun onCreate() {
        super.onCreate()

        // 定义 Koin 模块
        val myModule = module {
            // 将 MyViewModel 定义为全局单例
            single { RecordViewModel(MainScope()) }
            single { SocketViewModel() }
            single { PackageManagerViewModel() }
            single { NetworkViewModel() }
        }

        startKoin {
            androidContext(this@App)
            modules(myModule)
        }
    }
}