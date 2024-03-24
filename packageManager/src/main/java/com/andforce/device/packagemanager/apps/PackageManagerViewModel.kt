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
import java.io.File

class PackageManagerViewModel: ViewModel()  {
    private val _installedApps = MutableLiveData<List<AppBean>>()
    val installedApps: LiveData<List<AppBean>> get() = _installedApps

    private val _uninstallSuccess = MutableLiveData<Boolean>()
    val uninstallSuccess: LiveData<Boolean> get() = _uninstallSuccess

    private val _installSuccess = MutableLiveData<Boolean>()
    val installSuccess: LiveData<Boolean> get() = _installSuccess

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

    fun uninstallApp(applicationContext: Context, packageName: String) {
        viewModelScope.launch {
            val helper = PackageManagerHelper(applicationContext).also {
                it.deletePackage(packageName)
            }
            helper.registerListener { actionType, success ->
                if (actionType == PackageManagerHelper.ACTION_TYPE_UNINSTALL) {
                    if (success) {
                        loadInstalledApps(applicationContext)
                    }
                    _uninstallSuccess.postValue(success)
                }
            }
        }
    }

    fun installApp(applicationContext: Context, packageName: File) {
        viewModelScope.launch {
            val helper = PackageManagerHelper(applicationContext).also {
                it.installPackage(packageName)
            }
            helper.registerListener { actionType, success ->
                if (actionType == PackageManagerHelper.ACTION_TYPE_INSTALL) {
                    if (success) {
                        loadInstalledApps(applicationContext)
                    }
                    _installSuccess.postValue(success)
                }
            }
        }
    }
}

data class AppBean(val isSystem: Boolean, val packageName: String, val appName: String, val icon: Drawable)