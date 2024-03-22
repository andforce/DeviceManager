package com.andforce.network

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


class NetworkViewModel : ViewModel() {
    private val client = OkHttpClient.Builder()
        // 添加拦截器，打印所有的请求和响应
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("NetworkViewModel", "request: ${request.url}")
            Log.d("NetworkViewModel", "response: $response")
            response
        }
        .build()
    private val retrofit = Retrofit.Builder()
//        .baseUrl("http://10.66.50.84:3001")
        .baseUrl("http://10.66.32.51:3001")
        //.baseUrl("http://192.168.2.183:3001")
        // 使用OKHttp下载
        .client(client)
        .build()

    private val downloadService = retrofit.create(ApiService::class.java)

    private val _stateFlow = MutableStateFlow<File?>(null)
    val fileDownloadStateFlow: Flow<File> = _stateFlow.filter { it != null }.mapNotNull { it }

    fun downloadApk(context: Context, name: String, url: String) {
        viewModelScope.launch {
            val response = downloadService.downloadFile(url)
            dowload(context, response) {
                success {
                    _stateFlow.value = it
                }
                error {
                    Log.e("NetworkViewModel", "download error: $it")
                }
            }.startDowload()
        }
    }




    private val client2 = OkHttpClient.Builder()
        // 添加拦截器，打印所有的请求和响应
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("NetworkViewModel", "request: ${request.url}")
            Log.d("NetworkViewModel", "response: $response")
            response
        }
        .build()
    private val retrofit2 = Retrofit.Builder()
//        .baseUrl("http://10.66.50.84:3001")
        .baseUrl("http://10.66.32.51:3001")
        //.baseUrl("http://192.168.2.183:3001")
        .addConverterFactory(GsonConverterFactory.create())
        // 使用OKHttp下载
        .client(client2)
        .build()

    private val downloadService2 = retrofit2.create(ApiService2::class.java)

    fun postAppInfo(url: String, appInfo: List<AppInfo>) {
        viewModelScope.launch {
//            val requestBody = RequestBody.create(
//                "Content-Type, application/json".toMediaTypeOrNull(),
//                Gson().toJson(appInfo)
//            )

            val response = downloadService2.postAppInfo(appInfo)
            Log.d("NetworkViewModel", "postAppInfo response: $response")
        }
    }
}