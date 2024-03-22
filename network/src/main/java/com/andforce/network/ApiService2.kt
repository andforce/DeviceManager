package com.andforce.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService2 {

    // post 下面的数据到服务器
    // [ { "package": "com.abc.nihao", "name": "测试" }, { "package": "com.abc.nihao", "name": "测试" } ]
    // post_appinfo
    @POST("post_appinfo")
    suspend fun postAppInfo(@Body appInfo: List<@JvmSuppressWildcards AppInfo>): Response<ApiResult>

}