package com.andforce.network

import android.util.Log
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ServiceFactory {

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request()
            val response = it.proceed(request)
            Log.d("NetworkViewModel", "request: ${request.url}")
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