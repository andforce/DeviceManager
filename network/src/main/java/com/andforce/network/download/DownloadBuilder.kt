package com.andforce.network.download

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Created by jingzz on 2020/7/9.
 */
typealias DOWLOAD_ERROR = (Throwable) -> Unit
typealias DOWLOAD_PROCESS = (downloadedSize: Long, length: Long, process: Float) -> Unit
typealias DOWLOAD_SUCCESS = (uri: File) -> Unit

suspend fun download(context: Context, response: Response<ResponseBody>, block: DownloadBuilder.()->Unit): DownloadBuilder {
    val build = DownloadBuilder(context, response)
    build.block()
    return build
}

class DownloadBuilder(context: Context, val response: Response<ResponseBody>) {
    private var error: DOWLOAD_ERROR = {} //错误贺词
    private var process: DOWLOAD_PROCESS = { downloadedSize, filsLength, process -> } //进度
    private var success: DOWLOAD_SUCCESS = {} //下载完成
    var setUri: () -> Uri? = { null } //设置下载的uri
    var setFileName: () -> String? = { null } //设置文件名

    fun process(process: DOWLOAD_PROCESS) {
        this.process = process
    }

    fun error(error: DOWLOAD_ERROR) {
        this.error = error
    }

    fun success(success: DOWLOAD_SUCCESS) {
        this.success = success
    }

    suspend fun startDownload() {

        withContext(Dispatchers.Main){
            //使用流获取下载进度
            flow.flowOn(Dispatchers.IO)
                .collect {
                    when(it){
                        is DowloadStatus.DowloadErron -> error(it.t)
                        is DowloadStatus.DowloadProcess -> process(it.currentLength,it.length,it.process)
                        is DowloadStatus.DowloadSuccess -> success(it.uri)
                    }
                }
        }
    }
    private val flow = flow {
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
                        "${System.currentTimeMillis()}.${MimeTypeMap.getSingleton()
                            .getExtensionFromMimeType(contentType)}"
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
                    DowloadStatus.DowloadProcess(
                        currentLength.toLong(),
                        length,
                        currentLength.toFloat() / length.toFloat()
                    )
                )
            }
            bufferedInputStream.close()
            ops.close()
            ios.close()
            emit(DowloadStatus.DowloadSuccess(file!!))

        } catch (e: Exception) {
            emit(DowloadStatus.DowloadErron(e))
        }
    }



}

sealed class DowloadStatus{
    class DowloadProcess(val currentLength:Long,val length:Long,val process:Float): DowloadStatus()
    class DowloadErron(val t:Throwable): DowloadStatus()
    class DowloadSuccess(val uri:File): DowloadStatus()
}
