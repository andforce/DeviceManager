package com.andforce.network.api

import android.system.ErrnoException
import android.util.Log
import com.andforce.network.api.bean.ResponseResult
import com.google.gson.JsonParseException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.IOException
import org.json.JSONException
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

suspend inline fun <T> apiCall(crossinline call: suspend CoroutineScope.() -> ResponseResult<T>): ResponseResult<T> {
    return withContext(Dispatchers.IO) {

        try {
            return@withContext call()
        } catch (e: Throwable) {
            Log.e("ApiCaller", "apiCall() error: ", e)
            return@withContext e.toResponseResult()
        }
    }
}

const val CODE_CONNECT_ERROR = -4001
const val CODE_CONNECT_TIMEOUT_ERROR = -4000
const val CODE_MAYBE_SERVER_ERROR = -5000

const val CODE_JSON_PARSE_ERROR = -1000

fun <T> Throwable.toResponseResult(): ResponseResult<T> {
    return when (this) {
        is ErrnoException -> {
            ResponseResult(CODE_CONNECT_ERROR, "ErrnoException, 网络连接失败，请检查后再试")
        }

        is ConnectException -> {
            ResponseResult(CODE_CONNECT_ERROR, "ConnectException, 网络连接失败，请检查后再试")
        }

        is HttpException -> {
            ResponseResult(
                CODE_CONNECT_ERROR,
                "HttpException, 网络异常(${this.code()},${this.message()})"
            )
        }

        is UnknownHostException -> {
            ResponseResult(CODE_CONNECT_ERROR, "UnknownHostException, 网络连接失败，请检查后再试")
        }

        is SocketTimeoutException -> {
            ResponseResult(CODE_CONNECT_TIMEOUT_ERROR, "SocketTimeoutException, 请求超时，请稍后再试")
        }

        is IOException -> {
            ResponseResult(CODE_CONNECT_ERROR, "IOException, 网络异常(${this.localizedMessage})")
        }

        is JsonParseException, is JSONException -> {
            ResponseResult(CODE_JSON_PARSE_ERROR, "JsonParseException, 数据解析错误，请稍后再试")
        }

        else -> {
            ResponseResult(CODE_MAYBE_SERVER_ERROR, "系统错误(${this.localizedMessage})")
        }
    }
}