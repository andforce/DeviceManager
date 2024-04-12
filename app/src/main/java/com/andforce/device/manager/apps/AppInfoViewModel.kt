package com.andforce.device.manager.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import com.andforce.network.ServiceFactory
import com.andforce.network.api.jsonApiCall


class AppInfoViewModel : ViewModel() {

    // TODO: 替换成真实的HOST
    private val host = "http://localhost"
    private val appInfoService = ServiceFactory.createService(host, AppInfoService::class.java)

    suspend fun uploadAppInfoList(appInfo: List<AppInfo>) {
        val responseResult = jsonApiCall { appInfoService.postAppInfo(appInfo) }
        Log.d("NetworkViewModel", "postAppInfo response: $responseResult")
    }
}