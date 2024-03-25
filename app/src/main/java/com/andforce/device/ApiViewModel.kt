package com.andforce.device

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.device.apps.ApiService
import com.andforce.device.apps.AppInfo
import com.andforce.network.ServiceFactory
import com.andforce.network.api.jsonApiCall
import kotlinx.coroutines.launch


class ApiViewModel : ViewModel() {

    private val apiService = ServiceFactory.createService(ApiService::class.java)

    fun uploadAppInfoList(appInfo: List<AppInfo>) {
        viewModelScope.launch {
            val responseResult = jsonApiCall { apiService.postAppInfo(appInfo) }
            Log.d("NetworkViewModel", "postAppInfo response: $responseResult")
        }
    }
}