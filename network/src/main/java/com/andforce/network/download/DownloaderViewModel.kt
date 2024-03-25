package com.andforce.network.download

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andforce.network.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File


class DownloaderViewModel : ViewModel() {
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

    private val _stateFlow = MutableStateFlow<File?>(null)
    val fileDownloadStateFlow: Flow<File> = _stateFlow.filter { it != null }.mapNotNull { it }

    private val _downloadProcessFlow = MutableLiveData(0f)
    val downloadProcessFlow: LiveData<Float> = _downloadProcessFlow

    fun downloadApk(context: Context, url: String) {
        viewModelScope.launch {
            val response = downloadService.downloadFile(url)
            download(context, response) {
                success {
                    _stateFlow.value = it
                }
                process { _, _, process ->
                    _downloadProcessFlow.value = process
                }
                error {
                    Log.e("NetworkViewModel", "download error: $it")
                }
            }.startDownload()
        }
    }
}