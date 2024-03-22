package com.andforce.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Streaming
import retrofit2.http.Url

interface ApiService {
    @Streaming
    @GET
    suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>

}