package com.andforce.network.api

import com.andforce.network.api.bean.AppInfo
import com.andforce.network.api.bean.ResponseResult
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("post_appinfo")
    suspend fun postAppInfo(@Body appInfo: List<@JvmSuppressWildcards AppInfo>): ResponseResult<Any>

}