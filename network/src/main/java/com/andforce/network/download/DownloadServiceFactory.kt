package com.andforce.network.download

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object DownloadServiceFactory {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("DownloaderViewModel", "request: ${request.url}")
            Log.d("DownloaderViewModel", "response: $response")
            response
        }
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .client(okHttpClient)
        // 无实际意义, 只是为了创建Retrofit实例, 后续会动态替换, 如果不设置baseUrl, 后续会报错
        .baseUrl("http://localhost")
        .build()

    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}