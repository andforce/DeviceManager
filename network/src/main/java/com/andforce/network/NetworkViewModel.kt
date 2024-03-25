package com.andforce.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NetworkViewModel : ViewModel() {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("NetworkViewModel", "request: ${request.url}")
            Log.d("NetworkViewModel", "response: $response")
            response
        }
        .build()

    private val retrofit2 = Retrofit.Builder()
//        .baseUrl("http://10.66.32.51:3001")
//        .baseUrl("http://192.168.8.90:3001")
        .baseUrl("http://10.66.50.84:3001")
//        .baseUrl("http://192.168.2.183:3001")
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    private val downloadService2 = retrofit2.create(ApiService::class.java)

    fun uploadAppInfoList(appInfo: List<AppInfo>) {
        viewModelScope.launch {
            val response = downloadService2.postAppInfo(appInfo)
            Log.d("NetworkViewModel", "postAppInfo response: $response")
        }
    }
}