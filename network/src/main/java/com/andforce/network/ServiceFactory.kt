package com.andforce.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceFactory {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("NetworkViewModel", "request: ${request.url}")

            // 如果是POST请求，打印请求体
            if (request.method == "POST") {
                val json = try {
                    request.body?.let {
                        val buffer = okio.Buffer()
                        it.writeTo(buffer)
                        buffer.readUtf8().also {
                            buffer.close()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
                Log.d("NetworkViewModel", "request body: $json")
            }

            val response = chain.proceed(request)
            Log.d("NetworkViewModel", "response: $response")
            response
        }
        .callTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofitBuilder = Retrofit.Builder()
        .addConverterFactory(GsonConverterFactory.create())
        .client(okHttpClient)

    fun <T> createService(baseHost: String, serviceClass: Class<T>): T {
        return retrofitBuilder.baseUrl(baseHost).build().create(serviceClass)
    }
}