package com.andforce.device.apps

import com.andforce.network.api.JsonResponseResult
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("post_appinfo")
    suspend fun postAppInfo(@Body appInfo: List<@JvmSuppressWildcards AppInfo>): JsonResponseResult<Any>

}