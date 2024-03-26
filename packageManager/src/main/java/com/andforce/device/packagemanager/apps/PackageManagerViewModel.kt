package com.andforce.device.packagemanager.apps

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andforce.device.packagemanager.PackageManagerHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PackageManagerViewModel: ViewModel()  {
    private val _installedApps = MutableLiveData<List<AppBean>>()
    val installedAppsLiveData: LiveData<List<AppBean>> = _installedApps

    private val _uninstallSuccess = MutableLiveData<Boolean>()
    val uninstallSuccess: LiveData<Boolean> = _uninstallSuccess

    private val _installSuccess = MutableLiveData<Boolean>()
    val installSuccess: LiveData<Boolean> = _installSuccess


    suspend fun loadInstalledApps(context: Context) = withContext(Dispatchers.IO) {
        loadInstalledAppsInner(context, this)
    }

    private fun loadInstalledAppsInner(context: Context, scope: CoroutineScope) {
        val packageManagerHelper = PackageManagerHelper(context)
        val apps = packageManagerHelper.installedApps.filter { it.packageName != "com.andforce.device.manager" }
        _installedApps.postValue(apps)
    }

    suspend fun uninstallApp(applicationContext: Context, packageName: String) = withContext(Dispatchers.IO) {
        val helper = PackageManagerHelper(applicationContext).also {
            it.deletePackage(packageName)
        }
        helper.registerListener { actionType, success ->
            if (actionType == PackageManagerHelper.ACTION_TYPE_UNINSTALL) {
                if (success) {
                    loadInstalledAppsInner(applicationContext, this)
                }
                _uninstallSuccess.postValue(success)
            }
        }
    }

    suspend fun installApp(applicationContext: Context, packageName: File) = withContext(Dispatchers.IO) {
        val helper = PackageManagerHelper(applicationContext).also {
            it.installPackage(packageName)
        }
        helper.registerListener { actionType, success ->
            if (actionType == PackageManagerHelper.ACTION_TYPE_INSTALL) {
                if (success) {
                    //loadInstalledApps(applicationContext)
                    loadInstalledAppsInner(applicationContext, this)
                }
                _installSuccess.postValue(success)
            }
        }
    }
}