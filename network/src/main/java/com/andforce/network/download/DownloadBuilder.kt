package com.andforce.network.download

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import com.andforce.network.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

private val okHttpClient = OkHttpClient.Builder()
    .addInterceptor {
        val request = it.request()
        val response = it.proceed(request)
        Log.d("DownloaderViewModel", "request: ${request.url}")
        Log.d("DownloaderViewModel", "response: $response")
        response
    }
    .retryOnConnectionFailure(true)
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.HOST)
    .client(okHttpClient)
    .build()

private val downloadService = retrofit.create(DownloadApiService::class.java)

class DownloadBuilder(context: Context, fileUrl: String) {

    private var error: (Throwable) -> Unit = {}
    private var process: (downloadedSize: Long, length: Long, process: Float) -> Unit =
        { _, _, _ -> }
    private var success: (downloadFile: File) -> Unit = {}

    var setUri: () -> Uri? = { null }
    var setFileName: () -> String? = { null }

    fun onProcess(process: (downloadedSize: Long, length: Long, process: Float) -> Unit) {
        this.process = process
    }

    fun onError(error: (Throwable) -> Unit) {
        this.error = error
    }

    fun onSuccess(success: (uri: File) -> Unit) {
        this.success = success
    }

    suspend fun startDownload() {

        withContext(Dispatchers.Main) {
            //使用流获取下载进度
            flow.flowOn(Dispatchers.IO)
                .collect {
                    when (it) {
                        is DownloadStatus.DownloadError -> error(it.t)
                        is DownloadStatus.DownloadProcess -> process(
                            it.currentLength,
                            it.length,
                            it.process
                        )

                        is DownloadStatus.DownloadSuccess -> success(it.uri)
                    }
                }
        }
    }

    private val flow = flow {
        val response = downloadApiCall { downloadService.downloadFile(fileUrl) }
        if (response.isSuccessful.not()) {
            val errorMessage = "${response.code()} ${response.errorBody()?.string()}"
            val error = RuntimeException(errorMessage)
            emit(DownloadStatus.DownloadError(error))
        } else {
            try {
                val body = response.body() ?: throw RuntimeException("下载出错")
                //文件总长度
                val length = body.contentLength()
                //文件 mineType
                val contentType = body.contentType()?.toString()
                val ios = body.byteStream()

                var file: File? = null
                val ops = kotlin.run {
                    setUri()?.let {
                        context.contentResolver.openOutputStream(it)
                    } ?: kotlin.run {
                        val fileName = setFileName() ?: kotlin.run {
                            //如果连文件名都不给，那就自己生成文件名
                            "${System.currentTimeMillis()}.${
                                MimeTypeMap.getSingleton()
                                    .getExtensionFromMimeType(contentType)
                            }"
                        }
                        file =
                            File("${context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)}${File.separator}$fileName")
                        FileOutputStream(file)
                    }
                }
                //下载的长度
                var currentLength = 0
                //写入文件
                val bufferSize = 1024 * 8
                val buffer = ByteArray(bufferSize)
                val bufferedInputStream = BufferedInputStream(ios, bufferSize)

                var readLength: Int
                while (bufferedInputStream.read(buffer, 0, bufferSize).also { readLength = it } != -1) {
                    ops.write(buffer, 0, readLength)
                    currentLength += readLength
                    emit(
                        DownloadStatus.DownloadProcess(
                            currentLength.toLong(),
                            length,
                            currentLength.toFloat() / length.toFloat()
                        )
                    )
                }
                bufferedInputStream.close()
                ops.close()
                ios.close()
                emit(DownloadStatus.DownloadSuccess(file!!))

            } catch (e: Exception) {
                emit(DownloadStatus.DownloadError(e))
            }
        }
    }.flowOn(Dispatchers.IO)
}

sealed class DownloadStatus {
    class DownloadProcess(val currentLength: Long, val length: Long, val process: Float) :
        DownloadStatus()

    class DownloadError(val t: Throwable) : DownloadStatus()
    class DownloadSuccess(val uri: File) : DownloadStatus()
}
