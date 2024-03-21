package com.andforce.socket

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class SocketEventViewModel : ViewModel() {
    private val socketEventRepository = SocketEventRepository()

    private var socketClient: SocketClient? = null
    //private var socketUrl: String = "http://192.168.2.183:3001"
    private var socketUrl: String = "http://10.66.32.51:3001"

    private val _socketStatueEventFlow = MutableSharedFlow<SocketStatusListener.SocketStatus>(replay = 0)
    var socketStatusLiveData = _socketStatueEventFlow.asLiveData()

    private val _apkFilePushEventFlow = MutableStateFlow<ApkEvent?>(null)
    val apkFilePushEventFlow: Flow<ApkEvent?> = _apkFilePushEventFlow


    private val _mouseEventFlow = MutableSharedFlow<MouseEvent?>(replay = 0)
    var mouseEventFlow: SharedFlow<MouseEvent?> = _mouseEventFlow

    fun connectIfNeed() {

        if (socketClient == null) {
            socketClient = SocketClient(socketUrl)
        }

        if (socketClient?.isConnected() == true) {
            return
        }

        socketClient?.startConnection()
    }

    fun disconnect() {
        socketClient?.release()
        socketClient = null
    }

    fun listenMouseEventFromSocket() {
        viewModelScope.launch {
            socketClient?.let {
                socketEventRepository.listenMouseEventFromSocket(it).buffer(1024).collect { mouseEvent->
                    _mouseEventFlow.emit(mouseEvent)
                }
            }
        }
    }

    fun listenApkFilePushEvent() {
        viewModelScope.launch {
            socketClient?.let {
                socketEventRepository.listenApkFilePushEvent(it).collectLatest { apkEvent->
                    _apkFilePushEventFlow.emit(apkEvent)
                }
            }
        }
    }

    fun listenSocketStatus() {
        viewModelScope.launch {
            socketClient?.let {
                socketEventRepository.listenSocketStatus(it).collectLatest {status->
                    _socketStatueEventFlow.emit(status)
                }
            }
        }
    }

    suspend fun sendBitmapToServer(bitmap: Bitmap?) {
        bitmap?.let {
            withContext(Dispatchers.IO) {
                if (socketClient?.isConnected() == false) {
                    return@withContext
                }

                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                val byteArray = byteArrayOutputStream.toByteArray()

                socketClient?.send(byteArray)
                runCatching {
                    byteArrayOutputStream.close()
                }
                if (bitmap.isRecycled.not()) {
                    bitmap.recycle()
                }
            }
        }
    }
}