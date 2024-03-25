package com.andforce.network.download

import android.util.Log
import com.google.gson.JsonParseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.IOException
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend inline fun downloadApiCall(crossinline call: suspend CoroutineScope.() -> Response<ResponseBody>): Response<ResponseBody> {
    return withContext(Dispatchers.IO) {
        try {
            return@withContext call()
        } catch (e: Throwable) {
            Log.e("ApiCaller", "apiCall() error: ", e)
            return@withContext e.toResponse()
        }
    }
}

const val CODE_CONNECT_ERROR = -4001
const val CODE_CONNECT_TIMEOUT_ERROR = -4000
const val CODE_MAYBE_SERVER_ERROR = -5000

const val CODE_JSON_PARSE_ERROR = -1000

fun Throwable.toResponse(): Response<ResponseBody> {
    return when (this) {
        is android.system.ErrnoException -> {
            Response.error(
                CODE_CONNECT_ERROR,
                "ErrnoException, 网络连接失败，请检查后再试".toResponseBody()
            )
        }

        is java.net.ConnectException -> {
            Response.error(
                CODE_CONNECT_ERROR,
                "ConnectException, 网络连接失败，请检查后再试".toResponseBody()
            )
        }

        is HttpException -> {
            Response.error(
                CODE_CONNECT_ERROR,
                "HttpException, 网络异常(${this.code()},${this.message()})".toResponseBody()
            )
        }

        is UnknownHostException -> {
            Response.error(
                CODE_CONNECT_ERROR,
                "UnknownHostException, 网络连接失败，请检查后再试".toResponseBody()
            )
        }

        is SocketTimeoutException -> {
            Response.error(
                CODE_CONNECT_TIMEOUT_ERROR,
                "SocketTimeoutException, 请求超时，请稍后再试".toResponseBody()
            )
        }

        is IOException -> {
            Response.error(
                CODE_CONNECT_ERROR,
                "IOException, 网络异常(${this.message})".toResponseBody()
            )
        }

        is JsonParseException, is JSONException -> {
            Response.error(
                CODE_JSON_PARSE_ERROR,
                "JsonParseException, 数据解析错误，请稍后再试".toResponseBody()
            )
        }

        else -> {
            Response.error(CODE_MAYBE_SERVER_ERROR, "系统错误(${this.message})".toResponseBody())
        }
    }
}