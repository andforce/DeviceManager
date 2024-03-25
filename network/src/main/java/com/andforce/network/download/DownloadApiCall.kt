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

suspend inline fun apiCall(crossinline call: suspend CoroutineScope.() -> Response<ResponseBody>): Response<ResponseBody> {
    return withContext(Dispatchers.IO) {

        try {
            return@withContext call()
        } catch (e: Throwable) {
            Log.e("ApiCaller", "apiCall() error: ", e)
            return@withContext ApiException.build(e).toResponse()
        }
    }
}

// 网络、数据解析错误处理
class ApiException(
    private val code: Int,
    override val message: String?,
    override val cause: Throwable? = null
) : RuntimeException(message, cause) {
    companion object {
        // 网络状态码
        private const val CODE_NET_ERROR = 4000
        private const val CODE_TIMEOUT = 4080
        private const val CODE_JSON_PARSE_ERROR = 4010
        private const val CODE_SERVER_ERROR = 5000

        fun build(e: Throwable): ApiException {
            return when (e) {
                is android.system.ErrnoException -> {
                    ApiException(CODE_NET_ERROR, "ErrnoException, 网络连接失败，请检查后再试")
                }

                is java.net.ConnectException -> {
                    ApiException(CODE_NET_ERROR, "ConnectException,网络连接失败，请检查后再试")
                }

                is HttpException -> {
                    ApiException(CODE_NET_ERROR, "HttpException, 网络异常(${e.code()},${e.message()})")
                }

                is UnknownHostException -> {
                    ApiException(CODE_NET_ERROR, "UnknownHostException, 网络连接失败，请检查后再试")
                }

                is SocketTimeoutException -> {
                    ApiException(CODE_TIMEOUT, "SocketTimeoutException, 请求超时，请稍后再试")
                }

                is IOException -> {
                    ApiException(CODE_NET_ERROR, "IOException, 网络异常(${e.message})")
                }

                is JsonParseException, is JSONException -> {
                    ApiException(CODE_JSON_PARSE_ERROR, "JsonParseException, 数据解析错误，请稍后再试")
                }

                else -> {
                    ApiException(CODE_SERVER_ERROR, "系统错误(${e.message})")
                }
            }
        }
    }

    fun toResponse(): Response<ResponseBody> {
        return Response.error(code, (message ?: "").toResponseBody())
    }
}