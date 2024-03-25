package com.andforce.network

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


class NetworkViewModel : ViewModel() {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("NetworkViewModel", "request: ${request.url}")
            Log.d("NetworkViewModel", "response: $response")
            response
        }
        .callTimeout(10, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.HOST)
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)
        .build()
    private val apiService = retrofit.create(ApiService::class.java)

    fun uploadAppInfoList(appInfo: List<AppInfo>) {
        viewModelScope.launch {
            val responseResult = apiCall { apiService.postAppInfo(appInfo) }
            Log.d("NetworkViewModel", "postAppInfo response: $responseResult")
        }
    }
}