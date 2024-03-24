package com.andforce.device.packagemanager.apps

import android.content.Context
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
    val installedAppsLiveData: LiveData<List<AppBean>> = _installedApps

    private val _uninstallSuccess = MutableLiveData<Boolean>()
    val uninstallSuccess: LiveData<Boolean> = _uninstallSuccess

    private val _installSuccess = MutableLiveData<Boolean>()
    val installSuccess: LiveData<Boolean> = _installSuccess


    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val packageManagerHelper = PackageManagerHelper(context)
                val apps = packageManagerHelper.installedApps.filter { it.packageName != "com.andforce.device.manager" }
                _installedApps.postValue(apps)
            }
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