package com.andforce.network.download

import android.content.Context
import android.util.Log
import com.andforce.network.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object FileDownloader {
    private val okHttpClient = OkHttpClient.Builder()
        // 添加拦截器，打印所有的请求和响应
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
        .baseUrl(BuildConfig.HOST)
        .client(okHttpClient)
        .build()

    private val downloadService = retrofit.create(DownloadApiService::class.java)
    suspend fun download(
        context: Context,
        url: String,
        action: DownloadBuilder.() -> Unit
    ) {
        val response = downloadService.downloadFile(url)
        val build = DownloadBuilder(context, response)
        build.action()
        build.startDownload()
    }
}