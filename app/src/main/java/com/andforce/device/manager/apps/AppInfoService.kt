package com.andforce.device.manager.apps

import com.andforce.network.api.JsonResponseResult
import retrofit2.http.Body
import retrofit2.http.POST

interface AppInfoService {

    @POST("post_appinfo")
    suspend fun postAppInfo(@Body appInfo: List<@JvmSuppressWildcards AppInfo>): JsonResponseResult<Any>

}