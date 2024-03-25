package com.andforce.socket.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.andforce.socket.BuildConfig
import com.andforce.socket.SocketClient
import com.andforce.socket.apkevent.ApkPushEvent
import com.andforce.socket.apkevent.ApkUninstallEvent
import com.andforce.socket.mouseevent.MouseEvent
import com.andforce.socket.mouseevent.SocketStatusListener
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SocketEventViewModel : ViewModel() {
    private val socketEventRepository = SocketEventRepository()

    private var socketClient: SocketClient? = null
    private var socketUrl: String = BuildConfig.HOST

    private val _socketStatueEventFlow = MutableSharedFlow<SocketStatusListener.SocketStatus>(replay = 0, extraBufferCapacity = 1024, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    var socketStatusLiveData = _socketStatueEventFlow.asLiveData()

    // apk file push event
    private val _apkFilePushEventFlow = MutableStateFlow<ApkPushEvent?>(null)
    val apkFilePushEventFlow: StateFlow<ApkPushEvent?> = _apkFilePushEventFlow

    private val _apkUninstallEventFlow = MutableSharedFlow<ApkUninstallEvent?>(replay = 0, extraBufferCapacity = 1024, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val apkUninstallEventFlow: SharedFlow<ApkUninstallEvent?> = _apkUninstallEventFlow

    // mouse event
    private val _mouseEventFlow = MutableSharedFlow<MouseEvent?>(replay = 0)
    var mouseEventFlow: SharedFlow<MouseEvent?> = _mouseEventFlow

    private val _mouseMoveEventFlow = MutableSharedFlow<MouseEvent?>(replay = 0, extraBufferCapacity = 1024, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    var mouseMoveEventFlow: SharedFlow<MouseEvent?> = _mouseMoveEventFlow

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
                socketEventRepository.listenMouseEventFromSocket(it).collect { mouseEvent->
                    _mouseEventFlow.emit(mouseEvent)
                }
            }
        }
    }

    fun listenMouseMoveEventFromSocket() {
        viewModelScope.launch {
            socketClient?.let {
                socketEventRepository.listenMouseMoveEventFromSocket(it).collect { mouseEvent->
                    _mouseMoveEventFlow.emit(mouseEvent)
                }
            }
        }
    }

    fun listenApkFilePushEvent() {
        viewModelScope.launch {
            socketClient?.let {
                socketEventRepository.listenApkFilePushEvent(it).collect { apkEvent->
                    _apkFilePushEventFlow.emit(apkEvent)
                }
            }
        }
    }

    fun listenApkUninstallEvent() {
        viewModelScope.launch {
            socketClient?.let {
                socketEventRepository.listenApkUninstallEvent(it).collectLatest { apkEvent->
                    _apkUninstallEventFlow.emit(apkEvent)
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

    suspend fun sendBitmapToServer(byteArray: ByteArray?) {
        byteArray?.let {
            val isConnect = socketClient?.isConnected() ?: false
            Log.i("CAPTURE", "start sendBitmapToServer, isConnect:$isConnect")
            Log.i("CAPTURE", "------------------send finish------------------")
            if (isConnect.not()) {
                return
            }
            socketClient?.send(byteArray)
        }
    }
}