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


    fun <T> createService(serviceClass: Class<T>): T {
        return retrofit.create(serviceClass)
    }
}