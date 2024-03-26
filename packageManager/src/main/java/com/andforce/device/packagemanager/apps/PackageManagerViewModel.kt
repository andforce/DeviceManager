package com.andforce.device.packagemanager.apps

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andforce.device.packagemanager.PackageManagerHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PackageManagerViewModel: ViewModel()  {
    private val _appLoadedList = MutableLiveData<List<AppBean>>()
    val appLoadedList: LiveData<List<AppBean>> = _appLoadedList

    private val _appUninstallResult = MutableLiveData<Boolean>()
    val appUninstallResult: LiveData<Boolean> = _appUninstallResult

    private val _appInstallResult = MutableLiveData<Boolean>()
    val appInstallResult: LiveData<Boolean> = _appInstallResult


    suspend fun loadInstalledApps(context: Context) = withContext(Dispatchers.IO) {
        val packageManagerHelper = PackageManagerHelper(context)
        val apps = packageManagerHelper.installedApps.filter { it.packageName != "com.andforce.device.manager" }
        _appLoadedList.postValue(apps)
    }

    suspend fun uninstallApp(applicationContext: Context, packageName: String) = withContext(Dispatchers.IO) {
        val helper = PackageManagerHelper(applicationContext).also {
            it.deletePackage(packageName)
        }
        helper.registerListener { actionType, success ->
            if (actionType == PackageManagerHelper.ACTION_TYPE_UNINSTALL) {
                _appUninstallResult.postValue(success)
            }
        }
    }

    suspend fun installApp(applicationContext: Context, packageName: File) = withContext(Dispatchers.IO) {
        val helper = PackageManagerHelper(applicationContext).also {
            it.installPackage(packageName)
        }
        helper.registerListener { actionType, success ->
            if (actionType == PackageManagerHelper.ACTION_TYPE_INSTALL) {
                _appInstallResult.postValue(success)
            }
        }
    }
}