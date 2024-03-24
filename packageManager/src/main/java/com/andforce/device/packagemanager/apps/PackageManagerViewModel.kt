package com.andforce.device.packagemanager.apps

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.device.packagemanager.PackageManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PackageManagerViewModel: ViewModel()  {
    private val _installedApps = MutableLiveData<List<AppBean>>()
    val installedApps: LiveData<List<AppBean>> get() = _installedApps

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            val list = mutableListOf<AppBean>()

            withContext(Dispatchers.IO) {
                // 使用协程加载应用列表
                val pm = context.packageManager
                val apps = pm.getInstalledPackages(0)

                for (app in apps) {
                    val isSystem = app.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
                    val packageName = app.packageName
                    val appName = app.applicationInfo.loadLabel(pm).toString()
                    val icon = app.applicationInfo.loadIcon(pm)
                    list.add(AppBean(isSystem, packageName, appName, icon))
                }
            }
            _installedApps.value = list.filter { it.packageName != "com.andforce.device.manager" }
        }
    }

    fun uninstallApp(applicationContext: Context, appBean: AppBean) {
        val helper = PackageManagerHelper(applicationContext).also {
            it.deletePackage(appBean.packageName)
        }
        helper.registerListener { actionType, success ->
            if (actionType == PackageManagerHelper.ACTION_TYPE_UNINSTALL && success) {
                loadInstalledApps(applicationContext)
            }
        }
    }
}

data class AppBean(val isSystem: Boolean, val packageName: String, val appName: String, val icon: Drawable)