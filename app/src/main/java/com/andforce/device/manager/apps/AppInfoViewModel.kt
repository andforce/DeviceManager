package com.andforce.device.manager.apps

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.network.ServiceFactory
import com.andforce.network.api.jsonApiCall
import kotlinx.coroutines.launch


class AppInfoViewModel : ViewModel() {

    private val mAppInfoService = ServiceFactory.createService(AppInfoService::class.java)

    fun uploadAppInfoList(appInfo: List<AppInfo>) {
        viewModelScope.launch {
            val responseResult = jsonApiCall { mAppInfoService.postAppInfo(appInfo) }
            Log.d("NetworkViewModel", "postAppInfo response: $responseResult")
        }
    }
}