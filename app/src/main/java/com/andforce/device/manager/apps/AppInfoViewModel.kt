package com.andforce.device.manager.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import com.andforce.device.manager.BuildConfig
import com.andforce.network.ServiceFactory
import com.andforce.network.api.jsonApiCall


class AppInfoViewModel : ViewModel() {

    private val appInfoService = ServiceFactory.createService(BuildConfig.HOST, AppInfoService::class.java)

    suspend fun uploadAppInfoList(appInfo: List<AppInfo>) {
        val responseResult = jsonApiCall { appInfoService.postAppInfo(appInfo) }
        Log.d("NetworkViewModel", "postAppInfo response: $responseResult")
    }
}